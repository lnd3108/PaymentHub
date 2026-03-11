import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../models/category.models';
import { CategorySearch } from '../models/category-search.models';


@Injectable({
  providedIn: 'root'
})

export class CategoryService{
  private http = inject(HttpClient);

  private apiUrl = 'http://localhost:8094/api/jpa/categories';

  getAll(): Observable<Category[]>{
    return this.http.get<Category[]>(this.apiUrl);
  }

  getById(id: number): Observable<Category>{
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  create(data: Category): Observable<Category>{
    return this.http.post<Category>(`${this.apiUrl}/created`, data);
  }

  update(id: number, data: Category) {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void>{
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  search(data: CategorySearch): Observable<Category[]>{
    return this.http.post<Category[]>(`${this.apiUrl}/search`, data);
  }
}
