import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {AuthenticationResponse} from 'src/app/models/responses/authentication-response';
import {LoginTableRequestDto} from 'src/app/models/dto/table/login-table-request-dto';
import {LoginUserRequestDto} from 'src/app/models/dto/user/login-user-request-dto';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private readonly userAuthUrl = `${environment.api.baseUrl}/${environment.api.userAuthUrl}`;
  private readonly tableAuthUrl = `${environment.api.baseUrl}/${environment.api.tableAuthUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  loginUser(authRequest: LoginUserRequestDto): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(this.userAuthUrl, authRequest);
  }

  loginTable(authRequest: LoginTableRequestDto): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(this.tableAuthUrl, authRequest);
  }
}
