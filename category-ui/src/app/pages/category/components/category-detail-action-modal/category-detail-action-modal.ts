import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Category } from '../../../../models/category.models';

type ActionMode = 'submit' | 'approve' | 'cancel-approve';

@Component({
  selector: 'app-category-detail-action-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: 'category-detail-action-modal.html',
  styleUrl: './category-detail-action-modal.css',
})
export class CategoryDetailActionModalComponent {
  @Input() isOpen = false;
  @Input() mode: ActionMode = 'submit';
  @Input() oldData: Category | null = null;
  @Input() newData: Category | null = null;
  @Input() statusLabel = '';

  @Output() close = new EventEmitter<void>();
  @Output() submitAction = new EventEmitter<void>();
  @Output() approveAction = new EventEmitter<void>();
  @Output() rejectAction = new EventEmitter<void>();
  @Output() cancelApproveAction = new EventEmitter<void>();
  @Output() clearAction = new EventEmitter<void>();

  get badgeClass(): string {
    if (this.mode === 'submit') return 'bg-[#d5f5f2] text-[#0f9f98]';
    if (this.mode === 'approve') return 'bg-[#fff0c2] text-[#b7791f]';
    return 'bg-[#d8f6df] text-[#1f9d55]';
  }

  get newDataEntries() {
    return [
      ['Tên thành phần', this.newData?.paramType || '-'],
      ['Giá trị thành phần', this.newData?.paramValue || '-'],
      ['Danh mục theo nhóm', this.newData?.paramName || '-'],
      ['Cấu phần xử lý', this.newData?.componentCode || '-'],
      ['Ngày hiệu lực', this.newData?.effectiveDate || '-'],
      ['Ngày hết hiệu lực', this.newData?.endEffectiveDate || '-'],
      ['Mô tả', this.newData?.description || '-'],
    ];
  }

  get oldDataEntries() {
    return [
      ['Tên thành phần', this.oldData?.paramType || '-'],
      ['Giá trị thành phần', this.oldData?.paramValue || '-'],
      ['Danh mục theo nhóm', this.oldData?.paramName || '-'],
      ['Cấu phần xử lý', this.oldData?.componentCode || '-'],
      ['Ngày hiệu lực', this.oldData?.effectiveDate || '-'],
      ['Ngày hết hiệu lực', this.oldData?.endEffectiveDate || '-'],
      ['Mô tả', this.oldData?.description || '-'],
    ];
  }
}
