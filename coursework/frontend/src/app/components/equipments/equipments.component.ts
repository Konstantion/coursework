import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, Observable, of} from 'rxjs';
import {OrderResponse} from 'src/app/models/responses/order-response';
import {DataState} from 'src/app/models/state/enum/data-state';
import {OrdersPageState} from 'src/app/models/state/pages/orders-page-state';
import {OrderService} from 'src/app/services/order/order.service';
import {TableService} from 'src/app/services/table/table.service';

@Component({
  selector: 'app-equipments',
  templateUrl: './equipments.component.html',
  styleUrls: ['./equipments.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class EquipmentsComponent implements OnInit {
  @Input() isAdmin: boolean;
  onlyInactive = false;
  readonly DataState = DataState;
  private ordersPageSubject = new BehaviorSubject<OrdersPageState>({
    tableNames: new Map(),
    dataState: DataState.LOADING_STATE,
    inactive: false
  });
  pageState$ = this.ordersPageSubject.asObservable();

  constructor(
    private router: Router,
    private orderService: OrderService,
    private tableService: TableService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    let ordersObservable: Observable<OrderResponse>;
    if (this.isAdmin) ordersObservable = this.orderService.orders$;
    else ordersObservable = this.orderService.activeOrders$;

    ordersObservable.pipe(
      map(response => {
        const state = this.ordersPageSubject.value;
        state.dataState = DataState.LOADED_STATE;
        state.orders = response.data.orders;
        for (let order of response.data.orders) {
          if (order.tableId && !state.tableNames.has(order.tableId)) {
            this.tableService.findById$(order.tableId).subscribe(response => {
              const table = response.data.table;
              state.tableNames.set(table.id, table);
            })
          }
        }
        this.ordersPageSubject.next(state);
      }),
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

  onCard(id: string) {
    this.router.navigate([`equipments/${id}`]);
  }
}
