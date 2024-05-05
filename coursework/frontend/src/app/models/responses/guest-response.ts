import {GuestDto} from "../dto/guest/guest-dto";

export interface GuestResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { guest?: GuestDto, guests?: GuestDto[] };
}
