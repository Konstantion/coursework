import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {OrderService} from 'src/app/services/order/order.service';
import {TableService} from 'src/app/services/table/table.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class AdminComponent {
  constructor(
    private router: Router,
    private orderService: OrderService,
    private tableService: TableService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }
}
