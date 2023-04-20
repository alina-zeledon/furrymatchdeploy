import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IOwner {
  user_id: number;
  id?: number | null;
  message?: string | null;
  firstName?: string | null;
  secondName?: string | null;
  firstLastName?: string | null;
  secondLastName?: string | null;
  phoneNumber?: number | null;
  photo?: string | null;
  identityNumber?: string | null;
  address?: string | null;
  province?: string | null;
  canton?: string | null;
  district?: string | null;
  otp?: string | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id'> | null;
}

export type NewOwner = Omit<IOwner, 'user_id'> & { user_id: null };
