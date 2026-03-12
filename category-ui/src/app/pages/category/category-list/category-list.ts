import { Component, OnInit, inject, EventEmitter,Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../models/category.models';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CategorySearch } from '../../../models/category-search.models';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
})
export class CategoryList implements OnInit {
  private categoryService = inject(CategoryService);
  private toastr = inject(ToastrService);
  private router = inject(Router);

  categories: Category[] = [];
  allCategories: Category[] = [];

  loading = false;
  error = '';

  statusOptions: { value: string; label: string }[] = [];
  activeOptions: { value: string; label: string }[] = [];

  filters = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: '',
    isActive: '',
  };

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
    const uniqueStatuses = [
      ...new Set(
        data.map((item) => item.status).filter((v): v is number => v !== null && v !== undefined),
      ),
    ].sort((a, b) => a - b);

    const uniqueActives = [
      ...new Set(
        data.map((item) => item.isActive).filter((v): v is number => v !== null && v !== undefined),
      ),
    ].sort((a, b) => a - b);

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

  search(): void {
    this.loading = true;
    this.error = '';

    const request: CategorySearch = {
      paramName: this.filters.paramName.trim() || null,
      paramValue: this.filters.paramValue.trim() || null,
      paramType: this.filters.paramType.trim() || null,
      status: this.filters.status === '' ? null : Number(this.filters.status),
      isActive: this.filters.isActive === '' ? null : Number(this.filters.isActive),
    };

    this.categoryService.search(request).subscribe({
      next: (res) => {
        this.categories = res ?? [];
        this.loading = false;
      },
      error: (err) => {
        console.error('Lỗi khi tìm kiếm category:', err);
        this.error = 'Không tìm kiếm được dữ liệu';
        this.loading = false;
        this.toastr.error('Tìm kiếm thất bại', 'Lỗi');
      },
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

  goCreate(): void {
    this.router.navigate(['/categories/create']);
  }

  goEdit(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'edit']);
  }

  goCopy(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'copy']);
  }

  goSubmit(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'submit']);
  }

  goApprove(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'approve']);
  }

  goCancelApprove(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'cancel-approve']);
  }

  deleteCategory(item: Category): void {
    if (!item.id) return;

    Swal.fire({
      title: 'Xác nhận xóa',
      text: 'Bản ghi này sẽ bị xóa khỏi hệ thống.',
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Hủy',
      confirmButtonText: 'Xóa',
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
          console.error(err);
          this.toastr.error('Xóa thất bại', 'Lỗi');
        },
      });
    });
  }

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
    return item.status === 1 || item.status === 5 || item.status === 6 || item.status === 7;
  }

  canApprove(item: Category): boolean {
    return item.status === 3;
  }

  canCancelApprove(item: Category): boolean {
    return item.status === 4;
  }

  exportExcel(): void {
    if (!this.categories || this.categories.length === 0) {
      this.toastr.warning('Không có dữ liệu để xuất', 'Cảnh báo');
      return;
    }

    const headers = [
      'STT',
      'Danh mục theo nhóm',
      'Giá trị thành phần',
      'Tên thành phần',
      'Mô tả',
      'Cấu phần xử lý',
      'Ngày hiệu lực',
      'Ngày hết hiệu lực',
      'Trạng thái',
      'Trạng thái hoạt động',
    ];

    const rows = this.categories.map((item, index) => [
      index + 1,
      item.paramName ?? '',
      item.paramValue ?? '',
      item.paramType ?? '',
      item.description ?? '',
      item.componentCode ?? '',
      item.effectiveDate ?? '',
      item.endEffectiveDate ?? '',
      this.getStatusLabel(item.status ?? 0),
      item.isActive === 1 ? 'Hoạt động' : 'Không hoạt động',
    ]);

    const csvContent = [headers, ...rows]
      .map((row) => row.map((value) => `"${String(value).replace(/"/g, '""')}"`).join(','))
      .join('\n');

    const blob = new Blob(['\uFEFF' + csvContent], {
      type: 'text/csv;charset=utf-8;',
    });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;

    const today = new Date();
    const fileName = `category_export_${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}.csv`;

    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    window.URL.revokeObjectURL(url);
    this.toastr.success('Xuất file thành công', 'Thành công');
  }
}
