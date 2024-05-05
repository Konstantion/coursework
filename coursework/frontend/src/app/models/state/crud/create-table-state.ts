import {TableDto} from "src/app/models/dto/table/table-dto";
import {DataState} from "src/app/models/state/enum/data-state";

export interface CreateTableState {
  dataState?: DataState;
  table?: TableDto;
  invalid?: boolean;
  violations?: {
    password?: string, name?: string, tableType?: string, capacity?: string
  };
  message?: string;
}
