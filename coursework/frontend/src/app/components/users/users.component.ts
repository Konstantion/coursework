import {Component, Input} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, Observable, of, startWith} from 'rxjs';
import {CreateUserRequestDto} from 'src/app/models/dto/user/create-user-request-dto';
import {UserDto} from 'src/app/models/dto/user/user-dto';
import {UserResponse} from 'src/app/models/responses/user-response';
import {UpdateUserState} from 'src/app/models/state/crud/update-user-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {UsersPageState} from 'src/app/models/state/pages/users-page-state';
import {ObjectUtils} from 'src/app/models/util/object-utils';
import {UserService} from 'src/app/services/user/user.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class UsersComponent {
  @Input() isAdmin: boolean;
  createUserData: CreateUserRequestDto = {};
  onlyInactive = false;
  create = false;
  readonly DataState = DataState;
  private usersPageSubject = new BehaviorSubject<UsersPageState>({});
  pageState$ = this.usersPageSubject.asObservable();
  private createUserSubject = new BehaviorSubject<UpdateUserState>({});
  createState$ = this.createUserSubject.asObservable();

  constructor(
    private router: Router,
    private userService: UserService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    let observable: Observable<UserResponse>;
    if (this.isAdmin) observable = this.userService.users$;
    else observable = this.userService.users$;

    observable.pipe(
      map(response => {
        const state = this.usersPageSubject.value;
        state.users = response.data.users;
        state.dataState = DataState.LOADED_STATE;
      }),
      startWith(this.usersPageSubject.next({dataState: DataState.LOADING_STATE})),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onCreate() {
    this.createUserData = {};
    this.createUserSubject.next({});
    this.create = true;
  }

  createWaiter() {
    this.userService.createWaiter$(ObjectUtils.replaceEmptyWithNull(this.createUserData)).pipe(
      map(response => {
        const state = this.usersPageSubject.value;
        state.users.push(response.data.user);

        this.usersPageSubject.next(state);

        this.onClose();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.createUserSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  createAdmin() {
    this.userService.createAdmin$(ObjectUtils.replaceEmptyWithNull(this.createUserData)).pipe(
      map(response => {
        const state = this.usersPageSubject.value;
        state.users.push(response.data.user);

        this.usersPageSubject.next(state);

        this.onClose();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.createUserSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  onClose() {
    this.create = false;
  }

  handleError(error: any) {
    let errorResponse = error.error;
    if (error.status === 403) {
      this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
    } else if (error.status === 400) {
      this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
    } else {
      this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
    }
    return of();
  }

  onCard(user: UserDto) {
    this.router.navigate([`users/${user.id}`]);
  }
}
