import {CategoryDto} from "../../dto/category/category-dto";
import {DataState} from "../enum/data-state";

export interface CreateCategoryState {
  category?: CategoryDto;
  dataState?: DataState;
  invalid?: boolean;
  violations?: {
    name?: string;
  };
  message?: string;
}
