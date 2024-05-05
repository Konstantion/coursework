import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";

export interface UserPageState {
  dataState?: DataState;
  user?: UserDto;
}
