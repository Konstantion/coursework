import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, of, startWith} from 'rxjs';
import {Permission} from 'src/app/models/dto/user/permission';
import {Role} from 'src/app/models/dto/user/role';
import {UpdateUserRequestDto} from 'src/app/models/dto/user/update-user-request-dto';
import {DataState} from 'src/app/models/state/enum/data-state';
import {UserPageState} from 'src/app/models/state/pages/user-page-state';
import {UpdateUserState} from 'src/app/models/state/crud/update-user-state';
import {ObjectUtils} from 'src/app/models/util/object-utils';
import {UserService} from 'src/app/services/user/user.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class UserComponent implements OnInit {
  roleMap = Object.values(Role).map(role => {
    let name = role.toString();
    return {role, name}
  });
  permissionMap = Object.values(Permission).map(permission => {
    let name = this.replacePermissionUnderscore(permission, ' ');
    return {permission, name}
  });
  permissionModal = false;
  roleModal = false;
  showUpdateModal = false;
  updateData: UpdateUserRequestDto = {};
  selectedPermission: Permission = null;
  selectedRole: Role = null;
  readonly DataState = DataState;
  private userId = '';
  private userPageSubject = new BehaviorSubject<UserPageState>({})
  pageState$ = this.userPageSubject.asObservable();
  private updateUserSubject = new BehaviorSubject<UpdateUserState>({});
  updateState$ = this.updateUserSubject.asObservable();

  constructor(
    private userService: UserService,
    private activeRoute: ActivatedRoute,
    private router: Router,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    this.userId = this.activeRoute.snapshot.paramMap.get('id');

    this.userService.userById$(this.userId).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;
        state.dataState = DataState.LOADED_STATE;
        this.userPageSubject.next(state);
      }),
      startWith(() => this.userPageSubject.next({dataState: DataState.LOADING_STATE})),
      catchError(error => this.handleError(error))
    ).subscribe();
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

  onActivate() {
    this.userService.activateUserById$(this.userId).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onDeactivate() {
    this.userService.deactivateUserById$(this.userId).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onDelete() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete user?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.delete();
      },
      reject: () => {

      }
    });
  }

  delete() {
    this.userService.deleteUserById$(this.userId).pipe(
      map(response => {
        this.router.navigate([`tables`]);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onPermission() {
    this.permissionModal = true;
    this.selectedPermission = null;
  }

  addPermission() {
    this.userService.addPermissionToUserById$(this.userId, this.selectedPermission).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
        this.closePermission();
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  removePermission() {
    this.userService.removePermissionFromUserById$(this.userId, this.selectedPermission).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
        this.closePermission();
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  closeRole() {
    this.roleModal = false;
  }

  closeUpdate() {
    this.showUpdateModal = false;
  }

  onUpdate() {
    this.showUpdateModal = true;
    this.updateData = {...this.userPageSubject.value.user, email: this.userPageSubject.value.user.username};
  }

  update() {
    this.updateUserSubject.next({dataState: DataState.LOADING_STATE});
    this.userService.updateUser$(this.userId, ObjectUtils.replaceEmptyWithNull(this.updateData)).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;
        this.userPageSubject.next(state);
        this.closeUpdate();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.updateUserSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  addRole() {
    this.userService.addRoleToUserById$(this.userId, this.selectedRole).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
        this.closeRole();
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  removeRole() {
    this.userService.removeRoleFromUserById$(this.userId, this.selectedRole).pipe(
      map(response => {
        const state = this.userPageSubject.value;
        state.user = response.data.user;

        this.userPageSubject.next(state);
        this.closeRole();
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  closePermission() {
    this.permissionModal = false;
  }

  onRole() {
    this.roleModal = true;
    this.selectedRole = null;
  }

  replaceUnderscore(target: string, replacement: string): string {
    return target.replace(/_/g, replacement);
  }

  replacePermissionUnderscore(target: Permission, replacement: string): string {
    return this.replaceUnderscore(target.toString(), replacement);
  }

  getRoleFromString(input: string): Role | null {
    const enumValues = Object.values(Role);
    const matchedValue = enumValues.find(value => value === input);

    return matchedValue !== undefined ? matchedValue : null;
  }

  getPermissionFromString(input: string): Permission | null {
    const enumValues = Object.values(Permission);
    const matchedValue = enumValues.find(value => value === input);

    return matchedValue !== undefined ? matchedValue : null;
  }
}
