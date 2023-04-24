import { Component, OnInit } from '@angular/core';
import {
  SearchCriteriaService,
  EntityResponseType as SearchCriteriaEntityResponseType,
} from '../../search-criteria/service/search-criteria.service';
import { AccountService } from 'app/core/auth/account.service';
import { LikeType } from '../../enumerations/like-type.model';
import { LikeeService } from '../../likee/service/likee.service';
import { MatchService } from '../../match/service/match.service';
import { PetService, EntityArrayResponseType as PetEntityArrayResponseType } from '../service/pet.service';
import { IPet } from '../pet.model';
import { ISearchCriteria } from '../../search-criteria/search-criteria.model';
import { NavigationEnd, Router } from '@angular/router';
import Swal from 'sweetalert2';
import { NewLikee } from '../../likee/likee.model';
import { NewMatch } from '../../match/match.model';
import dayjs from 'dayjs/esm';

@Component({
  selector: 'jhi-search-match',
  templateUrl: './search-match.component.html',
  styleUrls: ['./search-match.component.scss'],
})
export class SearchMatchComponent implements OnInit {
  pets: IPet[] = [];
  currentPetId: number | null | undefined;
  currentPetIndex = 0;
  filters: ISearchCriteria | null = null;
  newCurrentPetIndex?: number;
  noMorePets = false;

