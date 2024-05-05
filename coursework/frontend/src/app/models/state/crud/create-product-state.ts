import {ProductDto} from "../../dto/product/product-dto";
import {DataState} from "../enum/data-state";


export interface CreateProductState {
  dataState?: DataState;
  product?: ProductDto;
  invalid?: boolean;
  violations?: {
    name?: string,
    price?: string,
    description?: string,
    weight?: string,
    file?: string
  };
  message?: string;
}
