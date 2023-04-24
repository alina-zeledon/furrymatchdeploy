import { IChat } from 'app/entities/chat/chat.model';
import { IOwner } from 'app/entities/owner/owner.model';

export interface IChatOwner {
  owner?: IOwner | null;
  chat?: IChat | null;
}
