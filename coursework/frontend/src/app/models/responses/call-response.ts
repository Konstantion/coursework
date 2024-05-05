import {CallDto} from "../dto/call/call-dto";

export interface CallResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { call?: CallDto, calls?: CallDto[] };
}
