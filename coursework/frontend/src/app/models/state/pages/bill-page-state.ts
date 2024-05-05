import {BillDto} from "../../dto/bill/bill-dto";
import {GuestDto} from "../../dto/guest/guest-dto";
import {ProductDto} from "../../dto/product/product-dto";
import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";

export interface BillPageState {
  bill?: BillDto;
  dataState?: DataState;
  guest?: GuestDto;
  waiter?: UserDto;
  products?: Map<ProductDto, number>
}
