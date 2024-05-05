import {Component, Input, OnInit} from '@angular/core';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, Observable, of} from 'rxjs';
import {UserDto} from 'src/app/models/dto/user/user-dto';
import {CallResponse} from 'src/app/models/responses/call-response';
import {DataState} from 'src/app/models/state/enum/data-state';
import {CallPageState} from 'src/app/models/state/pages/call-page-state';
import {CallService} from 'src/app/services/call/call.service';

@Component({
  selector: 'app-calls',
  templateUrl: './calls.component.html',
  styleUrls: ['./calls.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class CallsComponent implements OnInit {
  @Input() user: UserDto;
  readonly DataState = DataState;
  overlayVisible = false;
  private callsSubject = new BehaviorSubject<CallPageState>({dataState: DataState.LOADING_STATE});
  pageState$ = this.callsSubject.asObservable();

  constructor(
    private callsService: CallService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    let observable: Observable<CallResponse>;
    if (this.user.roles.includes('ADMIN')) observable = this.callsService.calls$;
    else observable = this.callsService.activeCalls$;
    observable.pipe(
      map(response => {
        this.callsSubject.next({dataState: DataState.LOADED_STATE, calls: response.data.calls});
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  toggle() {
    this.overlayVisible = !this.overlayVisible;
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
}
