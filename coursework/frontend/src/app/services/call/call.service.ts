import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CallResponse} from 'src/app/models/responses/call-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CallService {

  private readonly callUrl = `${environment.api.baseUrl}/${environment.api.callUrl}`;
  private readonly adminCallUrl = `${environment.api.baseUrl}/${environment.api.adminCallUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  activeCalls$ = <Observable<CallResponse>>this.http.get(`${this.callUrl}`)
    .pipe(
      tap(console.log)
    )

  closeCallById$ = (id: string) => <Observable<CallResponse>>this.http.delete(`${this.callUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  calls$ = <Observable<CallResponse>>this.http.get(`${this.adminCallUrl}`)
    .pipe(
      tap(console.log)
    )
}
