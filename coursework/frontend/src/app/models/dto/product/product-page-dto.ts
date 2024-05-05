import {ProductDto} from "./product-dto";

export interface ProductPageDto {
  content: ProductDto[];
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
