import {HallDto} from "src/app/models/dto/hall/hall-dto";
import {DataState} from "src/app/models/state/enum/data-state";

export interface CreateHallState {
  dataState?: DataState;
  hall?: HallDto;
  invalid?: boolean;
  violations?: { name?: string };
  message?: string;
}
