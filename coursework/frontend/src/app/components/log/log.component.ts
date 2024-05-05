import {Component} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, concatMap, map, of} from 'rxjs';
import {DataState} from 'src/app/models/state/enum/data-state';
import {BillPageState} from 'src/app/models/state/pages/bill-page-state';
import {BillService} from 'src/app/services/bill/bill.service';
import {UserService} from 'src/app/services/user/user.service';
import {GuestService} from 'src/app/services/guest/guest.service';
import {UserResponse} from 'src/app/models/responses/user-response';
import {GuestResponse} from 'src/app/models/responses/guest-response';
import {ProductDto} from 'src/app/models/dto/product/product-dto';
import {OrderService} from 'src/app/services/order/order.service';
import {ProductResponse} from 'src/app/models/responses/product-response';

@Component({
  selector: 'app-log',
  templateUrl: './log.component.html',
  styleUrls: ['./log.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class LogComponent {
  constructor(
    private billService: BillService,
    private userService: UserService,
    private guestService: GuestService,
    private orderService: OrderService,
    private activeRoute: ActivatedRoute,
    private router: Router,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  private billId = '';
  private billPageSubject = new BehaviorSubject<BillPageState>({});

  pageState$ = this.billPageSubject.asObservable();

  products: Map<ProductDto, number>;

  readonly DataState = DataState;

  ngOnInit(): void {
    this.billId = this.activeRoute.snapshot.paramMap.get('id');
    this.billPageSubject.next({dataState: DataState.LOADING_STATE});

    this.billService.billById$(this.billId).pipe(
      concatMap(response => {
        const state = this.billPageSubject.value;
        state.bill = response.data.bill;
        state.dataState = DataState.LOADED_STATE;
        this.billPageSubject.next(state);

        const waiterId = response.data.bill.waiterId;
        if (waiterId) return this.userService.userById$(waiterId);
        else return of<UserResponse>(null);
      }),
      concatMap(userResponse => {
        const state = this.billPageSubject.value;
        if (userResponse !== null) {
          state.waiter = userResponse.data.user;
          state.dataState = DataState.LOADED_STATE;
          this.billPageSubject.next(state);
        }

        const guestId = state.bill.guestId;
        if (guestId) return this.guestService.guestById$(guestId);
        else return of<GuestResponse>(null);
      }),
      concatMap(guestResponse => {
        const state = this.billPageSubject.value;
        if (guestResponse !== null) {
          state.guest = guestResponse.data.guest;
          state.dataState = DataState.LOADED_STATE;
          this.billPageSubject.next(state);
        }

        const orderId = state.bill.orderId;
        if (orderId) return this.orderService.orderProductsById$(orderId);
        else return of<ProductResponse>(null)
      }),
      map(productsResponse => {
        const state = this.billPageSubject.value;
        state.products = this.getProductsMapFromList(productsResponse.data.products.content);
        state.dataState = DataState.LOADED_STATE;

        this.billPageSubject.next(state);
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

  onGuest() {
    const guestId = this.billPageSubject.value.bill.guestId;
    if (guestId) this.router.navigate([`guests/${guestId}`]);
  }

  onOrder() {
    const orderId = this.billPageSubject.value.bill.orderId;

    if (orderId) {
      this.router.navigate([`equipments/${orderId}`])
    }
  }

  onActivate() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to activate log?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.activate();
      },
      reject: () => {

      }
    });
  }

  activate() {
    this.billService.activateBillById$(this.billId).pipe(
      map(response => {
        const state = this.billPageSubject.value;
        state.bill = response.data.bill;

        this.billPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe()
  }

  onClose() {
    this.billService.closeBillById$(this.billId).pipe(
      map(response => {
        const state = this.billPageSubject.value;
        state.bill = response.data.bill;

        this.billPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe()
  }


  onDelete() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete log?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.delete();
      },
      reject: () => {

      }
    });
  }

  delete() {
    this.billService.deleteBillById$(this.billId).pipe(
      map(response => {
        const state = this.billPageSubject.value;
        if (state.bill.orderId) this.router.navigate([`equipments/${state.bill.orderId}`]);
        else this.router.navigate([`expeditions`])
      }),
      catchError(error => this.handleError(error))
    ).subscribe()
  }

  onGenerate() {
    this.billService.generatePdfByBillId$(this.billId)
      .subscribe(response => {
        this.downloadFile(response);
        this.openFile(response);
      });
  }

  downloadFile(data: Blob) {
    const downloadLink = document.createElement('a');
    downloadLink.href = window.URL.createObjectURL(data);
    downloadLink.download = `log-${this.billId}.pdf`;
    downloadLink.click();
  }

  openFile(data: Blob) {
    const fileURL = URL.createObjectURL(data);
    window.open(fileURL);
  }

  getProductsMapFromList(products: ProductDto[]): Map<ProductDto, number> {
    const productIdsMap = new Map<string, number>();
    products.forEach(product => {
      if (productIdsMap.has(product.id)) {
        productIdsMap.set(product.id, productIdsMap.get(product.id) + 1);
      } else {
        productIdsMap.set(product.id, 1);
      }
    });
    const productsMap = new Map<ProductDto, number>();
    productIdsMap.forEach((quantity, id) => {
      const product = products.find(product => product.id == id);
      productsMap.set(product, quantity);
    })

    return productsMap;
  }

  onRow(id: string) {
    if (id) {
      this.router.navigate([`gears/${id}`]);
    }
  }
}
