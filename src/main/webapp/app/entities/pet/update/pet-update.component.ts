import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, Observable, Subject, throwError } from 'rxjs';
import { finalize, map, takeUntil, tap } from 'rxjs/operators';
import { catchError } from 'rxjs/operators';

import { PetFormService, PetFormGroup } from './pet-form.service';
import { IPet } from '../pet.model';
import { PetService } from '../service/pet.service';
import { PhotoService } from '../../photo/service/photo.service';
import { IOwner } from 'app/entities/owner/owner.model';
import { OwnerService } from 'app/entities/owner/service/owner.service';
import { IBreed } from 'app/entities/breed/breed.model';
import { BreedService } from 'app/entities/breed/service/breed.service';
import { PetType } from 'app/entities/enumerations/pet-type.model';
import { Sex } from 'app/entities/enumerations/sex.model';
import { IPhoto, NewPhoto } from '../../photo/photo.model';
import Swal from 'sweetalert2';
import { FormArray } from '@angular/forms';
import dayjs from 'dayjs/esm';

@Component({
  selector: 'jhi-pet-update',
  templateUrl: './pet-update.component.html',
  styleUrls: ['pet-update.component.css'],
})
export class PetUpdateComponent implements OnInit {
  isSaving = false;
  pet: IPet | null = null;
  petTypeValues = Object.keys(PetType);
  sexValues = Object.keys(Sex);

  maxPhotos = 5;
  update = false;

  activeImageIndex = 0;

  ownersSharedCollection: IOwner[] = [];
  breedsSharedCollection: IBreed[] = [];

  petPhotoData: { file: File; photoObj: IPhoto; customId: string }[] = [];

  editForm: PetFormGroup = this.petFormService.createPetFormGroup();

  pets: IPet[] = [];
  petFiles: File[] = [];
  petUpdateFiles: File[] = [];
  petPhotos: string[][] = Array(5).fill([]);

  filteredBreedsSharedCollection: IBreed[] = [];
  destroy$: Subject<void> = new Subject();

  constructor(
    protected petService: PetService,
    protected petFormService: PetFormService,
    protected ownerService: OwnerService,
    protected breedService: BreedService,
    protected photoService: PhotoService,
    protected activatedRoute: ActivatedRoute,
    private router: Router
  ) {}

  compareOwner = (o1: IOwner | null, o2: IOwner | null): boolean => this.ownerService.compareOwner(o1, o2);

