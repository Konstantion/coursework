import {CategoryDto} from "../../dto/category/category-dto";
import {DataState} from "../enum/data-state";

export interface CategoriesPageState {
  categories?: CategoryDto[];
  dataState?: DataState;
}
