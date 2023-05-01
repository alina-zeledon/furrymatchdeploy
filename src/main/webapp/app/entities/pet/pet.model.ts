import { IOwner } from 'app/entities/owner/owner.model';
import { IBreed } from 'app/entities/breed/breed.model';
import { PetType } from 'app/entities/enumerations/pet-type.model';
import { Sex } from 'app/entities/enumerations/sex.model';
import { IPhoto } from '../photo/photo.model';
import { IUser } from '../../admin/user-management/user-management.model';

export interface IPet {
  id: number;
  name?: string | null;
  petType?: PetType | null;
  description?: string | null;
  sex?: Sex | null;
  tradeMoney?: boolean | null;
  tradePups?: boolean | null;
  pedigree?: boolean | null;
  desireAmmount?: number | null;
  owner?: Pick<
    IOwner,
    'id' | 'user_id' | 'firstName' | 'secondLastName' | 'firstLastName' | 'secondLastName' | 'phoneNumber' | 'canton' | 'province' | 'photo'
  > | null;
  breed?: Pick<IBreed, 'id' | 'breed'> | null;
  photos?: IPhoto[];
  user?: Pick<IUser, 'email'> | null;
}

export type NewPet = Omit<IPet, 'id'> & { id: null };
