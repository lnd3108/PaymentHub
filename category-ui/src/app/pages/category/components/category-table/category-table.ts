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

  @Output() edit = new EventEmitter<Category>();
  @Output() delete = new EventEmitter<Category>();

  @Output() copy = new EventEmitter<Category>();
  @Output() viewSubmit = new EventEmitter<Category>();
  @Output() viewApprove = new EventEmitter<Category>();
  @Output() viewCancelApprove = new EventEmitter<Category>();

  getStatusLabel(status: number): string {
    switch (status) {
      case 1:
        return '1 - Tạo mới';
      case 3:
        return '3 - Chờ phê duyệt';
      case 4:
        return '4 - Đã phê duyệt';
      case 5:
        return '5 - Từ chối';
      case 7:
        return '7 - Hủy duyệt';
      default:
        return `${status} - Khác`;
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
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-slate-100 text-slate-700';
    }
  }

  canSubmit(item: Category): boolean {
    return item.status === 1 || item.status === 5 || item.status === 6;
  }

  canApprove(item: Category): boolean {
    return item.status === 3;
  }

  canCancelApprove(item: Category): boolean {
    return item.status === 4;
  }
}
