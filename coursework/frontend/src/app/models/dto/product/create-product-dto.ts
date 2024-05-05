export interface CreateProductRequestDto {
  name?: string;
  price?: number;
  weight?: number;
  image?: File;
  description?: string;
  categoryId?: string;
}
