import {TableDto} from "../../dto/table/table-dto";
import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";

export interface TablePageState {
  table?: TableDto;
  tableState?: DataState;
  users?: UserDto[];
  usersState?: DataState;
  message?: string;
  waiters?: UserDto[];
}
