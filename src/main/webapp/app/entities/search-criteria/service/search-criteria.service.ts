import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISearchCriteria, NewSearchCriteria } from '../search-criteria.model';

export type PartialUpdateSearchCriteria = Partial<ISearchCriteria> & Pick<ISearchCriteria, 'id'>;

export type EntityResponseType = HttpResponse<ISearchCriteria>;
export type EntityArrayResponseType = HttpResponse<ISearchCriteria[]>;

@Injectable({ providedIn: 'root' })
export class SearchCriteriaService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/search-criteria');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(searchCriteria: NewSearchCriteria): Observable<EntityResponseType> {
    return this.http.post<ISearchCriteria>(this.resourceUrl, searchCriteria, { observe: 'response' });
  }

  update(searchCriteria: ISearchCriteria): Observable<EntityResponseType> {
    return this.http.put<ISearchCriteria>(`${this.resourceUrl}/${this.getSearchCriteriaIdentifier(searchCriteria)}`, searchCriteria, {
      observe: 'response',
    });
  }

  partialUpdate(searchCriteria: PartialUpdateSearchCriteria): Observable<EntityResponseType> {
    return this.http.patch<ISearchCriteria>(`${this.resourceUrl}/${this.getSearchCriteriaIdentifier(searchCriteria)}`, searchCriteria, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ISearchCriteria>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
  findByPetId(id: number): Observable<EntityResponseType> {
    return this.http.get<ISearchCriteria>(`${this.resourceUrl}/petId/${id}`, { observe: 'response' });
  }

  findByUser(): Observable<EntityResponseType> {
    return this.http.get<ISearchCriteria>(`${this.resourceUrl}/user`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISearchCriteria[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getSearchCriteriaIdentifier(searchCriteria: Pick<ISearchCriteria, 'id'>): number {
    return searchCriteria.id;
  }

  compareSearchCriteria(o1: Pick<ISearchCriteria, 'id'> | null, o2: Pick<ISearchCriteria, 'id'> | null): boolean {
    return o1 && o2 ? this.getSearchCriteriaIdentifier(o1) === this.getSearchCriteriaIdentifier(o2) : o1 === o2;
  }

  addSearchCriteriaToCollectionIfMissing<Type extends Pick<ISearchCriteria, 'id'>>(
    searchCriteriaCollection: Type[],
    ...searchCriteriaToCheck: (Type | null | undefined)[]
  ): Type[] {
    const searchCriteria: Type[] = searchCriteriaToCheck.filter(isPresent);
    if (searchCriteria.length > 0) {
      const searchCriteriaCollectionIdentifiers = searchCriteriaCollection.map(
        searchCriteriaItem => this.getSearchCriteriaIdentifier(searchCriteriaItem)!
      );
      const searchCriteriaToAdd = searchCriteria.filter(searchCriteriaItem => {
        const searchCriteriaIdentifier = this.getSearchCriteriaIdentifier(searchCriteriaItem);
        if (searchCriteriaCollectionIdentifiers.includes(searchCriteriaIdentifier)) {
          return false;
        }
        searchCriteriaCollectionIdentifiers.push(searchCriteriaIdentifier);
        return true;
      });
      return [...searchCriteriaToAdd, ...searchCriteriaCollection];
    }
    return searchCriteriaCollection;
  }
}
