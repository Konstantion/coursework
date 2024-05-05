import {TableDto} from "../dto/table/table-dto";

export interface TableResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { table?: TableDto, tables?: TableDto[] };
}
