import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { BehaviorSubject, Observable, catchError, concatMap, map, of, tap } from 'rxjs';
import { CategoryDto } from 'src/app/models/dto/category/category-dto';
import { CreateProductRequestDto } from 'src/app/models/dto/product/create-product-dto';
import { ProductDto } from 'src/app/models/dto/product/product-dto';
import { CreateProductState } from 'src/app/models/state/crud/create-product-state';
import { DataState } from 'src/app/models/state/enum/data-state';
import { FindProductsState } from 'src/app/models/state/crud/find-products-state';
import { ProductsPageState } from 'src/app/models/state/pages/products-page-state';
import { CategoryService } from 'src/app/services/category/category.service';
import { ProductService } from 'src/app/services/product/product.service';
import { ProductResponse } from 'src/app/models/responses/product-response';

@Component({
  selector: 'app-products',
  templateUrl: './gears.component.html',
  styleUrls: ['./gears.component.css'],
  providers: [ConfirmationService, MessageService]
})
export class GearsComponent implements OnInit {
  @Input() border: boolean = false;
  @Input() size: number = null;
  @Input() isAdmin: boolean;
  @Output() productClick = new EventEmitter<string>();


  constructor(
    private confirmationService: ConfirmationService,
    private messageService: MessageService,
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router
  ) { }

  private pageSubject = new BehaviorSubject<ProductsPageState>({ dataState: DataState.LOADING_STATE });
  private createSubject = new BehaviorSubject<CreateProductState>({});
  private searchInputTimeout: any;

  pageState$ = this.pageSubject.asObservable();
  createState$ = this.createSubject.asObservable();

  productId = '';
  categories: CategoryDto[] = [];
  showCreateModal = false;
  findProductsState: FindProductsState = { page: 1, size: 8, category: "", pattern: "" };
  productData: CreateProductRequestDto = {
    name: '',
    price: 0,
    weight: 0,
    image: null,
    description: '',
    categoryId: ''
  };

  readonly DataState = DataState;

  ngOnInit(): void {
    if (this.size) {
      this.findProductsState.size = this.size;
    }
    this.categoryService.categories$.pipe(
      concatMap(response => {
        const state = this.pageSubject.value;
        state.categories = response.data.categories;
        this.categories = response.data.categories;
        this.pageSubject.next(state);
        if (this.isAdmin) return this.productService.products$(this.findProductsState);
        else return this.productService.activeProducts$(this.findProductsState);
      }),
      map(response => {
        this.findProductsState.page = response.data.products.number + 1;
        const state = this.pageSubject.value;
        state.page = response.data.products;
        state.pages = this.pages(response.data.products.totalPages);
        state.dataState = DataState.LOADED_STATE;
        this.pageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  onSearch() {
    this.findProductsState.page = 1;
    this.fetchProducts();
  }

  updatePattern() {

    clearTimeout(this.searchInputTimeout);
    this.searchInputTimeout = setTimeout(() => {
      this.findProductsState.page = 1;
      this.fetchProducts();
    }, 400);
  }

  closeModal() {
    this.showCreateModal = false;
    this.productData = {
      name: '',
      price: 0,
      weight: 0,
      image: null,
      description: '',
      categoryId: ''
    };

  }

  showModal() {
    this.createSubject.next({ dataState: DataState.LOADED_STATE })
    this.showCreateModal = true;
  }

  createProduct() {
    this.productService.createProduct$(this.productData).pipe(
      map(response => {
        this.closeModal();
        return this.fetchProducts();
      }),
      catchError(error => {
        if (error.status === 422) {
          const response = error.error;
          this.createSubject.next({ invalid: true, violations: response.data })
          return of({});
        } else {
          return this.handleError(error);
        }
      })
    ).subscribe();
  }

  fetchProducts() {
    const state = this.pageSubject.value;
    state.dataState = DataState.LOADING_STATE;
    this.pageSubject.next(state);
    let observable: Observable<ProductResponse>;

    if (this.isAdmin) observable = this.productService.products$(this.findProductsState);
    else observable = this.productService.activeProducts$(this.findProductsState);

    observable.pipe(
      map(response => {
        this.findProductsState.page = response.data.products.number + 1;
        const state = this.pageSubject.value;
        state.page = response.data.products;
        state.pages = this.pages(response.data.products.totalPages);
        state.dataState = DataState.LOADED_STATE;
        this.pageSubject.next(state);
      }),
      catchError(error => this.handleError(error))
    ).subscribe();
  }

  handleError(error: any) {
    let errorResponse = error.error;
    if (error.status === 403) {
      this.messageService.add({ severity: 'error', summary: 'Rejected', detail: 'Not enough authorities' });
    } else if (error.status === 400) {
      this.messageService.add({ severity: 'error', summary: 'Rejected', detail: errorResponse.message });
    } else {
      this.messageService.add({ severity: 'error', summary: 'Rejected', detail: error.message });
    }
    return of();
  }

  pages(count: number): number[] {
    let pages: number[] = [];
    for (let i = 1; i <= count; i++) {
      pages.push(i);
    }
    return pages;
  }

  previousPage() {
    this.findProductsState.page--;
    this.fetchProducts();
  }

  nextPage() {
    this.findProductsState.page++;
    this.fetchProducts();
  }

  updatePage(page: number) {
    this.findProductsState.page = page;
    this.fetchProducts();
  }

  onImageSelected(event: any) {
    if (event.target.files.length > 0) {
      const file = event.target.files[0];
      this.productData.image = file;
    }
  }

  getCategory(product: ProductDto): CategoryDto {
    return this.categories.find(c => c.id === product.categoryId);
  }

  onCard(id: string) {
    this.productId = id;
    if (!this.border) {
      this.router.navigate([`gears/${id}`]);
    }
    this.productClick.emit(id);
  }
}
