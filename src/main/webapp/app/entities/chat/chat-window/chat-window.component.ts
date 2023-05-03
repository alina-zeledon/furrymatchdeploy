import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { IOwner } from 'app/entities/owner/owner.model';
import { ChatService } from '../service/chat.service';
import { IChat, NewChat } from '../chat.model';
import { Account } from '../../../core/auth/account.model';
import { AccountService } from '../../../core/auth/account.service';
import { HttpResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

@Component({
  selector: 'jhi-chat-window',
  templateUrl: './chat-window.component.html',
  styleUrls: ['./chat-window.component.scss'],
})
export class ChatWindowComponent implements OnInit, OnDestroy {
  @Input() recipient: IOwner | null = null;
  @Input() identityNumber: number | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() changeMessage = new EventEmitter<string>();
  newMessage = '';
  currentUser: Account | null = null;
  messages: IChat[] = [];
  match: any;
  isSendingMessage: boolean = false;
  messageUpdateInterval: any;

  constructor(private chatService: ChatService, private accountService: AccountService) {
    this.accountService.identity().subscribe((account: Account | null) => {
      this.currentUser = account;
      this.loadMessages();
    });
  }

  ngOnInit(): void {
    this.messageUpdateInterval = setInterval(() => {
      this.loadMessages();
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.messageUpdateInterval) {
      clearInterval(this.messageUpdateInterval);
    }
  }

  loadMessages(): void {
    if (this.currentUser && this.recipient && this.currentUser.id && this.recipient.id) {
      const senderStateChat1 = `${this.currentUser.id};${this.recipient.id};unread`;
      const senderStateChat2 = `${this.currentUser.id};${this.recipient.id};read`;
      const receiverStateChat1 = `${this.recipient.id};${this.currentUser.id};unread`;
      const receiverStateChat2 = `${this.recipient.id};${this.currentUser.id};read`;
      this.getMessages(senderStateChat1, senderStateChat2, receiverStateChat1, receiverStateChat2);
    } else {
      console.error('No se pueden cargar los mensajes: senderId o recipientId no son válidos');
    }
  }

  onCloseClick(): void {
    this.close.emit();
  }

  onSendMessage(event: Event): void {
    event.preventDefault();
    if (this.newMessage.trim()) {
      if (this.currentUser && this.recipient && this.currentUser.id && this.recipient.id && this.identityNumber) {
        this.isSendingMessage = true;
        const senderStateChat1 = `${this.currentUser.id};${this.recipient.id};unread`;
        const senderStateChat2 = `${this.currentUser.id};${this.recipient.id};read`;
        const receiverStateChat1 = `${this.recipient.id};${this.currentUser.id};unread`;
        const receiverStateChat2 = `${this.recipient.id};${this.currentUser.id};read`;
        const message: NewChat = {
          id: null,
          message: this.newMessage,
          dateChat: null,
          stateChat: senderStateChat1,
          match: { id: this.identityNumber },
        };
        this.changeMessage.emit(this.newMessage);
        this.chatService.create(message).subscribe(() => {
          this.newMessage = '';
          this.getMessages(senderStateChat1, senderStateChat2, receiverStateChat1, receiverStateChat2);
          this.isSendingMessage = false;
        });
      } else {
        Swal.fire({
          title: 'Error al enviar mensaje',
          text: 'Intentá más tarde o ponte en contacto con el equipo de FurrMatch para ayudarte',
          showCancelButton: false,
          showConfirmButton: true,
          showDenyButton: false,
          confirmButtonText: 'Sí',
          denyButtonText: 'No',
          icon: 'error',
          confirmButtonColor: '#3381f6',
          denyButtonColor: '#3381f6',
        });
      }
    }
  }

  getMessages(senderStateChat1: string, senderStateChat2: string, receiverStateChat1: string, receiverStateChat2: string): void {
    forkJoin([
      this.chatService.getChatsByStateChat(senderStateChat1, senderStateChat2),
      this.chatService.getChatsByStateChat(receiverStateChat1, receiverStateChat2),
    ]).subscribe(([res1, res2]: [HttpResponse<IChat[]>, HttpResponse<IChat[]>]) => {
      const sentMessages = res1.body || [];
      const receivedMessages = res2.body || [];
      this.messages = [...sentMessages, ...receivedMessages].sort((a, b) => a.dateChat!.toString().localeCompare(b.dateChat!.toString()));
    });
  }
}
