import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, concatMap, map, Observable, of, startWith, tap} from 'rxjs';
import {HallDto} from 'src/app/models/dto/hall/hall-dto';
import {CreateTableRequestDto} from 'src/app/models/dto/table/create-table-request-dto';
import {TableDto} from 'src/app/models/dto/table/table-dto';
import {TableWaiterRequestDto} from 'src/app/models/dto/table/table-waiter-request-dto';
import {UserDto} from 'src/app/models/dto/user/user-dto';
import {AddWaiterState} from 'src/app/models/state/add-waiter-state';
import {CreateTableState} from 'src/app/models/state/crud/create-table-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {TablePageState} from 'src/app/models/state/pages/table-page-state';
import {ObjectUtils} from 'src/app/models/util/object-utils';
import {HallService} from 'src/app/services/hall/hall.service';
import {TableService} from 'src/app/services/table/table.service';
import {UserService} from 'src/app/services/user/user.service';

@Component({
  selector: 'app-table',
  templateUrl: './expedition.component.html',
  styleUrls: ['./expedition.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class ExpeditionComponent implements OnInit {
  private tableId: string;
  private pageSubject = new BehaviorSubject<TablePageState>({});
  private tableSubject = new BehaviorSubject<TableDto>({});
  private usersSubject = new BehaviorSubject<UserDto[]>([]);
  private waitersSubject = new BehaviorSubject<UserDto[]>([]);

  readonly DataState = DataState;

  pageState$ = this.pageSubject.asObservable();
  updateTableState$: Observable<CreateTableState> = of({});
  addWaiterState$: Observable<AddWaiterState> = of({});
  removeWaiterState$: Observable<AddWaiterState> = of({});

  halls: HallDto[] = [];
  waiters: UserDto[] = [];
  showTableModal = false;
  showWaiterModal = false;
  removeWaiterModal = false;
  updateTableRequest: CreateTableRequestDto = {};
  addWaiterRequest: TableWaiterRequestDto = {};
  removeWaiterRequest: TableWaiterRequestDto = {};


  constructor(
    private activeRoute: ActivatedRoute,
    private router: Router,
    private tableService: TableService,
    private hallService: HallService,
    private userService: UserService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    this.activeRoute.params.subscribe(params => {
      this.tableId = params['id'];
      this.pageSubject.next({
        tableState: DataState.LOADING_STATE, table: this.tableSubject.value,
        usersState: DataState.LOADING_STATE, users: this.usersSubject.value
      });
      this.tableService.findById$(this.tableId).pipe(
        tap(response => {
          this.tableSubject.next(response.data.table);
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADING_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
        }),
        concatMap(() => {
          return this.tableService.tableWaiters$(this.tableId);
        }),
        concatMap(response => {
          this.waitersSubject.next(response.data.users);
          return this.userService.allActiveUsers$
        }),
        map(response => {
          this.usersSubject.next(response.data.users);
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
        }),
        catchError(error => {
          let errorResponse = error.error;
          this.pageSubject.next({
            tableState: DataState.ERROR_STATE, usersState: DataState.ERROR_STATE,
            table: this.tableSubject.value, users: this.usersSubject.value,
            message: errorResponse.message
          });
          return of({});
        })).subscribe();
    });

    this.hallService.activeHalls$.pipe(
      map(response => {
        this.halls = response.data.halls;
      })
    ).subscribe();
  }

  onActivate(): void {
    this.tableService.activateTable$(this.tableId).pipe(
      map(response => {
        this.tableSubject.next(response.data.table);
        this.pageSubject.next({
          tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
          usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
          waiters: this.waitersSubject.value
        });
      }),
      catchError(error => {
        let errorResponse = error.error;
        if (error.status === 403) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: 'Not enough authorities'
          })
        } else if (error.status === 400) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: errorResponse.message
          })
        } else {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: error.message
          })
        }
        return null;
      })).subscribe();
  }

  onDeactivate(): void {
    this.tableService.deactivateTable$(this.tableId).pipe(
      map(response => {
        this.tableSubject.next(response.data.table);
        this.pageSubject.next({
          tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
          usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
          waiters: this.waitersSubject.value
        });
      }),
      catchError(error => {
        let errorResponse = error.error;
        if (error.status === 403) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: 'Not enough authorities'
          })
        } else if (error.status === 400) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: errorResponse.message
          })
        } else {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: error.message
          })
        }
        return null;
      })).subscribe();
  }

  onDelete(): void {
    this.tableService.deleteTable$(this.tableId).pipe(
      map(response => {
        this.router.navigate(['expeditions']);
      }),
      catchError(error => {
        let errorResponse = error.error;
        if (error.status === 403) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: 'Not enough authorities'
          })
        } else if (error.status === 400) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: errorResponse.message
          })
        } else {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: error.message
          })
        }
        return null;
      })).subscribe();
  }

  onShowTableModal(): void {
    const table = this.tableSubject.value;
    this.updateTableRequest = {
      capacity: table.capacity,
      hallId: table.hallId,
      name: table.name,
      password: '',
      tableType: table.tableType
    };
    this.showTableModal = true;
  }

  onCloseTableModal(): void {
    this.showTableModal = false;
    this.updateTableState$ = of({});
  }

  onUpdate(): void {
    if (this.updateTableRequest.password === '') {
      this.updateTableRequest.password = null;
    }
    this.updateTableState$ = this.tableService.updateTable$(this.tableId, ObjectUtils.replaceEmptyWithNull(this.updateTableRequest))
      .pipe(
        map(response => {
          this.tableSubject.next(response.data.table);
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
          this.onCloseTableModal();
          return {
            dataState: DataState.LOADED_STATE, table: response.data.table
          }

        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError(error => {
          let errorResponse = error.error;
          if (error.status === 422) {
            return of({
              dataState: DataState.LOADED_STATE, invalid: true,
              violations: errorResponse.data
            });
          } else if (error.status === 403) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: 'Not enough authorities'
            })
          } else if (error.status === 400) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: errorResponse.message
            })
          } else {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: error.message
            })
          }
        })
      );
  }

  onHallClick(): void {
    if (this.tableSubject.value.hallId) {
      this.router.navigate([`camps/${this.tableSubject.value.hallId}`]);
    }
  }

  onOrderClick(): void {
    if (this.tableSubject.value.orderId) {
      this.router.navigate([`equipments/${this.tableSubject.value.orderId}`]);
    }
  }

  clearMessage(): void {
    this.pageSubject.next({
      tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
      usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
      waiters: this.waitersSubject.value
    });
  }

  deletePopUp() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete expedition?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.onDelete();
      },
      reject: () => {

      }
    });
  }

  userRoute(user: UserDto) {
    this.router.navigate([`users/${user.id}`]);
  }

  onAddWaiter() {
    this.addWaiterState$ = this.tableService.addWaiter$(this.tableId, this.addWaiterRequest)
      .pipe(
        concatMap(response => {
          this.tableSubject.next(response.data.table);
          return this.userService.userById$(this.addWaiterRequest.waiterId);
        }),
        map(response => {
          const waiters = this.waitersSubject.value;
          if (!waiters.includes(response.data.user)) waiters.push(response.data.user);
          this.waitersSubject.next(waiters);
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
          this.onCloseWaiterModal();
          return {
            dataState: DataState.LOADED_STATE
          }
        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError(error => {
          let errorResponse = error.error;
          if (error.status === 422) {
            return of({
              dataState: DataState.LOADED_STATE, invalid: true,
              violations: errorResponse.data
            });
          } else if (error.status === 403) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: 'Not enough authorities'
            })
          } else if (error.status === 400) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: errorResponse.message
            })
          } else {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: error.message
            })
          }
        })
      )
  }

  onShowWaiterModal() {
    this.waiters = this.usersSubject.value.filter(waiter => !this.waitersSubject.value.map(user => user.id).includes(waiter.id));
    this.showWaiterModal = true;
    this.addWaiterRequest = {};
  }

  onCloseWaiterModal() {
    this.showWaiterModal = false;
    this.addWaiterState$ = of({});
  }

  onRemoveWaiterModal() {
    this.waiters = this.pageSubject.value.waiters;
    this.removeWaiterModal = true;
    this.removeWaiterRequest = {};
  }

  onCloseRemoveModal() {
    this.removeWaiterModal = false;
    this.removeWaiterState$ = of({});
  }

  onRemoveWaiter() {
    this.removeWaiterState$ = this.tableService.removeWaiter$(this.tableId, this.removeWaiterRequest.waiterId)
      .pipe(
        map(response => {
          this.tableSubject.next(response.data.table);
          this.waitersSubject.next(this.usersSubject.value.filter(user => this.tableSubject.value.waitersId.includes(user.id)));
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
          this.onCloseRemoveModal();
          return {
            dataState: DataState.LOADED_STATE
          }
        }),
        startWith({dataState: DataState.LOADING_STATE}),
        catchError(error => {
          let errorResponse = error.error;
          if (error.status === 422) {
            return of({
              dataState: DataState.LOADED_STATE, invalid: true,
              violations: errorResponse.data
            });
          } else if (error.status === 403) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: 'Not enough authorities'
            })
          } else if (error.status === 400) {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: errorResponse.message
            })
          } else {
            this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: error.message
            })
          }
        })
      )
  }

  onRemoveWaiters() {
    this.tableService.removeWaiters$(this.tableId)
      .pipe(
        map(response => {
          this.tableSubject.next(response.data.table);
          this.waitersSubject.next([]);
          this.pageSubject.next({
            tableState: DataState.LOADED_STATE, table: this.tableSubject.value,
            usersState: DataState.LOADED_STATE, users: this.usersSubject.value,
            waiters: this.waitersSubject.value
          });
        }),
        catchError(error => {
          let message;
          if (error.error != null) message = error.error.message;
          else if (error.status === 403) message = 'Not enough authorities';
          else message = error.message;
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: message});
          return of();
        })).subscribe();
  }

  onCreateOrder() {
    this.tableService.openOrderOnTableById$(this.tableId).pipe(
      map(response => {
        const table = this.tableSubject.value;
        table.orderId = response.data.order.id;
        this.tableSubject.next(table);
      }),
      catchError(error => {
        let errorResponse = error.error;
        if (error.status === 403) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: 'Not enough authorities'
          })
        } else if (error.status === 400) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: errorResponse.message
          })
        } else {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: error.message});
          return of({
            dataState: DataState.ERROR_STATE, invalid: false,
            message: error.message
          })
        }
      })).subscribe();
  }
}
