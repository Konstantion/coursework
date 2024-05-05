import {CategoryDto} from "../../dto/category/category-dto";
import {ProductPageDto} from "../../dto/product/product-page-dto";
import {DataState} from "../enum/data-state";

export interface ProductsPageState {
  dataState?: DataState;
  page?: ProductPageDto;
  categories?: CategoryDto[];
  pages?: number[];
}
