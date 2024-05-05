import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {GuestDto} from 'src/app/models/dto/guest/guest-dto';

@Component({
  selector: 'app-guest-card',
  templateUrl: './guest-card.component.html',
  styleUrls: ['./guest-card.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class GuestCardComponent {
  @Input() guest: GuestDto;

  @Output() onClick = new EventEmitter<string>();

  constructor() {
  }

  cardClick() {
    this.onClick.emit(this.guest.id);
  }
}
