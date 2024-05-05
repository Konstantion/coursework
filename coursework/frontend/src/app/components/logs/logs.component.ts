import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, Observable, of} from 'rxjs';
import {BillResponse} from 'src/app/models/responses/bill-response';
import {DataState} from 'src/app/models/state/enum/data-state';
import {BillsPageState} from 'src/app/models/state/pages/bills-page-state';
import {BillService} from 'src/app/services/bill/bill.service';
import {OrderService} from 'src/app/services/order/order.service';

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class LogsComponent implements OnInit {
  @Input() isAdmin: boolean;
  onlyInactive = false;
  readonly DataState = DataState;
  private ordersPageSubject = new BehaviorSubject<BillsPageState>({
    dataState: DataState.LOADING_STATE,
    inactive: false
  });
  pageState$ = this.ordersPageSubject.asObservable();

  constructor(
    private router: Router,
    private orderService: OrderService,
    private billService: BillService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    let billsObservable: Observable<BillResponse>;
    if (this.isAdmin) billsObservable = this.billService.bills$;
    else billsObservable = this.billService.activeBills$;

    billsObservable.pipe(
      map(response => {
        const state = this.ordersPageSubject.value;
        state.dataState = DataState.LOADED_STATE;
        state.bills = response.data.bills;
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
    this.router.navigate([`logs/${id}`]);
  }
}
