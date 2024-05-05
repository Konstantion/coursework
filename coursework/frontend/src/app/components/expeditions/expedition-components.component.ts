import {Component, Input, OnInit} from '@angular/core';
import {HallService} from 'src/app/services/hall/hall.service';
import {TableService} from 'src/app/services/table/table.service';
import {BehaviorSubject, catchError, concatMap, map, Observable, of, startWith, tap} from 'rxjs';
import {HallDto} from 'src/app/models/dto/hall/hall-dto';
import {TableDto} from 'src/app/models/dto/table/table-dto';
import {DataState} from 'src/app/models/state/enum/data-state';
import {CreateTableRequestDto} from 'src/app/models/dto/table/create-table-request-dto';

import {TableResponse} from 'src/app/models/responses/table-response';
import {CreateHallRequestDto} from 'src/app/models/dto/hall/create-hall-request-dto';
import {CreateTableState} from 'src/app/models/state/crud/create-table-state';
import {CreateHallState} from 'src/app/models/state/crud/create-hall-state';
import {TablesPageState} from 'src/app/models/state/pages/tables-page-state';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {HallResponse} from 'src/app/models/responses/hall-response';

@Component({
  selector: 'app-expeditions',
  templateUrl: './expedition-components.component.html',
  styleUrls: ['./expedition-components.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class ExpeditionComponents implements OnInit {
  @Input() isAdmin: boolean;
  createTableState$: Observable<CreateTableState> = of({});
  createHallState$: Observable<CreateHallState> = of({});
  pageState$: Observable<TablesPageState>;
  showTableModal = false;
  showHallModal = false;
  createTableRequest: CreateTableRequestDto = {tableType: 'COMMON'};
  createHallRequest: CreateHallRequestDto = {};
  readonly DataState = DataState;
  private hallsSubject = new BehaviorSubject<HallDto[]>(null);
  private tablesSubject = new BehaviorSubject<TableDto[]>(null);
  private hallIdSubject = new BehaviorSubject<string>('');
  hallIdStatus$ = this.hallIdSubject.asObservable();

  constructor(
    private hallService: HallService,
    private tableService: TableService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    let observable: Observable<HallResponse>;
    if (this.isAdmin) observable = this.hallService.halls$;
    else observable = this.hallService.activeHalls$;
    this.pageState$ = observable.pipe(
      tap(response => {
        this.hallsSubject.next(response.data.halls);
        if (this.hallsSubject.value.length !== 0) {
          this.hallIdSubject.next(this.hallsSubject.value[0].id);
        }
      }),
      concatMap(() => {
        if (this.hallIdSubject.value === '') {
          const fakeResponse: TableResponse = {
            data: {
              tables: []
            }
          }
          return of(fakeResponse);
        }

        if (this.isAdmin) return this.hallService.hallTables$(this.hallIdSubject.value);
        else return this.hallService.hallActiveTables$(this.hallIdSubject.value)
      }),
      map(response => {
        this.tablesSubject.next(response.data.tables);
        return {
          tablesState: DataState.LOADED_STATE, hallsState: DataState.LOADED_STATE,
          tables: this.tablesSubject.value, halls: this.hallsSubject.value
        };
      }),
      startWith({tablesState: DataState.LOADING_STATE, hallsState: DataState.LOADING_STATE})
    );
  }

  getDataAsString(data: Date | undefined): string {
    if (data instanceof Date) {
      return data.toLocaleDateString('en-US', {year: 'numeric', month: 'long', day: 'numeric'});
    } else {
      return '';
    }
  }

  onSelectChange(id: string): void {
    this.hallIdSubject.next(id);
    this.pageState$ = this.hallService.hallTables$(this.hallIdSubject.value).pipe(
      map(response => {
        this.tablesSubject.next(response.data.tables);
        return {
          tablesState: DataState.LOADED_STATE, hallsState: DataState.LOADED_STATE,
          tables: this.tablesSubject.value, halls: this.hallsSubject.value
        };
      }),
      startWith({
        tablesState: DataState.LOADING_STATE, hallsState: DataState.LOADED_STATE,
        tables: [], halls: this.hallsSubject.value
      })
    );
  }

  onShowTableModal(): void {
    console.log('Show modal')
    this.showTableModal = true;
  }

  onCloseTableModal(): void {
    this.showTableModal = false;
    this.createTableState$ = of({});
  }

  createTable(): void {
    this.createTableRequest.hallId = this.hallIdSubject.value;
    this.createTableState$ = this.tableService.createTable$(this.createTableRequest)
      .pipe(
        map(response => {
          this.createTableRequest = {tableType: 'COMMON'};
          this.tablesSubject.value.push(response.data.table);
          this.pageState$ = of({
            tablesState: DataState.LOADED_STATE, hallsState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, halls: this.hallsSubject.value
          })
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
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: 'Not enough authorities'
            })
          } else if (error.status === 400) {
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: errorResponse.message
            })
          } else {
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: error.message
            })
          }
        })
      );
  }

  onShowHallModal(): void {
    this.showHallModal = true;
  }

  onCloseHallModal(): void {
    this.showHallModal = false;
    this.createHallState$ = of({});
  }

  createHall(): void {
    this.createHallState$ = this.hallService.createHall$(this.createHallRequest)
      .pipe(
        map(response => {
          this.createHallRequest = {};
          this.hallsSubject.value.push(response.data.hall);
          this.pageState$ = of({
            tablesState: DataState.LOADED_STATE, hallsState: DataState.LOADED_STATE,
            tables: this.tablesSubject.value, halls: this.hallsSubject.value
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
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: 'Not enough authorities'
            })
          } else if (error.status === 400) {
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: errorResponse.message
            })
          } else {
            return of({
              dataState: DataState.ERROR_STATE, invalid: false,
              message: error.message
            })
          }
        })
      );
  }

  onSelectedHallClick(): void {
    this.router.navigate([`camps/${this.hallIdSubject.value}`]);
  }
}
