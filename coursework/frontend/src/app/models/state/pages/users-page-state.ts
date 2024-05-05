import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";

export interface UsersPageState {
  users?: UserDto[];
  dataState?: DataState;
}
