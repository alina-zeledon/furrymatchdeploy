import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter, forkJoin, Observable } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { PetService } from '../../pet/service/pet.service';
import { IContract } from '../contract.model';
import { LikeeService } from '../../likee/service/likee.service';
import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { EntityArrayResponseType, ContractService } from '../service/contract.service';
import { IPet } from '../../pet/pet.model';
import { IPhoto } from '../../photo/photo.model';
import { IMatch } from '../../match/match.model';
import { PhotoService } from '../../photo/service/photo.service';
import { MatchService } from '../../match/service/match.service';
import { AccountService } from '../../../core/auth/account.service';

interface PetEntry {
  pet: IPet;
  photo: IPhoto | undefined;
  contract: IContract | null;
  matchId: number;
  ownerId: number;
  otherNotes?: string;
}
@Component({
  selector: 'jhi-contract',
  templateUrl: './contract.component.html',
  styleUrls: ['./contract.component.css'],
})
export class ContractComponent implements OnInit {
  matches?: IMatch[];
  contracts?: IContract[];
  isLoading = false;
  currentPetId: number | null = null;
  // petData: Map<number, { pet: IPet; photo: IPhoto | undefined }> = new Map();
  petData: Map<number, { pet: IPet; photo: IPhoto | undefined; contract: IContract | null; matchId: number; ownerId: number }> = new Map();

  predicate = 'id';
  ascending = true;

  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;
  matchedPetsAndContracts: any[] | null = [];
  currentUserId: number | undefined;
  constructor(
    protected contractService: ContractService,
    protected activatedRoute: ActivatedRoute,
    protected modalService: NgbModal,
    protected petService: PetService,
    protected likeeService: LikeeService,
    protected photoService: PhotoService,
    private changeDetector: ChangeDetectorRef,
    protected matchService: MatchService,
    private accountService: AccountService,
    private router: Router
  ) {}

  trackId = (_index: number, item: IContract): number => this.contractService.getContractIdentifier(item);

