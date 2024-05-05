import {HallDto} from "src/app/models/dto/hall/hall-dto";
import {TableDto} from "src/app/models/dto/table/table-dto";
import {DataState} from "src/app/models/state/enum/data-state";

export interface TablesPageState {
  tables?: TableDto[];
  tablesState?: DataState;
  halls?: HallDto[];
  hallsState?: DataState;
}
