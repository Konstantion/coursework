import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateHallRequestDto} from 'src/app/models/dto/hall/create-hall-request-dto';
import {HallResponse} from 'src/app/models/responses/hall-response';
import {TableResponse} from 'src/app/models/responses/table-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HallService {

  private readonly hallUrl = `${environment.api.baseUrl}/${environment.api.hallUrl}`;
  private readonly adminHallUrl = `${environment.api.baseUrl}/${environment.api.adminHallUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  hall$ = (id: string) => <Observable<HallResponse>>this.http.get<HallResponse>(`${this.hallUrl}/${id}`)
    .pipe(
      tap(console.log)
    );

  activeHalls$ = <Observable<HallResponse>>this.http.get<HallResponse>(`${this.hallUrl}`)
    .pipe(
      tap(console.log)
    );

  halls$ = <Observable<HallResponse>>this.http.get<HallResponse>(`${this.adminHallUrl}`)
    .pipe(
      tap(console.log)
    );

  hallTables$ = (id: string) => <Observable<TableResponse>>this.http.get<TableResponse>(`${this.adminHallUrl}/${id}/tables`)
    .pipe(
      tap(console.log)
    );

  hallActiveTables$ = (id: string) => <Observable<TableResponse>>this.http.get<TableResponse>(`${this.hallUrl}/${id}/tables`)
    .pipe(
      tap(console.log)
    );

  createHall$ = (createHallRequest: CreateHallRequestDto) => <Observable<HallResponse>>this.http.post<HallResponse>(`${this.adminHallUrl}`, createHallRequest)
    .pipe(
      tap(console.log)
    )

  updateHall$ = (id: string, updateHallRequest: CreateHallRequestDto) => <Observable<HallResponse>>this.http.put<HallResponse>(`${this.adminHallUrl}/${id}`, updateHallRequest)
    .pipe(
      tap(console.log)
    )

  activateHall$ = (id: string) => <Observable<HallResponse>>this.http.put(`${this.adminHallUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    )

  deactivateHall$ = (id: string) => <Observable<HallResponse>>this.http.put(`${this.adminHallUrl}/${id}/deactivate`, '')
    .pipe(
      tap(console.log)
    )

  deleteHall$ = (id: string) => <Observable<HallResponse>>this.http.delete(`${this.adminHallUrl}/${id}`)
    .pipe(
      tap(console.log)
    )
}
