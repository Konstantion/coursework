import {CategoryDto} from "../../dto/category/category-dto";
import {DataState} from "../enum/data-state";

export interface CategoryPageState {
  dataState?: DataState;
  category?: CategoryDto;
}