  constructor(
    private searchCriteriaService: SearchCriteriaService,
    private accountService: AccountService,
    private petService: PetService,
    private likeService: LikeeService,
    private matchService: MatchService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.loadPets();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.loadPets();
      }
    });
  }

  loadPets(): void {
    this.petService.search().subscribe(
      (res: PetEntityArrayResponseType) => {
        this.pets = res.body || [];
        console.log('All Pets ', JSON.stringify(this.pets, null, 2));
        this.currentPetIndex = 0;
        this.findPetInSession();
        //this.loadSearchCriteriaForCurrentUser();
      },
      error => {
        console.error('Error fetching pets based on search criteria:', error);
      }
    );
  }

  findPetInSession(): void {
    this.petService.getPetInSession().subscribe(response => {
      this.currentPetId = response.body;
      //console.log('PET ID: ' + this.currentPetId);
      if (this.currentPetId == null) {
        console.error('The currentPetId is null');
      } else {
        //Need to pull search criteria to display owner's objective
        this.searchCriteriaService.findByPetId(this.currentPetId).subscribe(
          (res: SearchCriteriaEntityResponseType) => {
            this.filters = res.body;
            console.log('User Search Criteria From DB: ' + JSON.stringify(this.filters, null, 2));
            console.log('Pet Displayed', JSON.stringify(this.pets[this.currentPetIndex], null, 2));
            // Calls the search function with the searchCriteria object
            //this.loadPets(searchCriteria);
          },
          error => {
            console.error('Error fetching search criteria for user:', error);
          }
        );
      }
    });
  }
  /*
  loadSearchCriteriaForCurrentUser(): void {
    this.accountService.identity().subscribe(user => {
      this.petService.getPetInSession().subscribe(response => {
        this.currentPetId = response.body;
        console.log('PET ID: ' + this.currentPetId);

        if (this.currentPetId !== null) {
          this.searchCriteriaService.findByPetId(this.currentPetId).subscribe(
            (res: SearchCriteriaEntityResponseType) => {
              this.filters = res.body;
              console.log('User Search Criteria From DB: ' + JSON.stringify(this.filters, null, 2));
              console.log('Pet Displayed', JSON.stringify(this.pets[this.currentPetIndex], null, 2));
              // Calls the search function with the searchCriteria object
              //this.loadPets(searchCriteria);
            },
            error => {
              console.error('Error fetching search criteria for user:', error);
            }
          );
        } else {
          console.error('The currentPetId is null. Unable to fetch search criteria for user.');
        }
      });
    });
  }
*/
  /*
  loadSearchCriteriaForCurrentUser(): void {
    this.accountService.identity().subscribe(user => {
      if (user) {
        // imageUrl is pet in session
        this.currentPetId = parseInt(user.imageUrl);
        console.log('PET ID: ' + this.currentPetId );
        this.searchCriteriaService.find(this.currentPetId ).subscribe(
          (res: SearchCriteriaEntityResponseType) => {
            this.filters = res.body;
            console.log('User Search Criteria From DB: ' + JSON.stringify(this.filters, null, 2));
            console.log('Pet Displayed', JSON.stringify(this.pets[this.currentPetIndex], null, 2));
            // Calls the search function with the searchCriteria object
            //this.loadPets(searchCriteria);
          },
          error => {
            console.error('Error fetching search criteria for user:', error);
          }
        );
      }
    });
  }*/
  private moveToNextPet(): void {
    if (this.currentPetIndex < this.pets.length - 1) {
      this.currentPetIndex++;
    } else {
      this.newCurrentPetIndex = undefined;
      this.noMorePets = true;
    }
  }

  skipPet(): void {
    const dislike: NewLikee = {
      id: null,
      likeState: LikeType.Dislike,
      firstPet: { id: this.currentPetId as number },
      secondPet: { id: this.pets[this.currentPetIndex].id },
    };
    console.log('Disliking pet', dislike);
    this.likeService.create(dislike).subscribe(
      () => {
        console.log('Dislike created successfully');
        this.newCurrentPetIndex = this.currentPetIndex + 1;
        this.moveToNextPet();
      },
      error => {
        console.error('Error creating dislike:', error);
      }
    );
  }
  like(): void {
    const like: NewLikee = {
      id: null,
      likeState: LikeType.Like,
      firstPet: { id: this.currentPetId as number }, // Create an object with only the id property
      secondPet: { id: this.pets[this.currentPetIndex].id },
    };
    console.log('First PET ID' + this.currentPetId);
    console.log('Second PET ID' + this.pets[this.currentPetIndex].id);
    this.likeService.saveLikeIsMatch(like).subscribe(response => {
      if (response.body) {
        console.log('Match ID:', response.body);
        const matchId = response.body;
        console.log('Match found!');
        this.router.navigate([`/match/${matchId}/view`]);
      } else {
        console.log('No match found.');
      }
      this.newCurrentPetIndex = this.currentPetIndex + 1;
      this.moveToNextPet();
    });
  }

  /*
  like(): void {
    const like: NewLikee = {
      id: null,
      likeState: LikeType.Like,
      firstPet: { id: this.currentPetId as number }, // Create an object with only the id property
      secondPet: { id: this.pets[this.currentPetIndex].id },
    };
    console.log('First PET ID' + this.currentPetId);
    console.log('Second PET ID' + this.pets[this.currentPetIndex].id);
    this.likeService.create(like).subscribe(() => {
      this.likeService.isMatch(this.currentPetId as number, this.pets[this.currentPetIndex].id).subscribe(response => {
        if (response.body) {
          console.log('Match found!');
          console.log('First PET ID' + this.currentPetId);
          console.log('Second PET ID' + this.pets[this.currentPetIndex].id);
          //  this.router.navigate(['/match/id/view']);
          /*const newMatch: NewMatch = {
            id: null,
            notifyMatch: true,
            dateMatch: dayjs(),
            contract: null,
            firstLiked: { id: this.pets[this.currentPetIndex].id },
            secondLiked: { id: this.currentPetId as number },
          };
          console.log('Match object ' + JSON.stringify(newMatch, null, 2));
            this.matchService.create(newMatch).subscribe(() => {
            Swal.fire({
              title: 'Success',
              text: 'Es un match!',
              icon: 'success',
              confirmButtonColor: '#3381f6',
              confirmButtonText: 'Cerrar',
            });
          });
        } else {
          console.log('No match found.');
        }
        this.newCurrentPetIndex = this.currentPetIndex + 1;
        this.moveToNextPet();
      });
    });
  }*/
}
