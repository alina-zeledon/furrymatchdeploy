import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ILikee, NewLikee } from '../likee.model';

export type PartialUpdateLikee = Partial<ILikee> & Pick<ILikee, 'id'>;

export type EntityResponseType = HttpResponse<ILikee>;
export type EntityArrayResponseType = HttpResponse<ILikee[]>;

@Injectable({ providedIn: 'root' })
export class LikeeService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/likees');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(likee: NewLikee): Observable<EntityResponseType> {
    return this.http.post<ILikee>(this.resourceUrl, likee, { observe: 'response' });
  }

  saveLikeIsMatch(likee: NewLikee): Observable<HttpResponse<Number>> {
    return this.http.post<Number>(this.resourceUrl + '/match', likee, { observe: 'response' });
  }
  update(likee: ILikee): Observable<EntityResponseType> {
    return this.http.put<ILikee>(`${this.resourceUrl}/${this.getLikeeIdentifier(likee)}`, likee, { observe: 'response' });
  }

  partialUpdate(likee: PartialUpdateLikee): Observable<EntityResponseType> {
    return this.http.patch<ILikee>(`${this.resourceUrl}/${this.getLikeeIdentifier(likee)}`, likee, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ILikee>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  isMatch(firstPetId: number, secondPetId: number): Observable<HttpResponse<boolean>> {
    return this.http.get<boolean>(`${this.resourceUrl}/isMatch/${firstPetId}/${secondPetId}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ILikee[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getLikeeIdentifier(likee: Pick<ILikee, 'id'>): number {
    return likee.id;
  }

  compareLikee(o1: Pick<ILikee, 'id'> | null, o2: Pick<ILikee, 'id'> | null): boolean {
    return o1 && o2 ? this.getLikeeIdentifier(o1) === this.getLikeeIdentifier(o2) : o1 === o2;
  }

  addLikeeToCollectionIfMissing<Type extends Pick<ILikee, 'id'>>(
    likeeCollection: Type[],
    ...likeesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const likees: Type[] = likeesToCheck.filter(isPresent);
    if (likees.length > 0) {
      const likeeCollectionIdentifiers = likeeCollection.map(likeeItem => this.getLikeeIdentifier(likeeItem)!);
      const likeesToAdd = likees.filter(likeeItem => {
        const likeeIdentifier = this.getLikeeIdentifier(likeeItem);
        if (likeeCollectionIdentifiers.includes(likeeIdentifier)) {
          return false;
        }
        likeeCollectionIdentifiers.push(likeeIdentifier);
        return true;
      });
      return [...likeesToAdd, ...likeeCollection];
    }
    return likeeCollection;
  }
}
