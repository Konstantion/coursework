import {GuestDto} from "../../dto/guest/guest-dto";
import {DataState} from "../enum/data-state";

export interface GuestsPageState {
  guests?: GuestDto[];
  dataState?: DataState;
  inactive?: boolean;
}
