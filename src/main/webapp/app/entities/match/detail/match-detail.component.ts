import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PetService } from '../../pet/service/pet.service';
import { PhotoService } from '../../photo/service/photo.service';
import { LikeeService } from '../../likee/service/likee.service';
import { EntityArrayResponseType, MatchService } from '../service/match.service';

import { IMatch } from '../match.model';
import { IPet } from '../../pet/pet.model';
import { IPhoto } from '../../photo/photo.model';
import { forkJoin, Observable } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { ILikee } from '../../likee/likee.model';

@Component({
  selector: 'jhi-match-detail',
  templateUrl: './match-detail.component.html',
  styleUrls: ['match-detail.component.css'],
})
export class MatchDetailComponent implements OnInit {
  match: IMatch | null = null;

  firstLikedIds: ILikee | null = null;
  currentPetId: number | null | undefined;
  matchedPetId: number | undefined;
  currentPetName: string | null | undefined;
  currentPetPhotoUrl: string | null | undefined;
  petsIds: number[] = [];
  petData: Map<number, { pet: IPet; photo: IPhoto | undefined }> = new Map();

  constructor(
    protected activatedRoute: ActivatedRoute,
    protected petService: PetService,
    protected photoService: PhotoService,
    protected likeeService: LikeeService,
    protected matchService: MatchService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ match }) => {
      this.match = match;
    });

    this.loadSearchCriteriaForCurrentUser();
  }

  loadSearchCriteriaForCurrentUser(): void {
    this.matchService.getCurrentUserPetId().subscribe(response => {
      this.currentPetId = response.body;
      console.log('PET ID: ' + this.currentPetId);
    });
    this.loadPetsFromLikee();
  }

  loadPetsFromLikee() {
    if (this.match && this.match.firstLiked && this.match.firstLiked.id) {
      this.likeeService.find(this.match.firstLiked.id).subscribe(
        (res: HttpResponse<ILikee>) => {
          this.firstLikedIds = res.body;
          if (this.firstLikedIds?.firstPet?.id && this.firstLikedIds?.secondPet?.id) {
            this.loadPetObjects(this.firstLikedIds.firstPet.id, this.firstLikedIds.secondPet.id);
          }
        },
        error => console.log('Error al cargar firstLikee:', error)
      );
    }
  }

  loadPetObjects(firstPetId: number, secondPetId: number) {
    const petRequests: Observable<HttpResponse<IPet>>[] = [firstPetId, secondPetId].map(petId => this.petService.find(petId));

    forkJoin(petRequests).subscribe(pets => {
      pets.forEach(petResponse => {
        if (petResponse.body) {
          this.petData.set(petResponse.body.id, { pet: petResponse.body, photo: undefined });
        }
      });

      console.log('Pet Data (antes de fotos):', this.petData);
      this.loadPetPhotos();
    });
  }

  loadPetPhotos() {
    const petIds = Array.from(this.petData.keys());
    const photoRequests: Observable<EntityArrayResponseType>[] = petIds.map(petId => this.photoService.findAllPhotosByPetID(petId));

    forkJoin(photoRequests).subscribe(photos => {
      photos.forEach((photoResponse, index) => {
        console.log('Photo Response:', photoResponse); // Agregamos este mensaje de depuración
        const photo = photoResponse.body?.length ? photoResponse.body[0] : undefined;
        if (photo) {
          const petId = petIds[index];
          const pet = this.petData.get(petId)?.pet;
          console.log('Pet ID:', petId, 'Pet:', pet); // Agregamos este mensaje de depuración
          if (pet) {
            this.petData.set(petId, { pet: pet, photo: photo });
          }
        }
      });

      // Aquí puedes usar petData para mostrar los nombres y fotos de las mascotas en el front-end
      console.log('Pet Data:', this.petData);

      if (this.currentPetId !== null && this.currentPetId !== undefined && this.petData.has(this.currentPetId)) {
        const currentPetEntry = this.petData.get(this.currentPetId);
        if (currentPetEntry) {
          this.currentPetName = currentPetEntry.pet.name;
          this.currentPetPhotoUrl = currentPetEntry.photo?.photoUrl;
        }
      }
    });
  }

  getOtherPetEntry(): { pet: IPet; photo?: IPhoto } | undefined {
    for (const petEntry of this.petData.values()) {
      if (petEntry.pet.id !== this.currentPetId) {
        this.matchedPetId = petEntry.pet.id;
        return petEntry;
      }
    }
    return undefined;
  }

  previousState(): void {
    window.history.back();
  }

  toChatSection() {
    this.router.navigate(['/pet/' + this.matchedPetId + '/view']);
  }

  backToFilters() {
    this.router.navigate(['']);
  }
}
