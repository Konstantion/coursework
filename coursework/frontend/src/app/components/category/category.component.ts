import {Component} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, map, of} from 'rxjs';
import {CreateCategoryRequestDto} from 'src/app/models/dto/category/create-category-request-dto';
import {CreateCategoryState} from 'src/app/models/state/crud/create-category-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {CategoryPageState} from 'src/app/models/state/pages/category-page-state';
import {CategoryService} from 'src/app/services/category/category.service';

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class CategoryComponent {
  updateCategoryData: CreateCategoryRequestDto = {};
  update = false;
  readonly DataState = DataState;
  private categoryId = '';
  private categoryPageSubject = new BehaviorSubject<CategoryPageState>({});
  pageState$ = this.categoryPageSubject.asObservable();
  private updateCategorySubject = new BehaviorSubject<CreateCategoryState>({});
  updateState$ = this.updateCategorySubject.asObservable();

  constructor(
    private categoryService: CategoryService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private router: Router,
    private activeRoute: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.categoryId = this.activeRoute.snapshot.paramMap.get('id');

    this.categoryService.categoryById$(this.categoryId).pipe(
      map(response => {
        const state = this.categoryPageSubject.value;
        state.category = response.data.category;
        state.dataState = DataState.LOADED_STATE;

        this.categoryPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onUpdate() {
    this.updateCategoryData = {...this.categoryPageSubject.value.category};
    this.updateCategorySubject.next({});
    this.update = true;
  }

  onDelete() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete category?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.delete();
      },
      reject: () => {

      }
    });
  }

  delete() {
    this.categoryService.deleteCategoryById$(this.categoryId)
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

  updateCategory() {
    this.categoryService.updateCategoryById$(this.categoryId, this.updateCategoryData).pipe(
      map(response => {
        const state = this.categoryPageSubject.value;
        state.category = response.data.category;

        this.categoryPageSubject.next(state);

        this.onClose();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.updateCategorySubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  onCreator() {
    let creatorId = this.categoryPageSubject.value.category.creatorId;
    if (creatorId) this.router.navigate([`users/${creatorId}`]);
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
