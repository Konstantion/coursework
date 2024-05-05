export interface ProductDto {
  id: string;
  name: string;
  price: number;
  weight: number;
  categoryId: string;
  description: string;
  creatorId: string;
  imageBytes: ArrayBuffer;
  createdAt: Date;
  deactivateAt: Date;
  active: boolean;
}
