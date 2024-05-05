import {Component} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';

@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class SpinnerComponent {

}
