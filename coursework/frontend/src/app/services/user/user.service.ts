import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateUserRequestDto} from 'src/app/models/dto/user/create-user-request-dto';
import {Permission} from 'src/app/models/dto/user/permission';
import {Role} from 'src/app/models/dto/user/role';
import {UpdateUserRequestDto} from 'src/app/models/dto/user/update-user-request-dto';
import {UserResponse} from 'src/app/models/responses/user-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly userUrl = `${environment.api.baseUrl}/${environment.api.userUrl}`;
  private readonly adminUserUrl = `${environment.api.baseUrl}/${environment.api.adminUserUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  allActiveUsers$ = <Observable<UserResponse>>this.http.get(`${this.userUrl}`)
    .pipe(
      tap(console.log)
    );

  users$ = <Observable<UserResponse>>this.http.get(`${this.adminUserUrl}`)
    .pipe(
      tap(console.log)
    );

  userById$ = (id: string) => <Observable<UserResponse>>this.http.get(`${this.userUrl}/${id}`)
    .pipe(
      tap(console.log)
    );

  activateUserById$ = (id: string) => <Observable<UserResponse>>this.http.put(`${this.adminUserUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    );

  deactivateUserById$ = (id: string) => <Observable<UserResponse>>this.http.put(`${this.adminUserUrl}/${id}/deactivate`, '')
    .pipe(
      tap(console.log)
    );

  deleteUserById$ = (id: string) => <Observable<UserResponse>>this.http.delete(`${this.adminUserUrl}/${id}`)
    .pipe(
      tap(console.log)
    );

  addRoleToUserById$ = (id: string, role: Role) => {
    let params = new HttpParams()
      .set('role', role);

    return <Observable<UserResponse>>this.http.put(`${this.adminUserUrl}/${id}/roles`, '', {params})
      .pipe(
        tap(console.log)
      );
  }

  removeRoleFromUserById$ = (id: string, role: Role) => {
    let params = new HttpParams()
      .set('role', role);

    return <Observable<UserResponse>>this.http.delete(`${this.adminUserUrl}/${id}/roles`, {params})
      .pipe(
        tap(console.log)
      );
  }

  addPermissionToUserById$ = (id: string, permission: Permission) => {
    let params = new HttpParams()
      .set('permission', permission);

    return <Observable<UserResponse>>this.http.put(`${this.adminUserUrl}/${id}/permissions`, '', {params})
      .pipe(
        tap(console.log)
      );
  }

  removePermissionFromUserById$ = (id: string, permission: Permission) => {
    let params = new HttpParams()
      .set('permission', permission);

    return <Observable<UserResponse>>this.http.delete(`${this.adminUserUrl}/${id}/permissions`, {params})
      .pipe(
        tap(console.log)
      );
  }

  createWaiter$ = (requestDto: CreateUserRequestDto) => <Observable<UserResponse>>this.http.post(`${this.adminUserUrl}/waiters`, requestDto)
    .pipe(
      tap(console.log)
    )

  createAdmin$ = (requestDto: CreateUserRequestDto) => <Observable<UserResponse>>this.http.post(`${this.adminUserUrl}/admins`, requestDto)
    .pipe(
      tap(console.log)
    )

  updateUser$ = (id: string, requestDto: UpdateUserRequestDto) => <Observable<UserResponse>>this.http.put(`${this.adminUserUrl}/${id}`, requestDto)
    .pipe(
      tap(console.log)
    )
}
