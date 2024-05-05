import {OrderDto} from "../../dto/order/order-dto";
import {TableDto} from "../../dto/table/table-dto";
import {DataState} from "../enum/data-state";

export interface OrdersPageState {
  orders?: OrderDto[];
  dataState?: DataState;
  inactive?: boolean;
  tableNames?: Map<string, TableDto>;
}
