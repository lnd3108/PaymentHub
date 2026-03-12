import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../models/category.models';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import { CategoryFiltersComponent } from '../components/category-filters/category-filters';
import { CategoryTableComponent } from '../components/category-table/category-table';
import { CategoryFormModalComponent } from '../components/category-form-modal/category-form-modal';
import {CategoryDetailActionModalComponent } from '../components/category-detail-action-modal/category-detail-action-modal';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [
    CommonModule,
    CategoryFiltersComponent,
    CategoryTableComponent,
    CategoryFormModalComponent,
  ],
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
})
export class CategoryList implements OnInit {
  private categoryService = inject(CategoryService);
  private toastr = inject(ToastrService);

  categories: Category[] = [];
  allCategories: Category[] = [];

  loading = false;
  error = '';

  isModalOpen = false;
  modalMode: 'create' | 'update' | 'copy' = 'create';
  selectedCategory: Category | null = null;

  statusOptions: { value: string; label: string }[] = [];
  activeOptions: { value: string; label: string }[] = [];

  detailModalOpen = false;
  detailMode: 'submit' | 'approve' | 'cancel-approve' = 'submit';
  detailOldData: Category | null = null;
  detailNewData: Category | null = null;
  detailTarget: Category | null = null;

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getAll().subscribe({
      next: (data) => {
        this.allCategories = data ?? [];
        this.categories = [...this.allCategories];
        this.buildFilterOptions(this.allCategories);
        this.loading = false;
      },
      error: (err) => {
        console.error('Lỗi khi lấy danh sách category:', err);
        this.error = 'Không tải được danh sách category';
        this.loading = false;
        this.toastr.error('Không tải được danh sách category', 'Lỗi');
      },
    });
  }

  buildFilterOptions(data: Category[]): void {
    const statuses = data
      .map((item) => item.status)
      .filter((v): v is number => v !== null && v !== undefined);

    const actives = data
      .map((item) => item.isActive)
      .filter((v): v is number => v !== null && v !== undefined);

    const uniqueStatuses = [...new Set(statuses)].sort((a, b) => a - b);
    const uniqueActives = [...new Set(actives)].sort((a, b) => a - b);

    this.statusOptions = [
      { value: '', label: 'Tất cả' },
      ...uniqueStatuses.map((status) => ({
        value: String(status),
        label: this.getStatusLabel(status),
      })),
    ];

    this.activeOptions = [
      { value: '', label: 'Tất cả' },
      ...uniqueActives.map((active) => ({
        value: String(active),
        label: active === 1 ? 'Hoạt động' : 'Không hoạt động',
      })),
    ];
  }

  handleSearch(filters: {
    paramName: string;
    paramValue: string;
    paramType: string;
    status: string;
    isActive: string;
  }): void {
    const paramName = filters.paramName.trim().toLowerCase();
    const paramValue = filters.paramValue.trim().toLowerCase();
    const paramType = filters.paramType.trim().toLowerCase();

    const selectedStatus = filters.status === '' ? null : Number(filters.status);
    const selectedIsActive = filters.isActive === '' ? null : Number(filters.isActive);

    this.categories = this.allCategories.filter((item) => {
      const matchParamName = !paramName || item.paramName?.toLowerCase().includes(paramName);
      const matchParamValue = !paramValue || item.paramValue?.toLowerCase().includes(paramValue);
      const matchParamType = !paramType || item.paramType?.toLowerCase().includes(paramType);
      const matchStatus = selectedStatus === null || item.status === selectedStatus;
      const matchIsActive = selectedIsActive === null || item.isActive === selectedIsActive;

      return matchParamName && matchParamValue && matchParamType && matchStatus && matchIsActive;
    });
  }

  handleReset(): void {
    this.categories = [...this.allCategories];
  }

  openCreateModal(): void {
    this.modalMode = 'create';
    this.selectedCategory = null;
    this.isModalOpen = true;
  }

  openUpdateModal(item: Category): void {
    this.modalMode = 'update';
    this.selectedCategory = item;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.selectedCategory = null;
  }

  handleSave(payload: Category): void {
    if (this.modalMode === 'create') {
      this.createCategory(payload);
      return;
    }

    if (this.selectedCategory?.id) {
      this.updateCategory(this.selectedCategory.id, payload);
    }
  }

  createCategory(payload: Category): void {
    this.categoryService.create(payload).subscribe({
      next: () => {
        this.toastr.success('Thêm mới thành công', 'Thành công');
        this.closeModal();
        this.loadCategories();
      },
      error: (err) => {
        console.error('Lỗi khi thêm mới category:', err);
        this.toastr.error('Thêm mới thất bại', 'Lỗi');
      },
    });
  }

  updateCategory(id: number, payload: Category): void {
    this.categoryService.update(id, payload).subscribe({
      next: () => {
        this.toastr.success('Cập nhật thành công', 'Thành công');
        this.closeModal();
        this.loadCategories();
      },
      error: (err) => {
        console.error('Lỗi khi cập nhật category:', err);
        this.toastr.error('Cập nhật thất bại', 'Lỗi');
      },
    });
  }

  deleteCategory(item: Category): void {
    if (!item.id) return;

    Swal.fire({
      title: 'Xác nhận xóa?',
      text: 'Bản ghi này sẽ bị xóa khỏi hệ thống.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy',
      reverseButtons: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.categoryService.delete(item.id!).subscribe({
        next: () => {
          this.toastr.success('Xóa thành công', 'Thành công');
          this.loadCategories();
        },
        error: (err) => {
          console.error('Lỗi khi xóa category:', err);
          this.toastr.error('Xóa thất bại', 'Lỗi');
        },
      });
    });
  }

  getStatusLabel(status: number): string {
    switch (status) {
      case 1:
        return '1 - Tạo mới';
      case 2:
        return '2 - Khởi tạo';
      case 3:
        return '3 - Chờ phê duyệt';
      case 4:
        return '4 - Đã phê duyệt';
      case 5:
        return '5 - Từ chối';
      case 6:
        return '6 - Hủy duyệt';
      default:
        return `${status} - Khác`;
    }
  }

  openCopyModal(item: Category): void {
    this.modalMode = 'copy';
    this.selectedCategory = item;
    this.isModalOpen = true;
  }

  parseNewData(item: Category): Category | null {
    try {
      return item.newData ? JSON.parse(item.newData) : null;
    } catch {
      return null;
    }
  }

  openSubmitDetail(item: Category): void {
    this.detailMode = 'submit';
    this.detailTarget = item;
    this.detailOldData = null;
    this.detailNewData = item;
    this.detailModalOpen = true;
  }

  openApproveDetail(item: Category): void {
    this.detailMode = 'approve';
    this.detailTarget = item;
    this.detailOldData = item;
    this.detailNewData = this.parseNewData(item) ?? item;
    this.detailModalOpen = true;
  }

  openCancelApproveDetail(item: Category): void {
    this.detailMode = 'cancel-approve';
    this.detailTarget = item;
    this.detailOldData = null;
    this.detailNewData = item;
    this.detailModalOpen = true;
  }

  closeDetailModal(): void {
    this.detailModalOpen = false;
    this.detailTarget = null;
    this.detailOldData = null;
    this.detailNewData = null;
  }

  handleSaveAndSubmit(payload: Category): void {
    if (this.modalMode === 'create' || this.modalMode === 'copy') {
      this.categoryService.create(payload).subscribe({
        next: (created) => {
          const id = created?.id;
          if (!id) {
            this.toastr.success('Lưu thành công', 'Thành công');
            this.closeModal();
            this.loadCategories();
            return;
          }

          this.categoryService.submit(id).subscribe({
            next: () => {
              this.toastr.success('Lưu và gửi duyệt thành công', 'Thành công');
              this.closeModal();
              this.loadCategories();
            },
            error: () => this.toastr.error('Gửi duyệt thất bại', 'Lỗi'),
          });
        },
        error: () => this.toastr.error('Lưu thất bại', 'Lỗi'),
      });
      return;
    }

    if (this.selectedCategory?.id) {
      this.categoryService.update(this.selectedCategory.id, payload).subscribe({
        next: () => {
          this.toastr.success('Cập nhật thành công', 'Thành công');
          this.closeModal();
          this.loadCategories();
        },
        error: () => this.toastr.error('Cập nhật thất bại', 'Lỗi'),
      });
    }
  }

  confirmSubmit(): void {
    if (!this.detailTarget?.id) return;
    this.categoryService.submit(this.detailTarget.id).subscribe({
      next: () => {
        this.toastr.success('Gửi duyệt thành công', 'Thành công');
        this.closeDetailModal();
        this.loadCategories();
      },
      error: () => this.toastr.error('Gửi duyệt thất bại', 'Lỗi'),
    });
  }

  confirmApprove(): void {
    if (!this.detailTarget?.id) return;
    this.categoryService.approve(this.detailTarget.id).subscribe({
      next: () => {
        this.toastr.success('Phê duyệt thành công', 'Thành công');
        this.closeDetailModal();
        this.loadCategories();
      },
      error: () => this.toastr.error('Phê duyệt thất bại', 'Lỗi'),
    });
  }

  confirmReject(): void {
    if (!this.detailTarget?.id) return;
    this.categoryService
      .reject(this.detailTarget.id, { reason: 'Từ chối từ giao diện' })
      .subscribe({
        next: () => {
          this.toastr.success('Từ chối thành công', 'Thành công');
          this.closeDetailModal();
          this.loadCategories();
        },
        error: () => this.toastr.error('Từ chối thất bại', 'Lỗi'),
      });
  }

  confirmCancelApprove(): void {
    if (!this.detailTarget?.id) return;
    this.categoryService.cancelApprove(this.detailTarget.id).subscribe({
      next: () => {
        this.toastr.success('Hủy duyệt thành công', 'Thành công');
        this.closeDetailModal();
        this.loadCategories();
      },
      error: () => this.toastr.error('Hủy duyệt thất bại', 'Lỗi'),
    });
  }
}
