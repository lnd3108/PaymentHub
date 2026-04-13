import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CategoryEntity } from '../../../../domain/category/category.entity';
import { CategoryStatusPolicy } from '../../../../domain/category/category-status';
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
  @Input() selectedIds = new Set<number>();
  @Input() allSelected = false;
  @Input() someSelected = false;

  @Output() viewDetail = new EventEmitter<Category>();
  @Output() edit = new EventEmitter<Category>();
  @Output() delete = new EventEmitter<Category>();
  @Output() copy = new EventEmitter<Category>();
  @Output() viewSubmit = new EventEmitter<Category>();
  @Output() viewApprove = new EventEmitter<Category>();
  @Output() viewCancelApprove = new EventEmitter<Category>();
  @Output() toggleAll = new EventEmitter<boolean>();
  @Output() toggleItem = new EventEmitter<{ item: Category; checked: boolean }>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() sizeChange = new EventEmitter<number>();

  readonly pageSizeOptions = [20, 50, 100];

  get startItem(): number {
    if (this.totalElements === 0) {
      return 0;
    }

    return this.page * this.size + 1;
  }

  get endItem(): number {
    return Math.min((this.page + 1) * this.size, this.totalElements);
  }

  get visiblePages(): number[] {
    if (this.totalPages <= 5) {
      return Array.from({ length: this.totalPages }, (_, index) => index);
    }

    let start = Math.max(this.page - 2, 0);
    const end = Math.min(start + 4, this.totalPages - 1);

    if (end - start < 4) {
      start = Math.max(end - 4, 0);
    }

    return Array.from({ length: end - start + 1 }, (_, index) => start + index);
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
    this.sizeChange.emit(Number((event.target as HTMLSelectElement).value));
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

  onToggleAll(event: Event): void {
    this.onCheckboxClick(event as MouseEvent);
    this.toggleAll.emit((event.target as HTMLInputElement).checked);
  }

  onToggleItem(item: Category, event: Event): void {
    this.onCheckboxClick(event as MouseEvent);
    this.toggleItem.emit({
      item,
      checked: (event.target as HTMLInputElement).checked,
    });
  }

  isSelected(item: Category): boolean {
    return item.id != null && this.selectedIds.has(item.id);
  }

  getStatusLabel(status: number): string {
    return CategoryStatusPolicy.label(status);
  }

  getStatusClass(status: number): string {
    return CategoryStatusPolicy.badgeClass(status);
  }

  canSubmit(item: Category): boolean {
    return CategoryEntity.fromModel(item).canSubmit();
  }

  canApprove(item: Category): boolean {
    return CategoryEntity.fromModel(item).canApprove();
  }

  canCancelApprove(item: Category): boolean {
    return CategoryEntity.fromModel(item).canCancelApprove();
  }
}
