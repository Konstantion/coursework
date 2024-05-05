import {OrderDto} from "../../dto/order/order-dto";
import {ProductDto} from "../../dto/product/product-dto";
import {TableDto} from "../../dto/table/table-dto";
import {UserDto} from "../../dto/user/user-dto";
import {DataState} from "../enum/data-state";

export interface OrderPageState {
  orderState?: DataState;
  order?: OrderDto;
  table?: TableDto;
  user?: UserDto;

  productsState?: DataState;
  products?: Map<ProductDto, number>;
  productsList?: ProductDto[];

  message?: string;
}
