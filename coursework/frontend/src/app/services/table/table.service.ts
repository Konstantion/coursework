import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateTableRequestDto} from 'src/app/models/dto/table/create-table-request-dto';
import {TableWaiterRequestDto} from 'src/app/models/dto/table/table-waiter-request-dto';
import {OrderResponse} from 'src/app/models/responses/order-response';
import {TableResponse} from 'src/app/models/responses/table-response';
import {UserResponse} from 'src/app/models/responses/user-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TableService {

  private readonly tableUrl = `${environment.api.baseUrl}/${environment.api.tableUrl}`;
  private readonly adminTableUrl = `${environment.api.baseUrl}/${environment.api.adminTableUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  activeTables$ = <Observable<TableResponse>>this.http.get<TableResponse>(`${this.tableUrl}`)
    .pipe(
      tap(console.log)
    );

  tables$ = <Observable<TableResponse>>this.http.get<TableResponse>(`${this.adminTableUrl}`)
    .pipe(
      tap(console.log)
    );

  findById$ = (id: string) => <Observable<TableResponse>>this.http.get<TableResponse>(`${this.tableUrl}/${id}`)
    .pipe(
      tap(console.log)
    );


  createTable$ = (requestDto: CreateTableRequestDto) => <Observable<TableResponse>>this.http.post(`${this.adminTableUrl}`, requestDto)
    .pipe(
      tap(console.log)
    )

  updateTable$ = (id: string, requestDto: CreateTableRequestDto) => <Observable<TableResponse>>this.http.put(`${this.adminTableUrl}/${id}`, requestDto)
    .pipe(
      tap(console.log)
    )

  activateTable$ = (id: string) => <Observable<TableResponse>>this.http.put(`${this.adminTableUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    )

  deactivateTable$ = (id: string) => <Observable<TableResponse>>this.http.put(`${this.adminTableUrl}/${id}/deactivate`, '')
    .pipe(
      tap(console.log)
    )

  deleteTable$ = (id: string) => <Observable<TableResponse>>this.http.delete(`${this.adminTableUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  addWaiter$ = (id: string, requestDto: TableWaiterRequestDto) => <Observable<TableResponse>>this.http.put(`${this.adminTableUrl}/${id}/waiters/`, requestDto)
    .pipe(
      tap(console.log)
    )

  removeWaiter$ = (tableId: string, waiterId: string) => <Observable<TableResponse>>this.http.delete(`${this.adminTableUrl}/${tableId}/waiters/${waiterId}`)
    .pipe(
      tap(console.log)
    )

  removeWaiters$ = (id: string) => <Observable<TableResponse>>this.http.delete(`${this.adminTableUrl}/${id}/waiters`)
    .pipe(
      tap(console.log)
    )

  tableWaiters$ = (id: string) => <Observable<UserResponse>>this.http.get(`${this.tableUrl}/${id}/waiters`)
    .pipe(
      tap(console.log)
    )

  openOrderOnTableById$ = (id: string) => <Observable<OrderResponse>>this.http.post(`${this.tableUrl}/${id}/order`, '')
    .pipe(
      tap(console.log)
    )
}
