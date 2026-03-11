import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../models/category.models';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
})
export class CategoryList implements OnInit {
  private categoryService = inject(CategoryService);
  private toastr = inject(ToastrService);
  private fb = inject(FormBuilder);

  categories: Category[] = [];
  allCategories: Category[] = [];

  loading = false;
  error = '';

  isModalOpen = false;
  modalMode: 'create' | 'update' = 'create';
  selectedCategoryId: number | null = null;

  filters = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: '',
    isActive: '',
  };

  statusOptions: { value: string; label: string }[] = [];
  activeOptions: { value: string; label: string }[] = [];

  categoryForm = this.fb.group({
    paramName: ['', [Validators.required]],
    paramValue: ['', [Validators.required]],
    paramType: ['', [Validators.required]],
    description: [''],
    componentCode: [''],
    status: [1, [Validators.required]],
    isActive: [1, [Validators.required]],
    isDisplay: [1],
    effectiveDate: [''],
    endEffectiveDate: [''],
  });

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
    const uniqueStatuses = [...new Set(data.map((item) => item.status))].sort((a, b) => a - b);
    const uniqueActives = [...new Set(data.map((item) => item.isActive))].sort((a, b) => a - b);

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

  searchCategories(): void {
    const paramName = this.filters.paramName.trim().toLowerCase();
    const paramValue = this.filters.paramValue.trim().toLowerCase();
    const paramType = this.filters.paramType.trim().toLowerCase();

    const selectedStatus = this.filters.status === '' ? null : Number(this.filters.status);
    const selectedIsActive = this.filters.isActive === '' ? null : Number(this.filters.isActive);

    this.categories = this.allCategories.filter((item) => {
      const matchParamName = !paramName || item.paramName?.toLowerCase().includes(paramName);

      const matchParamValue = !paramValue || item.paramValue?.toLowerCase().includes(paramValue);

      const matchParamType = !paramType || item.paramType?.toLowerCase().includes(paramType);

      const matchStatus = selectedStatus === null || item.status === selectedStatus;

      const matchIsActive = selectedIsActive === null || item.isActive === selectedIsActive;

      return matchParamName && matchParamValue && matchParamType && matchStatus && matchIsActive;
    });
  }

  resetFilters(): void {
    this.filters = {
      paramName: '',
      paramValue: '',
      paramType: '',
      status: '',
      isActive: '',
    };

    this.categories = [...this.allCategories];
  }

  openCreateModal(): void {
    this.modalMode = 'create';
    this.selectedCategoryId = null;
    this.categoryForm.reset({
      paramName: '',
      paramValue: '',
      paramType: '',
      description: '',
      componentCode: '',
      status: 1,
      isActive: 1,
      isDisplay: 1,
      effectiveDate: '',
      endEffectiveDate: '',
    });
    this.isModalOpen = true;
  }

  openUpdateModal(item: Category): void {
    this.modalMode = 'update';
    this.selectedCategoryId = item.id ?? null;

    this.categoryForm.patchValue({
      paramName: item.paramName ?? '',
      paramValue: item.paramValue ?? '',
      paramType: item.paramType ?? '',
      description: item.description ?? '',
      componentCode: item.componentCode ?? '',
      status: item.status ?? 1,
      isActive: item.isActive ?? 1,
      isDisplay: item.isDisplay ?? 1,
      effectiveDate: item.effectiveDate ?? '',
      endEffectiveDate: item.endEffectiveDate ?? '',
    });

    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.categoryForm.markAsPristine();
    this.categoryForm.markAsUntouched();
  }

  submitForm(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.toastr.warning('Vui lòng nhập đầy đủ thông tin bắt buộc', 'Cảnh báo');
      return;
    }

    const payload = this.categoryForm.getRawValue() as Category;

    if (this.modalMode === 'create') {
      this.createCategory(payload);
      return;
    }

    if (this.selectedCategoryId) {
      this.updateCategory(this.selectedCategoryId, payload);
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

  deleteCategory(id?: number): void {
    if (!id) return;

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

      this.categoryService.delete(id).subscribe({
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

  getStatusClass(status: number): string {
    switch (status) {
      case 1:
        return 'bg-blue-50 text-blue-700';
      case 2:
        return 'bg-slate-100 text-slate-700';
      case 3:
        return 'bg-amber-50 text-amber-700';
      case 4:
        return 'bg-green-50 text-green-700';
      case 5:
        return 'bg-red-50 text-red-700';
      case 6:
        return 'bg-yellow-50 text-yellow-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }
}
