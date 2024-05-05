import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateBillRequestDto} from 'src/app/models/dto/bill/create-bill-request-dto';
import {BillResponse} from 'src/app/models/responses/bill-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BillService {

  private readonly billUrl = `${environment.api.baseUrl}/${environment.api.billUrl}`;
  private readonly adminBillUrl = `${environment.api.baseUrl}/${environment.api.adminBillUrl}`;


  constructor(
    private http: HttpClient
  ) {
  }


  createBill$ = (requestDto: CreateBillRequestDto) => <Observable<BillResponse>>this.http.post(`${this.billUrl}`, requestDto)
    .pipe(
      tap(console.log)
    )

  activeBills$ = <Observable<BillResponse>>this.http.get(`${this.billUrl}`)
    .pipe(
      tap(console.log)
    )

  closeBillById$ = (id: string) => <Observable<BillResponse>>this.http.put(`${this.billUrl}/${id}/close`, '')
    .pipe(
      tap(console.log)
    )

  billById$ = (id: string) => <Observable<BillResponse>>this.http.get(`${this.billUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  generatePdfByBillId$ = (id: string) => <Observable<Blob>>this.http.get(`${this.billUrl}/${id}/generate-pdf`, {responseType: 'blob'})
    .pipe(
      tap(console.log)
    )

  activateBillById$ = (id: string) => <Observable<BillResponse>>this.http.put(`${this.adminBillUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    )

  deleteBillById$ = (id: string) => <Observable<BillResponse>>this.http.delete(`${this.adminBillUrl}/${id}`)
    .pipe(
      tap(console.log)
    )
  bills$ = <Observable<BillResponse>>this.http.get(`${this.adminBillUrl}`)
    .pipe(
      tap(console.log)
    )
}
