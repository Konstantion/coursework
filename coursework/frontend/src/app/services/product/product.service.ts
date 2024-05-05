import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateProductRequestDto} from 'src/app/models/dto/product/create-product-dto';
import {ProductResponse} from 'src/app/models/responses/product-response';
import {FindProductsState} from 'src/app/models/state/crud/find-products-state';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private readonly productUrl = `${environment.api.baseUrl}/${environment.api.productUrl}`;
  private readonly adminProductUrl = `${environment.api.baseUrl}/${environment.api.adminProductUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  activeProducts$ = (state: FindProductsState) => {

    let params = new HttpParams();
    if (state.size) params = params.set('size', state.size);
    if (state.page) params = params.append('page', state.page);
    if (state.pattern) params = params.append('pattern', state.pattern);
    if (state.category) params = params.append('category', state.category);


    return <Observable<ProductResponse>>this.http.get(this.productUrl, {params}).pipe(tap(console.log))
  }

  products$ = (state: FindProductsState) => {

    let params = new HttpParams();
    if (state.size) params = params.set('size', state.size);
    if (state.page) params = params.append('page', state.page);
    if (state.pattern) params = params.append('pattern', state.pattern);
    if (state.category) params = params.append('category', state.category);


    return <Observable<ProductResponse>>this.http.get(this.adminProductUrl, {params}).pipe(tap(console.log))
  }

  productById$ = (id: string) => <Observable<ProductResponse>>this.http.get(`${this.productUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  activateProductById$ = (id: string) => <Observable<ProductResponse>>this.http.put(`${this.adminProductUrl}/${id}/activate`, '')
    .pipe(
      tap(console.log)
    )

  deactivateProductById$ = (id: string) => <Observable<ProductResponse>>this.http.put(`${this.adminProductUrl}/${id}/deactivate`, '')
    .pipe(
      tap(console.log)
    )

  deleteProductById$ = (id: string) => <Observable<ProductResponse>>this.http.delete(`${this.adminProductUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  updateProductById$ = (id: string, requestDto: CreateProductRequestDto) => {
    const formData = new FormData();
    if (requestDto.name) formData.append('name', requestDto.name);
    if (requestDto.price) formData.append('price', requestDto.price.toString());
    if (requestDto.weight) formData.append('weight', requestDto.weight.toString());
    if (requestDto.image) formData.append('image', requestDto.image);
    if (requestDto.description) formData.append('description', requestDto.description);
    if (requestDto.categoryId) formData.append('categoryId', requestDto.categoryId);

    return <Observable<ProductResponse>>this.http.put(`${this.adminProductUrl}/${id}`, formData)
      .pipe(
        tap(console.log)
      )
  }

  createProduct$ = (requestDto: CreateProductRequestDto) => {
    const formData = new FormData();
    if (requestDto.name) formData.append('name', requestDto.name);
    if (requestDto.price) formData.append('price', requestDto.price.toString());
    if (requestDto.weight) formData.append('weight', requestDto.weight.toString());
    if (requestDto.image) formData.append('image', requestDto.image);
    if (requestDto.description) formData.append('description', requestDto.description);
    if (requestDto.categoryId) formData.append('categoryId', requestDto.categoryId);

    return <Observable<ProductResponse>>this.http.post(this.adminProductUrl, formData)
      .pipe(
        tap(console.log)
      )
  }
}
