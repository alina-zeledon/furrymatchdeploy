import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { finalize, map, takeUntil } from 'rxjs/operators';

import { SearchCriteriaFormService, SearchCriteriaFormGroup } from './search-criteria-form.service';
import { ISearchCriteria } from '../search-criteria.model';
import { SearchCriteriaService } from '../service/search-criteria.service';
import { IPet } from 'app/entities/pet/pet.model';
import { PetService } from 'app/entities/pet/service/pet.service';
import { Sex } from 'app/entities/enumerations/sex.model';
import { RegisterService } from '../../../account/register/register.service';
import { PetType } from '../../enumerations/pet-type.model';
import { IBreed } from '../../breed/breed.model';
import { IOwner } from '../../owner/owner.model';
import { BreedService } from '../../breed/service/breed.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'jhi-search-criteria-update',
  templateUrl: './search-criteria-update.component.html',
  // styleUrls: ['../../pet/update/pet-update.component.css'],
  styleUrls: ['../../../account/register/register.component.css'],
})
export class SearchCriteriaUpdateComponent implements OnInit {
  isSaving = false;
  searchCriteria: ISearchCriteria | null = null;
  sexValues = Object.keys(Sex);
  petTypeValues = Object.keys(PetType);
  breedsSharedCollection: IBreed[] = [];

  selectedProvinceId: number | null = null;
  selectedCantonId: number | null = null;
  provinces: any;
  cantones: any;
  districts: any;

  step: number;
  title: string;
  objective: any;

  petsSharedCollection: IPet[] = [];

  editForm: SearchCriteriaFormGroup = this.searchCriteriaFormService.createSearchCriteriaFormGroup();
  filteredBreedsSharedCollection: IBreed[] = [];
  destroy$: Subject<void> = new Subject();

  constructor(
    protected searchCriteriaService: SearchCriteriaService,
    protected searchCriteriaFormService: SearchCriteriaFormService,
    protected petService: PetService,
    protected activatedRoute: ActivatedRoute,
    protected registerService: RegisterService,
    protected breedService: BreedService,
    private router: Router
  ) {
    this.step = 1;
    this.title = 'Mi interés es:';
  }

