import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {UserDto} from 'src/app/models/dto/user/user-dto';

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class UserCardComponent {
  @Input() user: UserDto;

  @Output() onClick = new EventEmitter<UserDto>()

  onCardClick() {
    this.onClick.emit(this.user);
  }
}
