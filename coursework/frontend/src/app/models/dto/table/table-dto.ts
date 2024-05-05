export interface TableDto {
  id?: string;
  name?: string;
  capacity?: number;
  tableType?: string;
  hallId?: string;
  orderId?: string;
  waitersId?: string[];
  createdAt?: Date;
  deletedAt?: Date;
  active?: boolean;
}
