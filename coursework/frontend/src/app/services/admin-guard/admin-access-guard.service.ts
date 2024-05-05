import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {JwtHelperService} from '@auth0/angular-jwt';
import {Observable} from 'rxjs';
import {AuthenticationResponseDto} from 'src/app/models/dto/authentication/authentication-response-dto';

@Injectable({
  providedIn: 'root'
})
export class AdminAccessGuardService implements CanActivate {

  constructor(private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const authResponse: AuthenticationResponseDto = JSON.parse(storedUser);
      const token = authResponse.token;
      if (token) {
        const jwtHelper = new JwtHelperService();
        const isTokenNonExpired = !jwtHelper.isTokenExpired(token);
        if (isTokenNonExpired && authResponse.authenticated.user.roles.includes('ADMIN')) {
          return true;
        }
      }
    }
    this.router.navigate(['tables']);
    return false;
  }
}
