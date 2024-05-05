export interface UserDto {
  id?: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  phoneNumber?: string;
  age?: number;
  active?: boolean;
  roles?: string[];
  createdAt?: Date;
  permissions?: string[];
}
