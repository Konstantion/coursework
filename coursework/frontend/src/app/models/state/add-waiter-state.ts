import {DataState} from "src/app/models/state/enum/data-state";

export interface AddWaiterState {
  dataState?: DataState;
  waiterId?: string;
  invalid?: boolean;
  violations?: {};
  message?: string;
}