  compareBreed = (o1: IBreed | null, o2: IBreed | null): boolean => this.breedService.compareBreed(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ pet }) => {
      this.pet = pet;
      if (pet) {
        this.update = true;
        this.updateForm(pet);
        this.loadExistingPetPhotos();
      }

      this.loadRelationshipsOptions();
    });

    this.editForm
      .get('petType')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe(petType => {
        if (petType !== undefined) {
          this.updateFilteredBreeds(petType);
        }
      });

    this.loadPets();
  }

  loadExistingPetPhotos(): void {
    if (this.pet && this.pet.id) {
      this.photoService.findAllPhotosByPetID(this.pet.id).subscribe((response: HttpResponse<any[]>) => {
        const photos = response.body;

        photos?.forEach((photo: any, index: number) => {
          if (photo.photoUrl) {
            this.photoService.downloadImage(photo.photoUrl).subscribe((response: Blob) => {
              const blob = new Blob([response], { type: 'image/jpeg' });
              const file = new File([blob], `pet_photo_${index}.jpeg`, { type: 'image/jpeg' });
              const customId = 'custom_' + index;
              const photoObj: IPhoto = {
                id: photo.id,
                uploadDate: null,
                photoUrl: photo.photoUrl,
                pet: null,
                customId,
              };
              const parentObj = { file, photoObj, customId };
              this.petPhotoData.push(parentObj);
              this.petFiles.push(file);
            });
          }
        });
      });
    }
  }

  loadPets(): void {
    this.petService.query().subscribe((res: HttpResponse<IPet[]>) => {
      this.pets = res.body || [];
    });
  }

  onSelect(event?: any): void {
    const maxPhotosReached = this.update ? this.petUpdateFiles.length >= this.maxPhotos : this.petFiles.length >= this.maxPhotos;

    if (maxPhotosReached) {
      Swal.fire({
        title: 'Error',
        text: 'Solo se permiten hasta 5 fotos por mascota',
        icon: 'error',
        confirmButtonColor: '#3381f6',
        confirmButtonText: 'Cerrar',
      });
      return;
    }

    const filesArray = this.update ? this.petUpdateFiles : this.petFiles;
    filesArray.push(...event.addedFiles);
  }

  onRemove(event?: any): void {
    if (this.update) {
      this.petUpdateFiles.splice(this.petUpdateFiles.indexOf(event), 1);
    } else {
      this.petFiles.splice(this.petFiles.indexOf(event), 1);
    }
  }

  onRemoveFromDB(parentObj: { file: File }): void {
    console.log('Removing:', parentObj.file);
    const fileToRemove = parentObj.file;

    const photoToRemoveIndex = this.petPhotoData.findIndex(photo => photo.file === fileToRemove);

    if (photoToRemoveIndex >= 0) {
      const removedPhoto = this.petPhotoData[photoToRemoveIndex];

      if (removedPhoto) {
        const removedPhotoUrl = removedPhoto.photoObj.photoUrl;
        console.log('removedPhotoUrl:', removedPhotoUrl);

        if (removedPhotoUrl) {
          const removedPhotoId = removedPhoto.photoObj.id;
          console.log('removedPhotoId:', removedPhotoId);

          Swal.fire({
            title: '¿Estás seguro?',
            text: 'Esta acción eliminará la imagen permanentemente.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar',
          }).then(result => {
            if (result.value) {
              this.petFiles.splice(photoToRemoveIndex, 1);
              this.petPhotoData.splice(photoToRemoveIndex, 1);

              // Si se trata de una foto existente, elimínela de la base de datos
              if (removedPhotoId) {
                this.deletePhotoFromDatabase(removedPhotoId);
              }

              Swal.fire({
                title: 'Eliminada',
                text: 'La imagen ha sido eliminada.',
                icon: 'success',
                confirmButtonColor: '#3085d6',
                confirmButtonText: 'Aceptar',
              });
            }
          });
        }
      }
    }
  }

  deletePhotoFromDatabase(photoId: number): void {
    this.photoService.delete(photoId).subscribe(() => {
      console.log('Foto con id: ' + photoId + 'fue eliminada de la base de datos');
    });
    Swal.fire({
      title: 'Fotografía borrada exitosamente',
      text: 'Continuá actualizando los datos.',
      icon: 'success',
      confirmButtonColor: '#3381f6',
      confirmButtonText: 'Cerrar',
    });
  }

  onUpload(): void {
    const files = this.update ? this.petUpdateFiles : this.petFiles;

    console.log(
      'Fotos nuevas:',
      this.petFiles.map(file => file.name)
    );
    console.log(
      'Fotos update:',
      this.petUpdateFiles.map(file => file.name)
    );

    if (!files.length) {
      Swal.fire({
        title: 'Error',
        text: 'Debés primero arrastrar o seleccionar una imagen.',
        icon: 'error',
        confirmButtonColor: '#3381f6',
        confirmButtonText: 'Cerrar',
      });
      return;
    }

    if (this.update) {
      if (this.pet && this.pet.id) {
        this.photoService.findAllPhotosByPetID(this.pet.id).subscribe((response: HttpResponse<any[]>) => {
          const existingPhotos = response.body;
          const totalPhotos = (existingPhotos?.length || 0) + files.length;

          if (totalPhotos > 5) {
            Swal.fire({
              title: 'Error',
              text: 'Solo se permiten hasta 5 fotos por mascota',
              icon: 'error',
              confirmButtonColor: '#3381f6',
              confirmButtonText: 'Cerrar',
            });
            return;
          }

          const uploadObservables = files.map(file_data => {
            const data = new FormData();
            data.append('file', file_data);
            data.append('upload_preset', 'furry_match');
            data.append('cloud_name', 'alocortesu');

            return this.petService.uploadImage(data);
          });

          forkJoin(uploadObservables).subscribe(responses => {
            responses.forEach(response => {
              const secureUrl = response.secure_url;
              this.petPhotos.push(secureUrl);
            });

            Swal.fire({
              title: 'Fotografías agregadas',
              text: 'Continuá registrando los datos.',
              icon: 'success',
              confirmButtonColor: '#3381f6',
              confirmButtonText: 'Cerrar',
            });

            const photos = this.createUpdatePhotosArray();
            console.log('Objeto photos:', photos);

            photos.forEach(photo => {
              console.log('Foto a enviar al servicio: ', photos);

              this.photoService.create(photo).subscribe(() => {
                console.log('Foto ' + photo.photoUrl + ' fue ingresada a la base de datos');
              });
            });
          });
          this.uploadNewPhotos(files);
        });
      }
    } else {
      // Lógica original para fotos nuevas
      files.forEach(file_data => {
        const data = new FormData();
        data.append('file', file_data);
        data.append('upload_preset', 'furry_match');
        data.append('cloud_name', 'alocortesu');

        this.petService.uploadImage(data).subscribe(response => {
          if (response) {
            const secureUrl = response.secure_url;
            this.petPhotos.push(secureUrl);

            Swal.fire({
              title: 'Fotografías agregadas',
              text: 'Continuá registrando los datos.',
              icon: 'success',
              confirmButtonColor: '#3381f6',
              confirmButtonText: 'Cerrar',
            });
          }
        });
        console.log(
          'Fotos en onUpload',
          files.map(file => file.name)
        );
      });
    }
  }

  private uploadNewPhotos(files: File[]): void {
    const uploadObservables = files.map(file_data => {
      const data = new FormData();
      data.append('file', file_data);
      data.append('upload_preset', 'furry_match');
      data.append('cloud_name', 'alocortesu');

      return this.petService.uploadImage(data);
    });

    forkJoin(uploadObservables).subscribe(responses => {
      responses.forEach(response => {
        const secureUrl = response.secure_url;
        this.petPhotos.push(secureUrl);
      });

      Swal.fire({
        title: 'Fotografías agregadas',
        text: 'Continuá registrando los datos.',
        icon: 'success',
        confirmButtonColor: '#3381f6',
        confirmButtonText: 'Cerrar',
      });

      const photos = this.createUpdatePhotosArray();
      console.log('Objeto photos:', photos);

      photos.forEach(photo => {
        console.log('Foto a enviar al servicio: ', photo);

        this.photoService.create(photo).subscribe(() => {
          console.log('Foto ' + photo.photoUrl + ' fue ingresada a la base de datos');
        });
      });
    });
  }

  previousState(): void {
    window.history.back();
  }

  updateFilteredBreeds(petType: PetType | null): void {
    if (petType) {
      console.log('El valor de petType no está vacío:', petType);
      this.filteredBreedsSharedCollection = this.breedsSharedCollection.filter(breed => breed.breedType === petType);
    } else {
      console.log('El valor de petType está vacío');
      this.filteredBreedsSharedCollection = [];
    }
    console.log('Filtered breeds:', this.filteredBreedsSharedCollection);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  photos: FormArray = new FormArray<any>([]);

  save(): void {
    console.log('Save function called');
    this.isSaving = true;
    const pet = this.petFormService.getPet(this.editForm);
    console.log('Pet object:', pet);
    pet.photos = this.update ? [] : this.createPhotosArray();
    console.log(pet.photos.length);

    if (!this.update && pet.photos.length === 0) {
      Swal.fire({
        title: 'Error',
        text: 'Debés subir al menos una foto de la mascota antes de continuar.',
        icon: 'error',
        confirmButtonColor: '#3381f6',
        confirmButtonText: 'Cerrar',
      });
      this.isSaving = false;
      return;
    }

    if (pet.id !== null) {
      this.subscribeToSaveResponse(this.petService.update(pet), 1);
    } else {
      this.subscribeToSaveResponse(this.petService.create(pet), 2);
    }
  }

  private createPhotosArray(): IPhoto[] {
    const photos: IPhoto[] = [];

    const flattenedPhotos = this.petPhotos.reduce((acc, val) => acc.concat(val), []);
    let counter = 0;
    for (const photoUrl of flattenedPhotos) {
      const photo: IPhoto = {
        id: counter,
        uploadDate: null,
        photoUrl: photoUrl,
        pet: this.update ? this.pet : null,
      };
      photos.push(photo);
      counter++;
    }
    return photos;
  }

  private createUpdatePhotosArray(): NewPhoto[] {
    const photos: NewPhoto[] = [];

    const flattenedPhotos = this.petPhotos.reduce((acc, val) => acc.concat(val), []);

    for (const photoUrl of flattenedPhotos) {
      const photo: NewPhoto = {
        id: null,
        uploadDate: dayjs(),
        photoUrl: photoUrl,
        pet: this.pet,
      };
      photos.push(photo);
    }
    return photos;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPet>>, num: number): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(num),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(num: number): void {
    if (num == 2) {
      this.router.navigate(['/search-criteria/new']);
    } else {
      this.router.navigate(['/pet/' + this.pet?.id + '/view']);
    }
    // this.previousState();
    // console.log('exito');
  }

  protected onSaveError(): void {
    // Api for inheritance.
    console.log('error');
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(pet: IPet): void {
    this.pet = pet;
    this.petFormService.resetForm(this.editForm, pet);

    this.ownersSharedCollection = this.ownerService.addOwnerToCollectionIfMissing<IOwner>(this.ownersSharedCollection, pet.owner);
    this.filteredBreedsSharedCollection = this.breedService.addBreedToCollectionIfMissing<IBreed>(this.breedsSharedCollection, pet.breed);
  }

  protected loadRelationshipsOptions(): void {
    this.ownerService
      .query()
      .pipe(map((res: HttpResponse<IOwner[]>) => res.body ?? []))
      .pipe(map((owners: IOwner[]) => this.ownerService.addOwnerToCollectionIfMissing<IOwner>(owners, this.pet?.owner)))
      .subscribe((owners: IOwner[]) => (this.ownersSharedCollection = owners));

    this.breedService
      .query()
      .pipe(map((res: HttpResponse<IBreed[]>) => res.body ?? []))
      .pipe(tap((breeds: IBreed[]) => console.log('API response:', breeds)))
      .pipe(map((breeds: IBreed[]) => this.breedService.addBreedToCollectionIfMissing<IBreed>(breeds, this.pet?.breed)))
      .subscribe((breeds: IBreed[]) => (this.breedsSharedCollection = breeds));
    console.log(
      'Dog breeds:',
      this.breedsSharedCollection.filter(breed => breed.breedType === 'Perro')
    );
    console.log(
      'Cat breeds:',
      this.breedsSharedCollection.filter(breed => breed.breedType === 'Gato')
    );
    console.log(this.pet?.breed);
    console.log('Breeds loaded:', this.breedsSharedCollection);
  }

  delete(): void {
    Swal.fire({
      title: '¿Deseás eliminar a tu mascota?',
      text: 'Si hacés click en el botón de Sí perderás toda la información del mismo.',
      showCancelButton: false,
      showConfirmButton: true,
      showDenyButton: true,
      confirmButtonText: 'Sí',
      denyButtonText: 'No',
      icon: 'success',
      confirmButtonColor: '#3381f6',
      denyButtonColor: '#3381f6',
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.petService.delete(this.pet?.id).subscribe(() => {
          this.router.navigate(['/pet']);
          console.log('borrado');
        });
      } else if (result.isDenied) {
        /*Swal.fire({
          title: 'Error',
          text: 'No se pudo borrar tu mascota. Por favor intentá nuevamente.',
          icon: 'error',
          confirmButtonColor: '#3381f6',
          confirmButtonText: 'Cerrar',
        });*/
      }
    });
  }
}
