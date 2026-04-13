import { Injectable, inject } from '@angular/core';
import {
  CategoryBatchActionResponse,
  CategoryPageResponse,
  CategoryService,
} from '../../../service/category.service';
import { Category } from '../../../models/category.models';
import { CategoryFilter, CategoryFilterValue } from '../../../domain/category/category-filter';

@Injectable()
export class CategoryListFacade {
  private readonly categoryService = inject(CategoryService);

  categories: Category[] = [];
  loading = false;
  error = '';

  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;

  selectedIds = new Set<number>();
  currentFilter = CategoryFilter.empty();

  get isFiltering(): boolean {
    return !this.currentFilter.isEmpty;
  }

  load(onError?: () => void): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getAll(this.page, this.size).subscribe({
      next: (res) => {
        this.applyPageResponse(res);
        this.loading = false;
      },
      error: () => {
        this.error = 'Không tải được danh sách category';
        this.loading = false;
        onError?.();
      },
    });
  }

  search(filter?: CategoryFilter | CategoryFilterValue, onError?: () => void): void {
    if (filter) {
      this.currentFilter = filter instanceof CategoryFilter ? filter : CategoryFilter.fromValue(filter);
    }

    this.loading = true;
    this.error = '';

    this.categoryService.search(this.currentFilter.toRequest(), this.page, this.size).subscribe({
      next: (res: any) => {
        this.applyPageResponse({
          content: res?.content ?? [],
          totalElements: res?.totalElements ?? 0,
          totalPages: res?.totalPages ?? 0,
          page: res?.page ?? res?.number ?? 0,
          size: res?.size ?? this.size,
          first: !!res?.first,
          last: !!res?.last,
          empty: !!res?.empty,
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Không tìm kiếm được dữ liệu';
        this.loading = false;
        onError?.();
      },
    });
  }

  resetFilters(onError?: () => void): void {
    this.currentFilter = CategoryFilter.empty();
    this.page = 0;
    this.error = '';
    this.load(onError);
  }

  changePage(newPage: number): boolean {
    if (newPage < 0 || newPage >= this.totalPages || newPage === this.page) {
      return false;
    }

    this.page = newPage;
    this.reloadCurrentView();
    return true;
  }

  changeSize(newSize: number): void {
    this.size = newSize;
    this.page = 0;
    this.reloadCurrentView();
  }

  reloadCurrentView(onError?: () => void): void {
    if (this.isFiltering) {
      this.search(undefined, onError);
      return;
    }

    this.load(onError);
  }

  updateFilter(filterValue: CategoryFilterValue): void {
    this.currentFilter = CategoryFilter.fromValue(filterValue);
    this.page = 0;
  }

  clearSelection(): void {
    this.selectedIds.clear();
    this.selectedIds = new Set();
  }

  applyBatchResult(res: CategoryBatchActionResponse): void {
    this.applyUpdatedStatusRows(res.updated ?? []);
  }

  private applyPageResponse(res: CategoryPageResponse): void {
    this.categories = res?.content ?? [];
    this.totalElements = res?.totalElements ?? 0;
    this.totalPages = res?.totalPages ?? 0;
    this.page = res?.page ?? 0;
    this.size = res?.size ?? this.size;
    this.clearSelection();
  }

  private applyUpdatedStatusRows(updatedRows: { id: number; status: number }[]): void {
    const updatedMap = new Map<number, number>();
    updatedRows.forEach((row) => updatedMap.set(row.id, row.status));

    const touchedRows: Category[] = [];
    const untouchedRows: Category[] = [];
    let removedCount = 0;

    for (const item of this.categories) {
      if (item.id == null || !updatedMap.has(item.id)) {
        untouchedRows.push(item);
        continue;
      }

      const merged: Category = {
        ...item,
        status: updatedMap.get(item.id),
      };

      if (this.currentFilter.matches(merged)) {
        touchedRows.push(merged);
      } else {
        removedCount += 1;
      }
    }

    this.categories = [...touchedRows, ...untouchedRows];

    if (removedCount > 0) {
      this.totalElements = Math.max(this.totalElements - removedCount, 0);
      this.totalPages = this.totalElements > 0 ? Math.ceil(this.totalElements / this.size) : 0;
      if (this.totalPages === 0) {
        this.page = 0;
      } else if (this.page >= this.totalPages) {
        this.page = this.totalPages - 1;
      }
    }

    this.clearSelection();
  }
}
