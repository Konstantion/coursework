import {CategoryDto} from "../../dto/category/category-dto";
import {ProductDto} from "../../dto/product/product-dto";
import {DataState} from "../enum/data-state";

export interface ProductPageState {
  dataState?: DataState;
  product?: ProductDto;
  category?: CategoryDto;
  imageUrl?: string;
}
