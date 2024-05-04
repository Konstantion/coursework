import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, concatMap, map, Observable, of, startWith, tap} from 'rxjs';
import {CreateHallRequestDto} from 'src/app/models/dto/hall/create-hall-request-dto';
import {HallDto} from 'src/app/models/dto/hall/hall-dto';
import {TableDto} from 'src/app/models/dto/table/table-dto';
import {CreateHallState} from 'src/app/models/state/crud/create-hall-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {HallPageState} from 'src/app/models/state/pages/hall-page-state';
import {ObjectUtils} from 'src/app/models/util/object-utils';
import {HallService} from 'src/app/services/hall/hall.service';

@Component({
  selector: 'app-hall',
  templateUrl: './camp.component.html',
  styleUrls: ['./camp.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class CampComponent implements OnInit {
  private hallId: string;
  private tablesSubject = new BehaviorSubject<TableDto[]>(null);
  private hallSubject = new BehaviorSubject<HallDto>(null);
  private pageSubject = new BehaviorSubject<HallPageState>({});

  readonly DataState = DataState;

  pageState$: Observable<HallPageState> = this.pageSubject.asObservable();
  updateHallState$: Observable<CreateHallState> = of({});

  showHallModal = false;
  updateHallRequest: CreateHallRequestDto = {};

  constructor(
    private hallService: HallService,
    private activeRoute: ActivatedRoute,
    private router: Router,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    this.hallId = this.activeRoute.snapshot.paramMap.get('id');
    this.hallService.hall$(this.hallId)
      .pipe(
        tap(response => {
          this.hallSubject.next(response.data.hall);
          this.pageSubject.next({
            tablesState: DataState.LOADING_STATE, hallState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value
          });
        }),
        concatMap(
          () => {
            return this.hallService.hallTables$(this.hallSubject.value.id)
          }),
        map(response => {
          this.tablesSubject.next(response.data.tables);
          this.pageSubject.next({
            tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value
          });
        }),
        catchError(error => {
          let errorResponse = error.error;
          this.pageSubject.next({
            tablesState: DataState.ERROR_STATE, hallState: DataState.ERROR_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value,
            message: errorResponse.message
          });
          return of({});
        })).subscribe();
  }

  onDelete(): void {
    this.hallService.deleteHall$(this.hallId).pipe(
      map(response => {
        this.router.navigate(['expeditions']);
      }),
      catchError(error => {
        let errorResponse = error.error;
        if (error.status === 403) {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: 'Not enough authorities'});
          this.pageSubject.next({
            tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value,
            message: 'Not enough authorities'
          })
        } else {
          this.messageService.add({severity: 'error', summary: 'Rejected', detail: errorResponse.message});
          this.pageSubject.next({
            tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value,
            message: errorResponse.message
          })
        }
        return null;
      })).subscribe();
  }

  onActivate(): void {
    this.hallService.activateHall$(this.hallId).pipe(
      map(response => {
        this.hallSubject.next(response.data.hall);
        this.pageSubject.next({
          tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
          tables: this.tablesSubject.value, hall: this.hallSubject.value
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
      })).subscribe();
  }

  onDeactivate(): void {
    this.hallService.deactivateHall$(this.hallId).pipe(
      map(response => {
        this.hallSubject.next(response.data.hall);
        this.pageSubject.next({
          tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
          tables: this.tablesSubject.value, hall: this.hallSubject.value
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
      })).subscribe();
  }


  onShowHallModal(): void {
    this.updateHallRequest.name = this.hallSubject.value.name;
    this.showHallModal = true;
  }

  onCloseHallModal(): void {
    this.showHallModal = false;
    this.updateHallState$ = of({});
  }

  updateHall(): void {
    this.updateHallState$ = this.hallService.updateHall$(this.hallId, ObjectUtils.replaceEmptyWithNull(this.updateHallRequest))
      .pipe(
        map(response => {
          this.updateHallRequest = {};
          this.hallSubject.next(response.data.hall);
          this.pageSubject.next({
            tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, hall: this.hallSubject.value
          })
          this.onCloseHallModal();
          return {
            dataState: DataState.LOADED_STATE, hall: response.data.hall
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

  clearMessage(): void {
    this.pageSubject.next({
      tablesState: DataState.LOADED_STATE, hallState: DataState.LOADED_STATE,
      tables: this.tablesSubject.value, hall: this.hallSubject.value
    })
  }

  deletePopUp() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete camp?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.onDelete();
      },
      reject: () => {

      }
    });
  }
}
