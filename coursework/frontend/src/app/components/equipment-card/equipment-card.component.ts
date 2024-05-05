import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {OrderDto} from 'src/app/models/dto/order/order-dto';
import {TableDto} from 'src/app/models/dto/table/table-dto';
import {ObjectUtils} from 'src/app/models/util/object-utils';

@Component({
  selector: 'app-equipment-card',
  templateUrl: './equipment-card.component.html',
  styleUrls: ['./equipment-card.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class EquipmentCardComponent {
  @Input() order: OrderDto;
  @Input() table: TableDto;

  @Output() onClick = new EventEmitter<string>();

  constructor() {
  }

  cardClick() {
    this.onClick.emit(this.order.id);
  }

  dateString(): string {
    return ObjectUtils.formatDate(this.order.createdAt);
  }
}

