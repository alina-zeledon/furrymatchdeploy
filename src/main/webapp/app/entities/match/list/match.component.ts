import { AfterContentChecked, AfterContentInit, AfterViewChecked, Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router } from '@angular/router';
import { combineLatest, filter, forkJoin, Observable, switchMap, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ChangeDetectorRef } from '@angular/core';

import { IMatch } from '../match.model';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ASC, DESC, SORT, ITEM_DELETED_EVENT, DEFAULT_SORT_DATA } from 'app/config/navigation.constants';
import { EntityArrayResponseType, MatchService } from '../service/match.service';
import { MatchDeleteDialogComponent } from '../delete/match-delete-dialog.component';
import { IPet } from '../../pet/pet.model';
import { ILikee } from 'app/entities/likee/likee.model';
import { IPhoto } from '../../photo/photo.model';
import { PetService } from '../../pet/service/pet.service';
import { PhotoService } from '../../photo/service/photo.service';
import { LikeeService } from '../../likee/service/likee.service';

interface PetEntry {
  pet: IPet;
  photo: IPhoto | undefined;
  matchId: number;
}

@Component({
  selector: 'jhi-match',
  templateUrl: './match.component.html',
  styleUrls: ['./match.component.css'],
})
export class MatchComponent implements OnInit, AfterContentInit {
  matches?: IMatch[];

  currentPetId: number | null = null;
  firstLikedIds: { like: number; match: number }[] = [];
  petsIds: { petId: number; match: number }[] = [];

  petData: Map<number, { pet: IPet; photo: IPhoto | undefined; matchId: number }> = new Map();

  isLoading = false;

  predicate = 'id';
  ascending = true;

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  constructor(
    protected matchService: MatchService,
    protected activatedRoute: ActivatedRoute,
    public router: Router,
    protected modalService: NgbModal,
    protected petService: PetService,
    protected likeeService: LikeeService,
    protected photoService: PhotoService,
    private changeDetector: ChangeDetectorRef
  ) {}

  trackId = (_index: number, item: IMatch): number => this.matchService.getMatchIdentifier(item);

  ngAfterContentInit(): void {
    this.changeDetector.detectChanges();
  }

  ngOnInit(): void {
    this.loadSearchCriteriaForCurrentUser();
    this.load();
  }

  loadSearchCriteriaForCurrentUser(): void {
    this.matchService.getCurrentUserPetId().subscribe(response => {
      this.currentPetId = response.body;
      console.log('PET ID: ' + this.currentPetId);
    });
  }

  delete(match: IMatch): void {
    const modalRef = this.modalService.open(MatchDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.match = match;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        switchMap(() => this.loadFromBackendWithRouteInformations())
      )
      .subscribe({
        next: (res: EntityArrayResponseType) => {
          this.onResponseSuccess(res);
        },
      });
  }

  load(): void {
    this.loadFromBackendWithRouteInformations().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  navigateToWithComponentValues(): void {
    this.handleNavigation(this.page, this.predicate, this.ascending);
  }

  navigateToPage(page = this.page): void {
    this.handleNavigation(page, this.predicate, this.ascending);
  }

  protected loadFromBackendWithRouteInformations(): Observable<EntityArrayResponseType> {
    return combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
      tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
      switchMap(() => this.queryBackend(this.page, this.predicate, this.ascending))
    );
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    const page = params.get(PAGE_HEADER);
    this.page = +(page ?? 1);
    const sort = (params.get(SORT) ?? data[DEFAULT_SORT_DATA]).split(',');
    this.predicate = sort[0];
    this.ascending = sort[1] === ASC;
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.matches = dataFromBody;

    // Unique idLikees

    this.matches.forEach(match => {
      if (match.firstLiked?.id) {
        this.firstLikedIds.push({ like: match.firstLiked.id, match: match.id });
      }
    });
    console.log('Ids en tabla de matches: ' + this.firstLikedIds);

    this.loadPetsFromLikee();
  }

