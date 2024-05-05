import {AuthenticationResponseDto} from "../dto/authentication/authentication-response-dto";

export interface AuthenticationResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { authentication: AuthenticationResponseDto };
}
