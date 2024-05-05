import {ProductDto} from "../dto/product/product-dto";
import {ProductPageDto} from "../dto/product/product-page-dto";

export interface ProductResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { product?: ProductDto, products?: ProductPageDto };
}
