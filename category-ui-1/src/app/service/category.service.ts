import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { CategorySearchRequest } from '../domain/category/category-filter';
import { Category } from '../models/category.models';

export interface ApiResponse<T> {
  message?: string;
  data: T;
}

export interface CategoryPageResponse {
  content: Category[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  sortBy?: string | null;
  sortDir?: string | null;
}

export interface  CategoryStatusOnlyResponse{
  id: number;
  status: number;
}

export interface CategoryBatchError{
  id: number | null;
  code: string;
  message: string;
}

export interface CategoryBatchActionResponse{
  totalRequested: number;
  successCount: number;
  failedCount: number;
  updated: CategoryStatusOnlyResponse[];
  failed: CategoryBatchError[];
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

    return this.http
      .get<ApiResponse<CategoryPageResponse>>(this.apiUrl, { params })
      .pipe(map((res) => res.data));
  }

  getById(id: number): Observable<Category> {
    return this.http
      .get<ApiResponse<Category>>(`${this.apiUrl}/${id}`)
      .pipe(map((res) => res.data));
  }

  create(data: Category): Observable<Category> {
    return this.http.post<ApiResponse<Category>>(this.apiUrl, data).pipe(map((res) => res.data));
  }

  update(id: number, data: Category): Observable<Category> {
    return this.http
      .put<ApiResponse<Category>>(`${this.apiUrl}/${id}`, data)
      .pipe(map((res) => res.data));
  }

  delete(id: number): Observable<string> {
    return this.http
      .delete<ApiResponse<string>>(`${this.apiUrl}/${id}`)
      .pipe(map((res) => res.data));
  }

  search(data: CategorySearchRequest, page = 0, size = 20): Observable<CategoryPageResponse> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', 'id,desc');

    return this.http
      .post<ApiResponse<CategoryPageResponse>>(`${this.apiUrl}/search`, data, { params })
      .pipe(map((res) => res.data));
  }

  submit(id: number): Observable<Category> {
    return this.http
      .post<ApiResponse<Category>>(`${this.apiUrl}/${id}/submit`, {})
      .pipe(map((res) => res.data));
  }

  createAndSubmit(data: Category): Observable<Category> {
    return this.http
      .post<ApiResponse<Category>>(`${this.apiUrl}/submit-create`, data)
      .pipe(map((res) => res.data));
  }

  approve(id: number): Observable<Category> {
    return this.http
      .post<ApiResponse<Category>>(`${this.apiUrl}/${id}/approve`, {})
      .pipe(map((res) => res.data));
  }

  reject(id: number, data: { reason: string }): Observable<Category> {
    return this.http
      .post<ApiResponse<Category>>(`${this.apiUrl}/${id}/reject`, data)
      .pipe(map((res) => res.data));
  }

  cancelApprove(id: number): Observable<Category> {
    return this.http
      .post<ApiResponse<Category>>(`${this.apiUrl}/${id}/cancel-approve`, {})
      .pipe(map((res) => res.data));
  }

  submitBatch(ids: number[]): Observable<CategoryBatchActionResponse> {
    return this.http
      .post<ApiResponse<CategoryBatchActionResponse>>(`${this.apiUrl}/submit-batch`, { ids })
      .pipe(map((res) => res.data));
  }

  approveBatch(ids: number[]): Observable<CategoryBatchActionResponse> {
    return this.http
      .post<ApiResponse<CategoryBatchActionResponse>>(`${this.apiUrl}/approve-batch`, { ids })
      .pipe(map((res) => res.data));
  }

  cancelApproveBatch(ids: number[]): Observable<CategoryBatchActionResponse> {
    return this.http
      .post<
        ApiResponse<CategoryBatchActionResponse>
      >(`${this.apiUrl}/batch-cancel-approve`, { ids })
      .pipe(map((res) => res.data));
  }

  exportExcel(filters?: any) {
    return this.http.post(`${this.apiUrl}/export`, filters || {}, {
      responseType: 'blob',
    });
  }

  importExcel(file: File, submitAfterImport: false) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('submitAfterImport', String(submitAfterImport));

    return this.http.post(`${this.apiUrl}/import`, formData);
  }
}
