import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../models/category.models';
import { CategorySearch } from '../models/category-search.models';

export interface CategoryPageResponse {
  content: Category[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8094/api/jpa/categories';

  getAll(page = 0, size = 20): Observable<CategoryPageResponse> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'id,desc');

    return this.http.get<CategoryPageResponse>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  create(data: Category): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, data);
  }

  update(id: number, data: Category): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(data: Partial<CategorySearch>, page = 0, size = 20): Observable<CategoryPageResponse> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'id,desc');

    return this.http.post<CategoryPageResponse>(`${this.apiUrl}/search`, data, { params });
  }

  submit(id: number): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/${id}/submit`, {});
  }

  createAndSubmit(id: number): Observable<Category>{
    return this.http.post<Category>(`${this.apiUrl}/${id}/submit-create`, {});
  }

  approve(id: number): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/${id}/approve`, {});
  }

  reject(id: number, data: { reason: string }): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/${id}/reject`, data);
  }

  cancelApprove(id: number): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/${id}/cancel-approve`, {});
  }
}