  comparePet = (o1: IPet | null, o2: IPet | null): boolean => this.petService.comparePet(o1, o2);
  compareBreed = (o1: IBreed | null, o2: IBreed | null): boolean => this.breedService.compareBreed(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ searchCriteria }) => {
      this.searchCriteria = searchCriteria;
      if (searchCriteria) {
        this.updateForm(searchCriteria);
        this.getCantones(Number(searchCriteria.provice));
        this.getDistricts(Number(searchCriteria.provice), Number(searchCriteria.canton));
      }
      this.registerService.getProvinces().subscribe((response: any) => {
        const provincesArray = Object.entries(response).map(([id, name]) => ({ id, name }));
        this.provinces = provincesArray;
      });
      this.loadRelationshipsOptions();
      this.editForm
        .get('filterType')
        ?.valueChanges.pipe(takeUntil(this.destroy$))
        .subscribe(filterType => {
          if (filterType !== undefined) {
            this.updateFilteredBreeds(filterType);
          }
        });
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const searchCriteria = this.searchCriteriaFormService.getSearchCriteria(this.editForm);
    if (searchCriteria.id !== null) {
      searchCriteria.objective = this.objective;
      this.subscribeToSaveResponse(this.searchCriteriaService.update(searchCriteria));
    } else {
      searchCriteria.objective = this.objective;
      this.subscribeToSaveResponse(this.searchCriteriaService.create(searchCriteria));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISearchCriteria>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    // this.previousState();
    Swal.fire({
      title: 'Acción exitosa',
      text: 'Se guardaron correctamente los filtros.',
      icon: 'success',
      confirmButtonColor: '#3381f6',
      confirmButtonText: 'Cerrar',
    }).then((result: any) => {
      this.router.navigate(['/pet']);
    });
  }

  protected onSaveError(): void {
    Swal.fire({
      title: 'Error',
      text: 'Hubo un problema guardando los filtros; intenta nuevamente.',
      icon: 'error',
      confirmButtonColor: '#3381f6',
      confirmButtonText: 'Cerrar',
    });
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(searchCriteria: ISearchCriteria): void {
    this.searchCriteria = searchCriteria;
    this.searchCriteriaFormService.resetForm(this.editForm, searchCriteria);

    this.petsSharedCollection = this.petService.addPetToCollectionIfMissing<IPet>(this.petsSharedCollection, searchCriteria.pet);
  }

  protected loadRelationshipsOptions(): void {
    this.petService
      .query()
      .pipe(map((res: HttpResponse<IPet[]>) => res.body ?? []))
      .pipe(map((pets: IPet[]) => this.petService.addPetToCollectionIfMissing<IPet>(pets, this.searchCriteria?.pet)))
      .subscribe((pets: IPet[]) => (this.petsSharedCollection = pets));
    this.breedService
      .query()
      .pipe(map((res: HttpResponse<IBreed[]>) => res.body ?? []))
      .subscribe((breeds: IBreed[]) => {
        this.breedsSharedCollection = breeds;
        if (this.searchCriteria) {
          this.filteredBreedsSharedCollection = this.breedsSharedCollection.filter(
            breed => breed.breedType === this.searchCriteria?.filterType
          );
        }
      });
  }

  selected = '';

  update(e: any) {
    this.selected = e.target.value;
  }

  updateProvince(e: any) {
    const selectedProvinceId = parseInt(e.target.value);
    if (selectedProvinceId) {
      this.selectedProvinceId = selectedProvinceId;
      this.getCantones(selectedProvinceId);
    } else {
      this.selectedProvinceId = null;
      this.selectedCantonId = null;
      this.cantones = null;
      this.districts = null;
    }
  }

  updateCanton(e: any) {
    const selectedCantonId = parseInt(e.target.value);
    if (selectedCantonId) {
      this.selectedCantonId = selectedCantonId;
      this.getDistricts(this.selectedProvinceId, selectedCantonId);
    } else {
      this.selectedCantonId = null;
      this.districts = null;
    }
  }

  getCantones(provinceId: number) {
    this.registerService.getCantones(provinceId).subscribe((response: any) => {
      const cantonesArray = Object.entries(response).map(([id, name]) => ({ id: parseInt(id), name }));
      this.cantones = cantonesArray;
    });
  }

  getDistricts(provinceId: number | null, cantonId: number) {
    this.registerService.getDistricts(provinceId, cantonId).subscribe((response: any) => {
      const districtsArray = Object.entries(response).map(([id, name]) => ({ id: parseInt(id), name }));
      this.districts = districtsArray;
    });
  }

  updateFilteredBreeds(filterType: any): void {
    if (filterType) {
      console.log('El valor de petType no está vacío:', filterType.value);
      this.filteredBreedsSharedCollection = this.breedsSharedCollection.filter(breed => breed.breedType === filterType.value);
    } else {
      console.log('El valor de petType está vacío');
      this.filteredBreedsSharedCollection = [];
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  selectObjective(obj: number): void {
    if (obj === 1) {
      this.objective = 1;
      this.title = 'Quiero cruzar a mi mascota y busco una con estas características y condiciones:';
      if (this.searchCriteria) {
        if (this.objective != this.searchCriteria.objective) {
          this.editForm.controls['filterType'].reset();
          this.editForm.controls['breed'].reset();
          this.editForm.controls['sex'].reset();
          this.editForm.controls['provice'].reset();
          this.editForm.controls['canton'].reset();
          this.editForm.controls['district'].reset();
          this.editForm.controls['tradePups'].reset();
          this.editForm.controls['tradeMoney'].reset();
          this.editForm.controls['pedigree'].reset();
        }
      }
      this.step = 2;
    } else {
      this.objective = 2;
      this.title = 'Busco amistades para mi mascota con estas características:';
      if (this.searchCriteria) {
        if (this.objective != this.searchCriteria.objective) {
          this.editForm.controls['filterType'].reset();
          this.editForm.controls['breed'].reset();
          this.editForm.controls['sex'].reset();
          this.editForm.controls['provice'].reset();
          this.editForm.controls['canton'].reset();
          this.editForm.controls['district'].reset();
          this.editForm.controls['tradePups'].reset();
          this.editForm.controls['tradeMoney'].reset();
          this.editForm.controls['pedigree'].reset();
        }
      }
      this.step = 2;
    }
  }
}
