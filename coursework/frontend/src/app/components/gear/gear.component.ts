import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {BehaviorSubject, catchError, concatMap, map, of} from 'rxjs';
import {CategoryDto} from 'src/app/models/dto/category/category-dto';
import {CreateProductRequestDto} from 'src/app/models/dto/product/create-product-dto';
import {ProductDto} from 'src/app/models/dto/product/product-dto';
import {CategoryResponse} from 'src/app/models/responses/category-response';
import {CreateProductState} from 'src/app/models/state/crud/create-product-state';
import {DataState} from 'src/app/models/state/enum/data-state';
import {ProductPageState} from 'src/app/models/state/pages/product-page-state';
import {CategoryService} from 'src/app/services/category/category.service';
import {ProductService} from 'src/app/services/product/product.service';

@Component({
  selector: 'app-product',
  templateUrl: './gear.component.html',
  styleUrls: ['./gear.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class GearComponent implements OnInit {
  constructor(
    private activeRoute: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private categoryService: CategoryService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {
  }

  private productId: string;
  private productPageSubject = new BehaviorSubject<ProductPageState>({});
  private createSubject = new BehaviorSubject<CreateProductState>({});


  createState$ = this.createSubject.asObservable();
  pageState$ = this.productPageSubject.asObservable();

  showUpdateModal = false;
  categories: CategoryDto[] = [];

  productData: CreateProductRequestDto = {};
  updateProductData: CreateProductRequestDto = {};

  readonly DataState = DataState

  ngOnInit(): void {
    this.productId = this.activeRoute.snapshot.paramMap.get('id');
    this.productPageSubject.next({dataState: DataState.LOADING_STATE});
    this.productService.productById$(this.productId).pipe(
      concatMap(productResponse => {
        const state = this.productPageSubject.value;
        state.product = productResponse.data.product;
        state.imageUrl = this.imageUrl(productResponse.data.product);
        this.productPageSubject.next(state);
        if (productResponse.data.product.categoryId) {
          return this.categoryService.categoryById$(productResponse.data.product.categoryId)
        } else {
          return of<CategoryResponse>({data: {category: null}});
        }

      }),
      map(categoryResponse => {
        const state = this.productPageSubject.value;
        state.category = categoryResponse.data.category;
        state.dataState = DataState.LOADED_STATE;
        this.productPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onActivate() {
    this.productService.activateProductById$(this.productId).pipe(
      map(response => {
        const state = this.productPageSubject.value;
        state.product = response.data.product;
        state.imageUrl = this.imageUrl(response.data.product);
        this.productPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onDeactivate() {
    this.productService.deactivateProductById$(this.productId).pipe(
      map(response => {
        const state = this.productPageSubject.value;
        state.product = response.data.product;
        state.imageUrl = this.imageUrl(response.data.product);
        this.productPageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  generateData(): CreateProductRequestDto {
    const product = this.productPageSubject.value.product;
    return {...product}
  }

  onUpdate() {
    this.categoryService.categories$.pipe(
      map(response => {
        this.categories = response.data.categories;
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
    this.productData = this.generateData();
    this.createSubject.next({})
    this.showUpdateModal = true;
  }

  updateProduct() {
    this.productService.updateProductById$(this.productId, this.productData).pipe(
      map(response => {
        const state = this.productPageSubject.value;
        state.product = response.data.product;
        state.imageUrl = this.imageUrl(response.data.product);
        this.productPageSubject.next(state);
        this.closeModal();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.createSubject.next({invalid: true, violations: response.data})
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  closeModal() {
    this.showUpdateModal = false;
    this.productData = {};
  }

  onDelete() {
    this.confirmationService.confirm({
      target: event.target,
      message: 'Are you sure that you want to delete gear?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.delete();
      },
      reject: () => {

      }
    });
  }

  delete() {
    this.productService.deactivateProductById$(this.productId).pipe(
      map(response => {
        this.router.navigate([`gears`]);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  imageUrl(product: ProductDto): string {
    const arrayBuffer = product.imageBytes;
    const altUrl = 'https://via.placeholder.com/100';
    if (arrayBuffer === null) {
      return altUrl;
    }
    return `data:image/png;base64,${arrayBuffer}`
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

  onImageSelected(event: any) {
    if (event.target.files.length > 0) {
      this.productData.image = event.target.files[0];
    }
  }
}
