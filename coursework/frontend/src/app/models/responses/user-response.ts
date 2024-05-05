import {UserDto} from "../dto/user/user-dto";

export interface UserResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { user?: UserDto, users?: UserDto[] };
}
