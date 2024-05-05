import {TableDto} from "../table/table-dto";

export interface CallDto {
  id?: string;
  tableId?: string;
  table?: TableDto
  purpose?: string;
  waitersId?: string[];
  openedAt?: Date;
}
