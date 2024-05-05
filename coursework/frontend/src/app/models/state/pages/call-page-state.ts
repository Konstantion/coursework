import {CallDto} from "../../dto/call/call-dto";
import {DataState} from "../enum/data-state";

export interface CallPageState {
  dataState?: DataState;
  calls?: CallDto[];
}
