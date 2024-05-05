import {BillDto} from "../dto/bill/bill-dto";

export interface BillResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { bill?: BillDto, bills?: BillDto[] };
}