  ngOnInit(): void {
    this.isLoading = true;
    this.loadSearchCriteriaForCurrentUser();

    // Listen for route changes
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.loadMatchedPetsAndContracts();
    });
  }

  loadSearchCriteriaForCurrentUser(): void {
    this.matchService.getCurrentUserPetId().subscribe(response => {
      this.currentPetId = response.body;
      console.log('PET ID: ' + this.currentPetId);
      this.loadMatchedPetsAndContracts();
    });
  }
  getCurrentOwnerId(): number | undefined {
    if (this.matchedPetsAndContracts) {
      for (const match of this.matchedPetsAndContracts) {
        const pet = match.find((item: { id: number | null }) => item.id === this.currentPetId);
        if (pet) {
          return pet.owner.id;
        }
      }
    }
    return undefined;
  }
  loadMatchedPetsAndContracts(): void {
    this.isLoading = true;
    this.contractService.getMatchedPetsAndContracts(this.currentPetId).subscribe(response => {
      this.matchedPetsAndContracts = response.body || [];
      console.log('QUERY RESULTS!!!' + JSON.stringify(this.matchedPetsAndContracts, null, 2));
      this.loadPetPhotos(); // Call loadPetPhotos() after populating matchedPetsAndContracts
    });
    console.log('This matched pet contracts ' + this.matchedPetsAndContracts);
  }
  getOtherPetIds(): number[] {
    if (!this.currentPetId || !this.matchedPetsAndContracts) {
      return [];
    }

    const currentPetId = this.currentPetId;
    const matchPetIds: number[] = [];

    this.matchedPetsAndContracts.forEach(match => {
      const firstPet = match[1];
      const secondPet = match[2];

      if (firstPet.id === currentPetId) {
        matchPetIds.push(secondPet.id);
      } else {
        matchPetIds.push(firstPet.id);
      }
    });

    return matchPetIds;
  }

  loadPetPhotos(): void {
    const otherPetIds = this.getOtherPetIds();
    console.log('OTHER PET IDS' + otherPetIds);
    const photoRequests: Observable<EntityArrayResponseType>[] = otherPetIds.map(petId => this.photoService.findAllPhotosByPetID(petId));
    console.log('PHOTO REQUESTS' + JSON.stringify(photoRequests, null, 2));

    // Save the other pets in this.petData
    this.matchedPetsAndContracts?.forEach(match => {
      const matchId = match[0].id;
      const firstPet = match[1];
      const secondPet = match[2];
      const contract = match[0].contract;
      const otherPet = firstPet.id === this.currentPetId ? secondPet : firstPet;
      const ownerId = firstPet.id === this.currentPetId ? secondPet.owner.id : firstPet.owner.id;

      this.petData.set(otherPet.id, { pet: otherPet, photo: undefined, contract: contract, matchId: matchId, ownerId: ownerId });
    });
    forkJoin(photoRequests).subscribe(photos => {
      photos.forEach((photoResponse, index) => {
        console.log('Photo Response:', photoResponse);
        const photo = photoResponse.body?.length ? photoResponse.body[0] : undefined;
        if (photo) {
          const petId = otherPetIds[index];
          const petEntry = this.petData.get(petId);
          console.log('Pet ID:', petId, 'Pet Entry:', petEntry);
          if (petEntry) {
            this.petData.set(petId, { ...petEntry, photo: photo });
            this.changeDetector.detectChanges(); // Add this line
          }
        }
      });

      console.log('Pet Data:', this.petData);
    });
    this.isLoading = false;
  }
  isCurrentUserCreatingContract(petEntry: PetEntry): boolean {
    if (!petEntry.contract || !petEntry.contract.otherNotes) {
      return false;
    }
    const otherNotesParts = petEntry.contract.otherNotes.split(';');
    const creatorId = parseInt(otherNotesParts[1], 10);
    const contractStatus = parseInt(otherNotesParts[2], 10);
    this.currentUserId = this.getCurrentOwnerId();
    // console.log('owner id', this.currentUserId)
    return this.currentUserId === creatorId && contractStatus === 1;
  }
  isCurrentUserSendingContract(petEntry: PetEntry): boolean {
    if (!petEntry.contract || !petEntry.contract.otherNotes) {
      return false;
    }
    const otherNotesParts = petEntry.contract.otherNotes.split(';');
    const creatorId = parseInt(otherNotesParts[1], 10);
    const contractStatus = parseInt(otherNotesParts[2], 10);
    this.currentUserId = this.getCurrentOwnerId();
    // console.log('owner id', this.currentUserId)
    return this.currentUserId === creatorId && contractStatus === 2;
  }

  isOtherUserSendingContract(petEntry: PetEntry): boolean {
    if (!petEntry.contract || !petEntry.contract.otherNotes) {
      return false;
    }
    const otherNotesParts = petEntry.contract.otherNotes.split(';');
    const creatorId = parseInt(otherNotesParts[1], 10);
    const contractStatus = parseInt(otherNotesParts[2], 10);
    this.currentUserId = this.getCurrentOwnerId();
    // console.log('owner id', this.currentUserId)
    return this.currentUserId !== creatorId && contractStatus === 2;
  }
  isOtherUserCreatingContract(petEntry: PetEntry): boolean {
    if (!petEntry.contract || !petEntry.contract.otherNotes) {
      return false;
    }
    const otherNotesParts = petEntry.contract.otherNotes.split(';');
    const creatorId = parseInt(otherNotesParts[1], 10);
    const contractStatus = parseInt(otherNotesParts[2], 10);
    this.currentUserId = this.getCurrentOwnerId();
    // console.log('owner id', this.currentUserId)
    return this.currentUserId !== creatorId && contractStatus === 1;
  }

  getPetDataValues(): PetEntry[] {
    //console.log('PET DATA VALUES', this.petData.values());
    return Array.from(this.petData.values());
  }

  saveMatchPet(matchId: number, ownerId: number | undefined, petId: number): void {
    const matchPet = matchId + ',' + ownerId + '-' + petId;
    this.contractService.saveMatchPet(matchPet).subscribe({
      next: () => this.router.navigateByUrl('/contract/new'),
      // next: () => this.router.navigateByUrl('/search-criteria/new'),
      error: () => console.log('error'),
    });
  }

  sendEmail(matchId: number, ownerId: number | undefined, petId: number, contractId: number | undefined): void {
    const matchPet = matchId + ',' + ownerId + '-' + petId;
    this.contractService.saveMatchPet(matchPet).subscribe({
      next: () => {
        this.contractService.sendEmail(contractId).subscribe({
          next: () => {
            this.router.navigateByUrl('/contract');
            this.loadMatchedPetsAndContracts();
          },
          error: () => console.log('error'),
        });
      },
      error: () => console.log('error'),
    });
  }
}
