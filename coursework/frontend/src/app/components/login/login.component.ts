import {HttpStatusCode} from '@angular/common/http';
import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {LoginUserRequestDto} from 'src/app/models/dto/user/login-user-request-dto';
import {AuthenticationService} from 'src/app/services/authentication/authentication.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class LoginComponent {
  authenticationRequest: LoginUserRequestDto = {};
  errorMsg = '';
  validationState = {
    isInvalid: false,
    validationError: ''
  }

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router
  ) {
  }

  login() {
    this.errorMsg = '';
    this.authenticationService.loginUser(this.authenticationRequest)
      .subscribe({
        next: (authenticationResponse) => {
          console.log(authenticationResponse);
          let authenticationDto = authenticationResponse.data?.authentication;
          localStorage.setItem('user', JSON.stringify(authenticationDto));
          this.router.navigate(['tables']);
        },
        error: (errorResponse) => {
          this.validationState.isInvalid = false;
          console.error(errorResponse);
          const errorStatus = errorResponse.error.statusCode;
          if (errorStatus === HttpStatusCode.UnprocessableEntity) {
            this.validationState.isInvalid = true;
            this.validationState.validationError = errorResponse.error.data.password;
          }
          this.errorMsg = errorResponse.error.message;
        }
      });
  }
}
