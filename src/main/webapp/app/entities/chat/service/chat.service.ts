import { EventEmitter, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IChat, NewChat } from '../chat.model';

export type PartialUpdateChat = Partial<IChat> & Pick<IChat, 'id'>;

type RestOf<T extends IChat | NewChat> = Omit<T, 'dateChat'> & {
  dateChat?: string | null;
};

export type RestChat = RestOf<IChat>;

export type NewRestChat = RestOf<NewChat>;

export type PartialUpdateRestChat = RestOf<PartialUpdateChat>;

export type EntityResponseType = HttpResponse<IChat>;
export type EntityArrayResponseType = HttpResponse<IChat[]>;

@Injectable({ providedIn: 'root' })
export class ChatService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/chats');
  public chatRead: EventEmitter<void> = new EventEmitter<void>();
  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(chat: NewChat): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chat);
    return this.http.post<RestChat>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  createEmpty(id: number): Observable<EntityResponseType> {
    return this.http.post<IChat>(`${this.resourceUrl}/empty`, id, { observe: 'response' });
  }

  update(chat: IChat): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chat);
    return this.http
      .put<RestChat>(`${this.resourceUrl}/${this.getChatIdentifier(chat)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(chat: PartialUpdateChat): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chat);
    return this.http
      .patch<RestChat>(`${this.resourceUrl}/${this.getChatIdentifier(chat)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  getChatsByStateChat(state1: string, state2: string): Observable<HttpResponse<IChat[]>> {
    return this.http.get<IChat[]>(`${this.resourceUrl}/state?state1=${state1}&state2=${state2}`, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestChat>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestChat[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getChatIdentifier(chat: Pick<IChat, 'id'>): number {
    return chat.id;
  }

  compareChat(o1: Pick<IChat, 'id'> | null, o2: Pick<IChat, 'id'> | null): boolean {
    return o1 && o2 ? this.getChatIdentifier(o1) === this.getChatIdentifier(o2) : o1 === o2;
  }

  addChatToCollectionIfMissing<Type extends Pick<IChat, 'id'>>(
    chatCollection: Type[],
    ...chatsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const chats: Type[] = chatsToCheck.filter(isPresent);
    if (chats.length > 0) {
      const chatCollectionIdentifiers = chatCollection.map(chatItem => this.getChatIdentifier(chatItem)!);
      const chatsToAdd = chats.filter(chatItem => {
        const chatIdentifier = this.getChatIdentifier(chatItem);
        if (chatCollectionIdentifiers.includes(chatIdentifier)) {
          return false;
        }
        chatCollectionIdentifiers.push(chatIdentifier);
        return true;
      });
      return [...chatsToAdd, ...chatCollection];
    }
    return chatCollection;
  }

  protected convertDateFromClient<T extends IChat | NewChat | PartialUpdateChat>(chat: T): RestOf<T> {
    return {
      ...chat,
      dateChat: chat.dateChat?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restChat: RestChat): IChat {
    return {
      ...restChat,
      dateChat: restChat.dateChat ? dayjs(restChat.dateChat) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestChat>): HttpResponse<IChat> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestChat[]>): HttpResponse<IChat[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
  getUnreadChatsForCurrentUser(): Observable<HttpResponse<IChat[]>> {
    return this.http.get<IChat[]>(`${this.resourceUrl}/unread`, { observe: 'response' });
  }

  updateChatState(matchId: number, senderId: number): Observable<void> {
    return this.http.put<void>(`${this.resourceUrl}/update-state/${matchId}/${senderId}`, null);
  }
}
