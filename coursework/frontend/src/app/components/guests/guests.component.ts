import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, Observable, of, startWith} from 'rxjs';
import {CreateGuestRequestDto} from 'src/app/models/dto/guest/create-guest-request-dto';
import {GuestResponse} from 'src/app/models/responses/guest-response';
import {UpdateGuestState} from 'src/app/models/state/crud/update-guest-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {GuestsPageState} from 'src/app/models/state/pages/guests-page-state';
import {ObjectUtils} from 'src/app/models/util/object-utils';
import {GuestService} from 'src/app/services/guest/guest.service';

@Component({
  selector: 'app-guests',
  templateUrl: './guests.component.html',
  styleUrls: ['./guests.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class GuestsComponent implements OnInit {
  @Input() isAdmin: boolean;
  createGuestData: CreateGuestRequestDto = {};
  onlyInactive = false;
  create = false;
  readonly DataState = DataState;
  private guestsPageSubject = new BehaviorSubject<GuestsPageState>({});
  pageState$ = this.guestsPageSubject.asObservable();
  private createGuestSubject = new BehaviorSubject<UpdateGuestState>({});
  createState$ = this.createGuestSubject.asObservable();

  constructor(
    private router: Router,
    private guestService: GuestService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  ngOnInit(): void {
    let observable: Observable<GuestResponse>;
    if (this.isAdmin) observable = this.guestService.guests$;
    else observable = this.guestService.activeGuests$;

    observable.pipe(
      map(response => {
        const state = this.guestsPageSubject.value;
        state.guests = response.data.guests;
        state.dataState = DataState.LOADED_STATE;
      }),
      startWith(this.guestsPageSubject.next({dataState: DataState.LOADING_STATE})),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onCreate() {
    this.createGuestData = {};
    this.createGuestSubject.next({});
    this.create = true;
  }

  createGuest() {
    this.guestService.createGuest$(ObjectUtils.replaceEmptyWithNull(this.createGuestData)).pipe(
      map(response => {
        const state = this.guestsPageSubject.value;
        state.guests.push(response.data.guest);

        this.guestsPageSubject.next(state);

        this.onClose();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.createGuestSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  onClose() {
    this.create = false;
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
    this.router.navigate([`guests/${id}`]);
  }
}
