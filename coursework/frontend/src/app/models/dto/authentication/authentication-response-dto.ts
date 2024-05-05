import {TableDto} from "../table/table-dto";
import {UserDto} from "../user/user-dto";

export interface AuthenticationResponseDto {
  token?: string,
  authenticated?: { user?: UserDto, table?: TableDto, entity: Object }
}
