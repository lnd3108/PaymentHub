import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../models/category.models';
import { CategorySearch } from '../models/category-search.models';
import { CategoryService } from './category.service';


@Injectable({
  providedIn: 'root'
})

export class CategoryNativeService{
  private http = inject(HttpClient);

  private apiNativeUrl = 'http://localhost:8094/api/native/categories';

  getAllNative(): Observable<Category[]>{
    return this.http.get<Category[]>(this.apiNativeUrl);
  }

  getByidNative(id: number): Observable<Category>{
    return this.http.get<Category>(`${this.apiNativeUrl}/${id}`);
  }

  createNative(data: Category): Observable<Category>{
    return this.http.post<Category>(this.apiNativeUrl, data);
  }

  updateNative(id: number, data: Category):Observable<Category>{
    return this.http.post<Category>(`${this.apiNativeUrl}/&{id}`, data);
  }

  deleteNative(id: number): Observable<void>{
    return this.http.delete<void>(`${this.apiNativeUrl}/${id}`);
  }

  searchNative(data: CategorySearch):Observable<Category[]>{
    return this.http.post<Category[]>(`${this.apiNativeUrl}/search`, data);
  }
}
