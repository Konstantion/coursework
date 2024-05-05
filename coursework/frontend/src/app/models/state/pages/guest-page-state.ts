import {GuestDto} from "../../dto/guest/guest-dto";
import {DataState} from "../enum/data-state";

export interface GuestPageState {
  dataState?: DataState;
  guest?: GuestDto;
}
