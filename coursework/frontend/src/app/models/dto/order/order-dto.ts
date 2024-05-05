export interface OrderDto {
  id: string;
  active: boolean;
  productsId: string[];
  tableId: string;
  userId: string;
  billId: string;
  createdAt: Date;
  closedAt: Date;
}
