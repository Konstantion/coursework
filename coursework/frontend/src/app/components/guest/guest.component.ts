import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, of} from 'rxjs';
import {UpdateGuestRequestDto} from 'src/app/models/dto/guest/update-guest-request-dto';
import {UpdateGuestState} from 'src/app/models/state/crud/update-guest-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {GuestPageState} from 'src/app/models/state/pages/guest-page-state';
import {GuestService} from 'src/app/services/guest/guest.service';

@Component({
  selector: 'app-guest',
  templateUrl: './guest.component.html',
  styleUrls: ['./guest.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class GuestComponent implements OnInit {
  updateGuestData: UpdateGuestRequestDto = {};
  update = false;
  readonly DataState = DataState;
  private guestId = '';
  private guestPageSubject = new BehaviorSubject<GuestPageState>({dataState: DataState.LOADING_STATE});
  pageState$ = this.guestPageSubject.asObservable();
  private updateGuestSubject = new BehaviorSubject<UpdateGuestState>({});
  updateState$ = this.updateGuestSubject.asObservable();

  constructor(
    private guestService: GuestService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router,
    private activeRoute: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.guestId = this.activeRoute.snapshot.paramMap.get('id');

    this.guestService.guestById$(this.guestId).pipe(
      map(response => {
        const state = this.guestPageSubject.value;
        state.guest = response.data.guest;
        state.dataState = DataState.LOADED_STATE;

        this.guestPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onUpdate() {
    this.updateGuestData = {...this.guestPageSubject.value.guest};
    this.updateGuestSubject.next({});
    this.update = true;
  }

  onActivate() {
    this.guestService.activateGuestById$(this.guestId)
      .pipe(
        map(response => {
          const state = this.guestPageSubject.value;
          state.guest = response.data.guest;
          state.dataState = DataState.LOADED_STATE;

          this.guestPageSubject.next(state);
        }),
        catchError(error => this.handleError(error))
      ).subscribe();
  }

  onDeactivate() {
    this.guestService.deactivateGuestById$(this.guestId)
      .pipe(
        map(response => {
          const state = this.guestPageSubject.value;
          state.guest = response.data.guest;
          state.dataState = DataState.LOADED_STATE;

          this.guestPageSubject.next(state);
        }),
        catchError(error => this.handleError(error))
      ).subscribe();
  }

  onDelete() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete guest?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.delete();
      },
      reject: () => {

      }
    });
  }

  delete() {
    this.guestService.deleteGuestById$(this.guestId)
      .pipe(
        map(response => {
          this.router.navigate([`tables`]);
        }),
        catchError(error => this.handleError(error))
      ).subscribe();
  }

  onClose() {
    this.update = false;
  }

  updateGuest() {
    this.guestService.updateGuestById$(this.guestId, this.updateGuestData).pipe(
      map(response => {
        const state = this.guestPageSubject.value;
        state.guest = response.data.guest;

        this.guestPageSubject.next(state);

        this.onClose();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.updateGuestSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
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
}
