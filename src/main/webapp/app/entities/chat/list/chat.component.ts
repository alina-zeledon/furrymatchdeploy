import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router } from '@angular/router';
import { combineLatest, filter, Observable, of, switchMap, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';

import { IChat } from '../chat.model';
import { IOwner } from '../../owner/owner.model';

import { ITEMS_PER_PAGE, PAGE_HEADER, TOTAL_COUNT_RESPONSE_HEADER } from 'app/config/pagination.constants';
import { ASC, DESC, SORT, ITEM_DELETED_EVENT, DEFAULT_SORT_DATA } from 'app/config/navigation.constants';
import { EntityArrayResponseType, ChatService } from '../service/chat.service';
import { OwnerService } from '../../owner/service/owner.service';
import { ChatDeleteDialogComponent } from '../delete/chat-delete-dialog.component';
import { map } from 'rxjs/operators';

@Component({
  selector: 'jhi-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css'],
})
export class ChatComponent implements OnInit {
  chats?: IOwner[];
  chatArrays?: IOwner[];
  isLoading = false;

  selectedRecipient: IOwner | null = null;
  selectedMatchId: number | null = null;

  predicate = 'id';
  ascending = true;

  unreadChats: IChat[] = [];
  itemsPerPage = ITEMS_PER_PAGE;
  totalItems = 0;
  page = 1;

  unreadMatchIds: (number | undefined)[] = [];

  constructor(
    protected chatService: ChatService,
    protected ownerService: OwnerService,
    protected activatedRoute: ActivatedRoute,
    public router: Router,
    protected modalService: NgbModal
  ) {}

  trackId = (_index: number, item: IChat): number => this.chatService.getChatIdentifier(item);

  ngOnInit(): void {
    //this.load();
    this.listChats();
    this.loadUnreadChats();
  }

  listChats(): void {
    this.ownerService.findUserChats().subscribe(result => {
      if (result.body) {
        this.chats = result.body.filter((value, index, self) => self.findIndex(v => v.id === value.id) === index);
        //this.chats = result.body;
        let array2: IOwner[] = [];
        const array = [...this.chats];
        array.forEach((item, index) => {
          if (item.phoneNumber) {
            this.chatService.find(item.phoneNumber).subscribe(result => {
              array2[index] = { ...array[index], message: result.body?.message };
              console.log(JSON.stringify(result, null, 2));
            });
          }
          this.chatArrays = array2;
        });
      }
    });
  }
  loadUnreadChats(): void {
    this.chatService.getUnreadChatsForCurrentUser().subscribe((res: HttpResponse<IChat[]>) => {
      this.unreadChats = res.body || [];

      this.unreadMatchIds = this.unreadChats.map(unreadChat => unreadChat.match?.id).filter(id => id !== undefined);
      console.log('Unread chats: ' + JSON.stringify(this.unreadChats, null, 2));
    });
  }

  delete(id: any): void {
    Swal.fire({
      title: '¿Deseás eliminar la conversación?',
      text: 'Si hacés click en el botón de Sí perderás toda la información del mismo.',
      showCancelButton: false,
      showConfirmButton: true,
      showDenyButton: true,
      confirmButtonText: 'Sí',
      denyButtonText: 'No',
      icon: 'warning',
      confirmButtonColor: '#3381f6',
      denyButtonColor: '#3381f6',
    }).then((result: any) => {
      if (result.isConfirmed) {
        this.chatService.delete(id).subscribe(result => {
          this.listChats();
        });
      } else if (result.isDenied) {
        console.log('no');
      }
    });
  }

  onRecipientClick(recipient: IOwner): void {
    this.selectedRecipient = recipient;
    this.selectedMatchId = parseInt(recipient.identityNumber || '', 10) || null; // Convierte el identityNumber a un número
    console.log('Selected Match ID:', this.selectedMatchId);
    console.log('Recipient ID:', this.selectedRecipient.id);
    console.log('botón presionado');
    // Update the chat state to 'read'
    if (this.selectedMatchId !== null && this.selectedRecipient.id !== null && this.selectedRecipient.id !== undefined) {
      this.chatService.updateChatState(this.selectedMatchId, this.selectedRecipient.id).subscribe(() => {
        console.log('Chat state updated to read');
        this.chatService.chatRead.emit();
      });
    }
  }

  onChatWindowClose(): void {
    this.selectedRecipient = null;
  }

  // delete(chat: IChat): void {
  //   const modalRef = this.modalService.open(ChatDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
  //   modalRef.componentInstance.chat = chat;
  //   // unsubscribe not needed because closed completes on modal close
  //   modalRef.closed
  //     .pipe(
  //       filter(reason => reason === ITEM_DELETED_EVENT),
  //       switchMap(() => this.loadFromBackendWithRouteInformations())
  //     )
  //     .subscribe({
  //       next: (res: EntityArrayResponseType) => {
  //         this.onResponseSuccess(res);
  //       },
  //     });
  // }

  // load(): void {
  //   this.loadFromBackendWithRouteInformations().subscribe({
  //     next: (res: EntityArrayResponseType) => {
  //       this.onResponseSuccess(res);
  //     },
  //   });
  // }

  // navigateToWithComponentValues(): void {
  //   this.handleNavigation(this.page, this.predicate, this.ascending);
  // }

  // navigateToPage(page = this.page): void {
  //   this.handleNavigation(page, this.predicate, this.ascending);
  // }

  // protected loadFromBackendWithRouteInformations(): Observable<EntityArrayResponseType> {
  //   return combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data]).pipe(
  //     tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
  //     switchMap(() => this.queryBackend(this.page, this.predicate, this.ascending))
  //   );
  // }

  // protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
  //   const page = params.get(PAGE_HEADER);
  //   this.page = +(page ?? 1);
  //   const sort = (params.get(SORT) ?? data[DEFAULT_SORT_DATA]).split(',');
  //   this.predicate = sort[0];
  //   this.ascending = sort[1] === ASC;
  // }

  // protected onResponseSuccess(response: EntityArrayResponseType): void {
  //   this.fillComponentAttributesFromResponseHeader(response.headers);
  //   const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
  //   this.chatList = dataFromBody;
  //   console.log(this.chatList);
  // }

  // protected fillComponentAttributesFromResponseBody(data: IChat[] | null): IChat[] {
  //   return data ?? [];
  // }

  // protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
  //   this.totalItems = Number(headers.get(TOTAL_COUNT_RESPONSE_HEADER));
  // }

  // protected queryBackend(page?: number, predicate?: string, ascending?: boolean): Observable<EntityArrayResponseType> {
  //   this.isLoading = true;
  //   const pageToLoad: number = page ?? 1;
  //   const queryObject = {
  //     page: pageToLoad - 1,
  //     size: this.itemsPerPage,
  //     sort: this.getSortQueryParam(predicate, ascending),
  //   };
  //   return this.chatService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  // }

  // protected handleNavigation(page = this.page, predicate?: string, ascending?: boolean): void {
  //   const queryParamsObj = {
  //     page,
  //     size: this.itemsPerPage,
  //     sort: this.getSortQueryParam(predicate, ascending),
  //   };
  //
  //   this.router.navigate(['./'], {
  //     relativeTo: this.activatedRoute,
  //     queryParams: queryParamsObj,
  //   });
  // }

  // protected getSortQueryParam(predicate = this.predicate, ascending = this.ascending): string[] {
  //   const ascendingQueryParam = ascending ? ASC : DESC;
  //   if (predicate === '') {
  //     return [];
  //   } else {
  //     return [predicate + ',' + ascendingQueryParam];
  //   }
  // }
  protected readonly parseInt = parseInt;
}
