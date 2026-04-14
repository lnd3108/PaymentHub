import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CategoryEntity } from '../../../domain/category/category.entity';
import { CategoryStatusPolicy } from '../../../domain/category/category-status';
import { Category } from '../../../models/category.models';
import { CategoryService } from '../../../service/category.service';

type DetailMode = 'detail' | 'submit' | 'approve' | 'cancel-approve';

@Component({
  selector: 'app-category-detail-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-detail-page.html',
  styleUrl: './category-detail-page.css',
})
export class CategoryDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);
  private readonly categoryService = inject(CategoryService);

  mode: DetailMode = 'submit';
  id: number | null = null;
  loading = false;
  oldData: Category | null = null;
  newData: Category | null = null;
  showRejectModal = false;
  rejectReason = '';
  readonly rejectReasonMaxLength = 500;
  rejecting = false;

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] ?? 'submit') as DetailMode;
    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? Number(idParam) : null;

    if (this.id) {
      this.loadDetail(this.id);
    }
  }

  loadDetail(id: number): void {
    this.loading = true;

    this.categoryService.getById(id).subscribe({
      next: (data) => {
        const parsedNewData = CategoryEntity.fromModel(data).parseNewData();
        const hasNewData = !!parsedNewData && Object.keys(parsedNewData).length > 0;

        if (this.mode === 'detail' || this.mode === 'cancel-approve') {
          this.oldData = data;
          this.newData = null;
        } else if (hasNewData) {
          this.oldData = data;
          this.newData = parsedNewData;
        } else {
          this.oldData = null;
          this.newData = data;
        }

        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.toastr.error('Không tải được chi tiết', 'Lỗi');
      },
    });
  }

  goBack(): void {
    void this.router.navigate(['/categories']);
  }

  confirmSubmit(): void {
    if (!this.id) {
      return;
    }

    this.categoryService.submit(this.id).subscribe({
      next: () => {
        this.toastr.success('Gửi duyệt thành công', 'Thành công');
        void this.router.navigate(['/categories']);
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Gửi duyệt thất bại', 'Lỗi');
      },
    });
  }

  confirmApprove(): void {
    if (!this.id) {
      return;
    }

    this.categoryService.approve(this.id).subscribe({
      next: () => {
        this.toastr.success('Phê duyệt thành công', 'Thành công');
        void this.router.navigate(['/categories']);
      },
      error: (err) => {
        console.error('Approve error:', err);
        this.toastr.error(err?.error?.message || 'Phê duyệt thất bại', 'Lỗi');
      },
    });
  }

  openRejectModal(): void {
    this.rejectReason = '';
    this.showRejectModal = true;
  }

  closeRejectModal(): void {
    if (this.rejecting) {
      return;
    }

    this.showRejectModal = false;
    this.rejectReason = '';
  }

  submitReject(): void {
    if (!this.id) {
      return;
    }

    const reason = this.rejectReason.trim();
    if (!reason) {
      this.toastr.warning('Vui lòng nhập lý do từ chối', 'Cảnh báo');
      return;
    }

    this.rejecting = true;

    this.categoryService.reject(this.id, { reason }).subscribe({
      next: () => {
        this.rejecting = false;
        this.showRejectModal = false;
        this.rejectReason = '';
        this.toastr.success('Từ chối thành công', 'Thành công');
        void this.router.navigate(['/categories']);
      },
      error: (err) => {
        this.rejecting = false;
        console.error(err);
        this.toastr.error(err?.error?.message || 'Từ chối thất bại', 'Lỗi');
      },
    });
  }

  confirmCancelApprove(): void {
    if (!this.id) {
      return;
    }

    this.categoryService.cancelApprove(this.id).subscribe({
      next: () => {
        this.toastr.success('Hủy duyệt thành công', 'Thành công');
        void this.router.navigate(['/categories']);
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Hủy duyệt thất bại', 'Lỗi');
      },
    });
  }

  get statusLabel(): string {
    if (this.mode === 'submit') {
      return `1 - ${CategoryStatusPolicy.label(1)}`;
    }

    if (this.mode === 'approve') {
      return `3 - ${CategoryStatusPolicy.label(3)}`;
    }

    if (this.mode === 'cancel-approve') {
      return `4 - ${CategoryStatusPolicy.label(4)}`;
    }

    return 'Chi tiết bản ghi';
  }

  get badgeClass(): string {
    if (this.mode === 'submit') {
      return 'bg-[#d5f5f2] text-[#0f9f98]';
    }

    if (this.mode === 'approve') {
      return 'bg-[#fff0c2] text-[#b7791f]';
    }

    if (this.mode === 'cancel-approve') {
      return 'bg-[#d8f6df] text-[#1f9d55]';
    }

    return 'bg-slate-100 text-slate-700';
  }

  get oldDataEntries(): [string, string][] {
    return [
      ['Tên thành phần', this.oldData?.paramName || '-'],
      ['Giá trị thành phần', this.oldData?.paramValue || '-'],
      ['Danh mục theo nhóm', this.oldData?.paramType || '-'],
      ['Cấu phần xử lý', this.oldData?.componentCode || '-'],
      ['Ngày hiệu lực', this.oldData?.effectiveDate || '-'],
      ['Ngày hết hiệu lực', this.oldData?.endEffectiveDate || '-'],
      ['Mô tả', this.oldData?.description || '-'],
    ];
  }

  get newDataEntries(): [string, string][] {
    return [
      ['Tên thành phần', this.newData?.paramName || '-'],
      ['Giá trị thành phần', this.newData?.paramValue || '-'],
      ['Danh mục theo nhóm', this.newData?.paramType || '-'],
      ['Cấu phần xử lý', this.newData?.componentCode || '-'],
      ['Ngày hiệu lực', this.newData?.effectiveDate || '-'],
      ['Ngày hết hiệu lực', this.newData?.endEffectiveDate || '-'],
      ['Mô tả', this.newData?.description || '-'],
    ];
  }

  isChanged(index: number, value: string): boolean {
    if (!this.oldData || this.mode !== 'approve') {
      return false;
    }

    const oldValue = this.oldDataEntries[index][1];
    return value !== oldValue && value !== '-';
  }
}
