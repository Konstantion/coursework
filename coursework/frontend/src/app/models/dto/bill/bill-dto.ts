export interface BillDto {
  id?: string;
  waiterId?: string;
  orderId?: string;
  guestId?: string;
  active?: boolean;
  createdAt?: string;
  closedAt?: string;
  price?: number;
  priceWithDiscount?: number;
}
