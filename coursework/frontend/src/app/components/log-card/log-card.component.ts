import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BillDto} from 'src/app/models/dto/bill/bill-dto';

@Component({
  selector: 'app-log-card',
  templateUrl: './log-card.component.html',
  styleUrls: ['./log-card.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class LogCardComponent {
  @Input() bill: BillDto;
  @Output() onClick = new EventEmitter<string>();

  cardClick() {
    this.onClick.emit(this.bill.id);
  }
}
