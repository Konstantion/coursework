import {HallDto} from "../../dto/hall/hall-dto";
import {TableDto} from "../../dto/table/table-dto";
import {DataState} from "../enum/data-state";

export interface HallPageState {
  tables?: TableDto[];
  tablesState?: DataState;
  hall?: HallDto;
  hallState?: DataState;
  message?: string;
}