  loadPetsFromLikee() {
    const observables: Observable<HttpResponse<ILikee>>[] = [];

    this.firstLikedIds.forEach(likeeId => {
      observables.push(this.likeeService.find(likeeId.like));
    });

    let counter = 0;

    forkJoin(observables).subscribe((responses: HttpResponse<ILikee>[]) => {
      responses.forEach((response: HttpResponse<ILikee>) => {
        const likee = response.body;

        if (likee?.firstPet?.id === this.currentPetId || likee?.secondPet?.id === this.currentPetId) {
          if (likee.firstPet?.id && likee.firstPet.id !== this.currentPetId) {
            this.petsIds.push({ petId: likee.firstPet.id, match: this.firstLikedIds[counter].match });
          }
          if (likee.secondPet?.id && likee.secondPet.id !== this.currentPetId) {
            this.petsIds.push({ petId: likee.secondPet.id, match: this.firstLikedIds[counter].match });
          }
        }
        counter++;
      });

      console.log('Ids en tabla de likees: ' + this.petsIds);
      this.loadPetObjects();
    });
  }

  loadPetObjects() {
    const petRequests: Observable<HttpResponse<IPet>>[] = Array.from(this.petsIds).map(petId => this.petService.find(petId.petId));

    let counter2 = 0;
    forkJoin(petRequests).subscribe(pets => {
      pets.forEach(petResponse => {
        if (petResponse.body) {
          this.petData.set(petResponse.body.id, { pet: petResponse.body, photo: undefined, matchId: this.petsIds[counter2].match });
        }
        counter2++;
      });

      console.log('Pet Data (antes de fotos):', this.petData); // Agrega este mensaje de depuración
      this.loadPetPhotos();
    });
  }

  loadPetPhotos() {
    const photoRequests: Observable<EntityArrayResponseType>[] = Array.from(this.petsIds).map(petId =>
      this.photoService.findAllPhotosByPetID(petId.petId)
    );

    forkJoin(photoRequests).subscribe(photos => {
      photos.forEach((photoResponse, index) => {
        console.log('Photo Response:', photoResponse);
        const photo = photoResponse.body?.length ? photoResponse.body[0] : undefined;
        if (photo) {
          const petId = this.petsIds[index].petId;
          const matchId = this.petsIds[index].match;
          const pet = this.petData.get(petId)?.pet;
          console.log('Pet ID:', petId, 'Pet:', pet);
          if (pet) {
            this.petData.set(petId, { pet: pet, photo: photo, matchId: matchId });
            this.changeDetector.detectChanges(); // Añade esta línea
          }
        }
      });

      console.log('Pet Data:', this.petData);
    });
  }

  getPetDataValues(): PetEntry[] {
    return Array.from(this.petData.values());
  }

  protected fillComponentAttributesFromResponseBody(data: IMatch[] | null): IMatch[] {
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  }

  protected queryBackend(page?: number, predicate?: string, ascending?: boolean): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const pageToLoad: number = page ?? 1;
    const queryObject = {
      page: pageToLoad - 1,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };
    return this.matchService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(page = this.page, predicate?: string, ascending?: boolean): void {
    const queryParamsObj = {
      page,
      size: this.itemsPerPage,
      sort: this.getSortQueryParam(predicate, ascending),
    };

    this.router.navigate(['./'], {
      relativeTo: this.activatedRoute,
      queryParams: queryParamsObj,
    });
  }

  protected getSortQueryParam(predicate = this.predicate, ascending = this.ascending): string[] {
    const ascendingQueryParam = ascending ? ASC : DESC;
    if (predicate === '') {
      return [];
    } else {
      return [predicate + ',' + ascendingQueryParam];
    }
  }

  selectPet(petId: number, matchId: number): void {
    const text = matchId + ',' + petId;
    this.matchService.saveMatchAndPet(text).subscribe({
      next: () => this.router.navigateByUrl('/pet/' + petId + '/view'),
      error: () => console.log('error'),
    });
  }
}
