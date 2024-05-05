import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap} from 'rxjs';
import {CreateCategoryRequestDto} from 'src/app/models/dto/category/create-category-request-dto';
import {CategoryResponse} from 'src/app/models/responses/category-response';
import {environment} from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private readonly categoryUrl = `${environment.api.baseUrl}/${environment.api.categoryUrl}`;
  private readonly adminCategoryUrl = `${environment.api.baseUrl}/${environment.api.adminCategoryUrl}`;

  constructor(
    private http: HttpClient
  ) {
  }

  categories$ = <Observable<CategoryResponse>>this.http.get(this.categoryUrl)
    .pipe(
      tap(console.log)
    )

  categoryById$ = (id: string) => <Observable<CategoryResponse>>this.http.get(`${this.categoryUrl}/${id}`)
    .pipe(
      tap(console.log)
    )

  createCategory$ = (requestDto: CreateCategoryRequestDto) => <Observable<CategoryResponse>>this.http.post(`${this.adminCategoryUrl}`, requestDto)
    .pipe(
      tap(console.log)
    )

  updateCategoryById$ = (id: string, requestDto: CreateCategoryRequestDto) => <Observable<CategoryResponse>>this.http.put(`${this.adminCategoryUrl}/${id}`, requestDto)
    .pipe(
      tap(console.log)
    )

  deleteCategoryById$ = (id: string) => <Observable<CategoryResponse>>this.http.delete(`${this.adminCategoryUrl}/${id}`)
    .pipe(
      tap(console.log)
    )
}
