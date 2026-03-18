import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Category } from '../../../../models/category.models';

@Component({
  selector: 'app-category-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-table.html',
  styleUrl: './category-table.css',
})
export class CategoryTableComponent {
  @Input() categories: Category[] = [];
  @Input() loading = false;
  @Input() error = '';
  @Input() page = 0;
  @Input() size = 20;
  @Input() totalElements = 0;
  @Input() totalPages = 0;

  @Output() viewDetail = new EventEmitter<Category>();
  @Output() edit = new EventEmitter<Category>();
  @Output() delete = new EventEmitter<Category>();
  @Output() copy = new EventEmitter<Category>();
  @Output() viewSubmit = new EventEmitter<Category>();
  @Output() viewApprove = new EventEmitter<Category>();
  @Output() viewCancelApprove = new EventEmitter<Category>();

  @Output() pageChange = new EventEmitter<number>();
  @Output() sizeChange = new EventEmitter<number>();

  readonly pageSizeOptions = [20, 50, 100];

  get startItem(): number {
    if (this.totalElements === 0) return 0;
    return this.page * this.size + 1;
  }

  get endItem(): number {
    return Math.min((this.page + 1) * this.size, this.totalElements);
  }

  get visiblePages(): number[] {
    if (this.totalPages <= 5) {
      return Array.from({ length: this.totalPages }, (_, i) => i);
    }

    let start = Math.max(this.page - 2, 0);
    let end = Math.min(start + 4, this.totalPages - 1);

    if (end - start < 4) {
      start = Math.max(end - 4, 0);
    }

    return Array.from({ length: end - start + 1 }, (_, i) => start + i);
  }

  onPrevPage(): void {
    if (this.page > 0) {
      this.pageChange.emit(this.page - 1);
    }
  }

  onNextPage(): void {
    if (this.page < this.totalPages - 1) {
      this.pageChange.emit(this.page + 1);
    }
  }

  onSelectPage(page: number): void {
    if (page !== this.page) {
      this.pageChange.emit(page);
    }
  }

  onSizeSelect(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    this.sizeChange.emit(value);
  }

  getRowNumber(index: number): number {
    return this.page * this.size + index + 1;
  }

  onRowClick(item: Category): void {
    this.viewDetail.emit(item);
  }

  onCopy(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.copy.emit(item);
  }

  onViewSubmit(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.viewSubmit.emit(item);
  }

  onViewApprove(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.viewApprove.emit(item);
  }

  onViewCancelApprove(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.viewCancelApprove.emit(item);
  }

  onDelete(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.delete.emit(item);
  }

  onEdit(item: Category, event: MouseEvent): void {
    event.stopPropagation();
    this.edit.emit(item);
  }

  onCheckboxClick(event: MouseEvent): void {
    event.stopPropagation();
  }

  getStatusLabel(status: number): string {
    switch (status) {
      case 1:
        return 'Tạo mới';
      case 3:
        return 'Chờ phê duyệt';
      case 4:
        return 'Đã phê duyệt';
      case 5:
        return 'Từ chối';
      case 7:
        return 'Hủy duyệt';
      default:
        return 'Không xác định';
    }
  }

  getStatusClass(status: number): string {
    switch (status) {
      case 1:
        return 'bg-cyan-50 text-cyan-700';
      case 3:
        return 'bg-amber-50 text-amber-700';
      case 4:
        return 'bg-green-50 text-green-700';
      case 5:
        return 'bg-red-50 text-red-700';
      case 7:
        return 'bg-slate-100 text-slate-700';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  canSubmit(item: Category): boolean {
    return item.status === 1 || item.status === 5 || item.status === 6 || item.status === 7;
  }

  canApprove(item: Category): boolean {
    return item.status === 3;
  }

  canCancelApprove(item: Category): boolean {
    return item.status === 4;
  }
}
