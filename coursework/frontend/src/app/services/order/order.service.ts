import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {OrderProductRequestDto} from 'src/app/models/dto/order/order-product-request-dto';
import {OrderResponse} from 'src/app/models/responses/order-response';
import {ProductResponse} from 'src/app/models/responses/product-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly orderUrl = `${environment.api.baseUrl}/${environment.api.orderUrl}`;
  private readonly adminOrderUrl = `${environment.api.baseUrl}/${environment.api.adminOrderUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  activeOrders$ = <Observable<OrderResponse>>this.http.get(this.orderUrl)
    .pipe(
      tap(console.log)
    )

  orderById$ = (id: string) => <Observable<OrderResponse>>this.http.get(`${this.orderUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  closeOrderById$ = (id: string) => <Observable<OrderResponse>>this.http.put(`${this.orderUrl}/${id}/close`, '')
    .pipe(
      tap(console.log)
    )

  transferOrderToTableById$ = (orderId: string, tableId: string) => <Observable<OrderResponse>>this.http.put(`${this.orderUrl}/${orderId}/transfer/tables/${tableId}`, '')
    .pipe(
      tap(console.log)
    )

  orderProductsById$ = (id: string) => <Observable<ProductResponse>>this.http.get(`${this.orderUrl}/${id}/products`)
    .pipe(
      tap(console.log)
    )

  addProductsToOrderById$ = (id: string, requestDto: OrderProductRequestDto) =>
    <Observable<OrderResponse>>this.http.put(`${this.orderUrl}/${id}/products`, requestDto)
      .pipe(
        tap(console.log)
      )

  removeProductsFromOrderById$ = (id: string, requestDto: OrderProductRequestDto) =>
    <Observable<OrderResponse>>this.http.put(`${this.orderUrl}/${id}/products/remove`, requestDto)
      .pipe(
        tap(console.log)
      )

  orders$ = <Observable<OrderResponse>>this.http.get(`${this.adminOrderUrl}`)
    .pipe(
      tap(console.log)
    )

  deleteOrderById$ = (id: string) => <Observable<OrderResponse>>this.http.delete(`${this.adminOrderUrl}/${id}`)
    .pipe(
      tap(console.log)
    )
}
