import {Component, Input} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {TableDto} from 'src/app/models/dto/table/table-dto';

@Component({
  selector: 'app-expedition-card',
  templateUrl: './expedition-card.component.html',
  styleUrls: ['./expedition-card.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class ExpeditionCardComponent {
  @Input() table: TableDto;

  constructor(
    private router: Router
  ) {
  }

  onCardClick() {
    console.log(this.table.id);
    this.router.navigate([`expeditions/${this.table.id}`]);
  }
}
