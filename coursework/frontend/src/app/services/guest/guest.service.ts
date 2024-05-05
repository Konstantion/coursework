import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateGuestRequestDto} from 'src/app/models/dto/guest/create-guest-request-dto';
import {UpdateGuestRequestDto} from 'src/app/models/dto/guest/update-guest-request-dto';
import {GuestResponse} from 'src/app/models/responses/guest-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class GuestService {

  private readonly guestUrl = `${environment.api.baseUrl}/${environment.api.guestUrl}`;
  private readonly adminGuestUrl = `${environment.api.baseUrl}/${environment.api.adminGuestUrl}`;

  guests$ = <Observable<GuestResponse>>this.http.get(`${this.adminGuestUrl}`)
    .pipe(
      tap(console.log)
    )
  activeGuests$ = <Observable<GuestResponse>>this.http.get(`${this.guestUrl}`)
    .pipe(
      tap(console.log)
    )

  constructor(
    private http: HttpClient
  ) {
  }

  guestById$ = (id: string) => <Observable<GuestResponse>>this.http.get(`${this.guestUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  activateGuestById$ = (id: string) => <Observable<GuestResponse>>this.http.put(`${this.adminGuestUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    )

  deactivateGuestById$ = (id: string) => <Observable<GuestResponse>>this.http.put(`${this.adminGuestUrl}/${id}/deactivate`, '')
    .pipe(
      tap(console.log)
    )

  deleteGuestById$ = (id: string) => <Observable<GuestResponse>>this.http.delete(`${this.adminGuestUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  createGuest$ = (requestDto: CreateGuestRequestDto) => <Observable<GuestResponse>>this.http.post(`${this.adminGuestUrl}`, requestDto)
    .pipe(
      tap(console.log)
    )

  updateGuestById$ = (id: string, requestDto: UpdateGuestRequestDto) => <Observable<GuestResponse>>this.http.put(`${this.adminGuestUrl}/${id}`, requestDto)
    .pipe(
      tap(console.log)
    )
}
