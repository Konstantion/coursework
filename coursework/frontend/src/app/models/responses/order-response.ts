import {OrderDto} from "../dto/order/order-dto";

export interface OrderResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { order?: OrderDto, orders?: OrderDto[] };
}
