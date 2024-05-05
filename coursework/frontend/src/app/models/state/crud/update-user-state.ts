import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";


export interface UpdateUserState {
  dataState?: DataState;
  user?: UserDto;
  invalid?: boolean;
  violations?: {
    firstName?: string;
    lastName?: string;
    email?: string;
    phoneNumber?: string;
    age?: number;
    password?: string;
    passwordCopy?: string;
  };
  message?: string;
}
