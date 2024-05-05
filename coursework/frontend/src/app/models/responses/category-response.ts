import {CategoryDto} from "../dto/category/category-dto";

export interface CategoryResponse {
  timeStamp?: Date;
  statusCode?: number;
  status?: string;
  reason?: string;
  message?: string;
  developerMessage?: string;
  data?: { category?: CategoryDto, categories?: CategoryDto[] };
}
