import {GuestDto} from "../../dto/guest/guest-dto";
import {DataState} from "../enum/data-state";

export interface UpdateGuestState {
  guest?: GuestDto;
  dataState?: DataState;
  invalid?: boolean;
  violations?: {
    name?: string;
    phoneNumber?: string;
    discountPercent?: number;
  };
  message?: string;
}
