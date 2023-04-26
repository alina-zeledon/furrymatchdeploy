import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IPet, NewPet } from '../pet.model';
import { ISearchCriteria, NewSearchCriteria } from '../../search-criteria/search-criteria.model';
import { IPhoto } from '../../photo/photo.model';

export type PartialUpdatePet = Partial<IPet> & Pick<IPet, 'id'>;

export type EntityResponseType = HttpResponse<IPet>;
export type EntityArrayResponseType = HttpResponse<IPet[]>;

@Injectable({ providedIn: 'root' })
export class PetService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/pets');
  protected accountUrl = this.applicationConfigService.getEndpointFor('api/account/selectedPet');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  uploadImage(vals: any): Observable<any> {
    let data = vals;
    return this.http.post('https://api.cloudinary.com/v1_1/alocortesu/image/upload', data);
  }

  create(pet: NewPet): Observable<EntityResponseType> {
    return this.http.post<IPet>(this.resourceUrl, pet, { observe: 'response' });
  }

  update(pet: IPet): Observable<EntityResponseType> {
    return this.http.put<IPet>(`${this.resourceUrl}/${this.getPetIdentifier(pet)}`, pet, { observe: 'response' });
  }

  partialUpdate(pet: PartialUpdatePet): Observable<EntityResponseType> {
    return this.http.patch<IPet>(`${this.resourceUrl}/${this.getPetIdentifier(pet)}`, pet, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IPet>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    console.log('Options: ' + options);
    return this.http.get<IPet[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number | undefined): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getPetIdentifier(pet: Pick<IPet, 'id'>): number {
    return pet.id;
  }

  comparePet(o1: Pick<IPet, 'id'> | null, o2: Pick<IPet, 'id'> | null): boolean {
    return o1 && o2 ? this.getPetIdentifier(o1) === this.getPetIdentifier(o2) : o1 === o2;
  }

  addPetToCollectionIfMissing<Type extends Pick<IPet, 'id'>>(petCollection: Type[], ...petsToCheck: (Type | null | undefined)[]): Type[] {
    const pets: Type[] = petsToCheck.filter(isPresent);
    if (pets.length > 0) {
      const petCollectionIdentifiers = petCollection.map(petItem => this.getPetIdentifier(petItem)!);
      const petsToAdd = pets.filter(petItem => {
        const petIdentifier = this.getPetIdentifier(petItem);
        if (petCollectionIdentifiers.includes(petIdentifier)) {
          return false;
        }
        petCollectionIdentifiers.push(petIdentifier);
        return true;
      });
      return [...petsToAdd, ...petCollection];
    }
    return petCollection;
  }

  selectedPet(id: number): Observable<HttpResponse<{}>> {
    return this.http.post<HttpResponse<{}>>(`${this.accountUrl}/${id}`, {
      observe: 'response',
    });
  }
  search(): Observable<HttpResponse<IPet[]>> {
    const searchUrl = `${this.resourceUrl}/search`;
    return this.http.get<IPet[]>(searchUrl, { observe: 'response' });
  }

  getPetInSession(): Observable<HttpResponse<number>> {
    return this.http.get<number>(`${this.resourceUrl}/current`, { observe: 'response' });
  }
}
