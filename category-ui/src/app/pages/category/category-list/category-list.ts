import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../models/category.models';
import { CategoryFiltersComponent } from '../components/category-filters/category-filters';
import { CategoryTableComponent } from '../components/category-table/category-table';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule, CategoryFiltersComponent, CategoryTableComponent],
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
})
export class CategoryList implements OnInit {
  private categoryService = inject(CategoryService);
  private toastr = inject(ToastrService);
  private router = inject(Router);

  categories: Category[] = [];

  loading = false;
  error = '';

  // statusOptions: { value: string; label: string }[] = [];
  // activeOptions: { value: string; label: string }[] = [];

  statusOptions: { value: string; label: string }[] = [
    { value: '', label: 'Tất cả' },
    { value: '1', label: 'Tạo mới' },
    { value: '3', label: 'Chờ phê duyệt' },
    { value: '4', label: 'Đã phê duyệt' },
    { value: '5', label: 'Từ chối' },
    { value: '7', label: 'Hủy duyệt' },
  ];

  activeOptions: { value: string; label: string }[] = [
    { value: '', label: 'Tất cả' },
    { value: '1', label: 'Hoạt động' },
    { value: '0', label: 'Không hoạt động' },
  ];

  currentFilters = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: '',
    isActive: '',
  };

  page = 0;
  size = 20;
  totalElements = 0;
  totalPages = 0;
  isFiltering = false;

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getAll(this.page, this.size).subscribe({
      next: (res) => {
        this.categories = res?.content ?? [];
        this.totalElements = res?.totalElements ?? 0;
        this.totalPages = res?.totalPages ?? 0;
        this.page = res?.number ?? 0;
        this.size = res?.size ?? this.size;

        // if (this.page === 0) {
        //   this.buildFilterOptions(this.categories);
        // }

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

  searchCategories(): void {
    const filters = this.currentFilters;
    const request: any = {};

    if (filters.paramName.trim()) {
      request.paramName = filters.paramName.trim();
    }

    if (filters.paramValue.trim()) {
      request.paramValue = filters.paramValue.trim();
    }
    if (filters.paramType.trim()) {
      request.paramType = filters.paramType.trim();
    }

    if (filters.status !== '') {
      request.status = [Number(filters.status)];
    }

    if (filters.isActive !== '') {
      request.isActive = [Number(filters.isActive)];
    }

    console.log('search request = ', request);

    this.loading = true;
    this.error = '';

    this.categoryService.search(request, this.page, this.size).subscribe({
      next: (res: any) => {
        this.categories = res?.content ?? [];
        this.totalElements = res?.totalElements ?? 0;
        this.totalPages = res?.totalPages ?? 0;
        this.page = res?.number ?? 0;
        this.size = res?.size ?? this.size;
        this.loading = false;
      },
      error: (err) => {
        console.error('Lỗi khi tìm kiếm category:', err);
        console.error('Response error body:', err?.error);
        this.error = 'Không tìm kiếm được dữ liệu';
        this.loading = false;
        this.toastr.error('Tìm kiếm thất bại', 'Lỗi');
      },
    });
  }

  getActiveLabel(isActive: number): string {
    switch (isActive) {
      case 1:
        return 'Hoạt động';
      case 0:
        return 'Không hoạt động';
      default:
        return 'Không xác định';
    }
  }

  buildFilterOptions(data: Category[]): void {
    const allowedStatuses = [1, 3, 4, 5, 7];
    const allowedActives = [1, 0];

    const uniqueStatuses = [
      ...new Set(
        data
          .map((item) => item.status)
          .filter((v): v is number => v !== null && v !== undefined && allowedStatuses.includes(v)),
      ),
    ].sort((a, b) => a - b);

    const uniqueActives = [
      ...new Set(
        data
          .map((item) => item.isActive)
          .filter((v): v is number => v !== null && v !== undefined && allowedActives.includes(v)),
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
        label: this.getActiveLabel(active),
      })),
    ];
  }

  onSearch(filters: {
    paramName: string;
    paramValue: string;
    paramType: string;
    status: string;
    isActive: string;
  }): void {
    this.currentFilters = { ...filters };
    this.isFiltering = true;
    this.page = 0;
    this.searchCategories();
  }

  onResetFilters(): void {
    this.currentFilters = {
      paramName: '',
      paramValue: '',
      paramType: '',
      status: '',
      isActive: '',
    };

    this.isFiltering = false;
    this.page = 0;
    this.error = '';
    this.loadCategories();
  }

  onPageChange(newPage: number): void {
    if (newPage < 0 || newPage >= this.totalPages || newPage === this.page) return;

    this.page = newPage;

    if (this.isFiltering) {
      this.searchCategories();
    } else {
      this.loadCategories();
    }
  }

  onSizeChange(newSize: number): void {
    this.size = newSize;
    this.page = 0;

    if (this.isFiltering) {
      this.searchCategories();
    } else {
      this.loadCategories();
    }
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
    const id = item.id;

    if (id == null) {
      this.toastr.error('Không tìm thấy id bản ghi', 'Lỗi');
      return;
    }

    Swal.fire({
      title: 'Xác nhận hủy duyệt',
      text: 'Bản ghi này sẽ chuyển sang trạng thái hủy duyệt.',
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Hủy',
      confirmButtonText: 'Xác nhận',
      reverseButtons: true,
      confirmButtonColor: '#007c7a',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.categoryService.cancelApprove(id).subscribe({
        next: () => {
          this.toastr.success('Hủy duyệt thành công', 'Thành công');
          this.loadCategories();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error(err?.error?.message || 'Hủy duyệt thất bại', 'Lỗi');
        },
      });
    });
  }

  deleteCategory(item: Category): void {
    const id = item.id;

    if (id == null) {
      this.toastr.error('Không tìm thấy id bản ghi', 'Lỗi');
      return;
    }

    if (item.isDisplay === 2) {
      this.toastr.warning('Bản ghi đã duyệt không cho phép xóa', 'cảnh báo');
      return;
    }

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

      this.categoryService.delete(id).subscribe({
        next: () => {
          this.toastr.success('Xóa thành công', 'Thành công');
          this.loadCategories();
        },
        error: (err) => {
          console.error(err);

          const message = err?.error?.message || 'Xóa thất bại';
          this.toastr.error('Xóa thất bại', 'Lỗi');
        },
      });
    });
  }

  exportExcel(): void {
    if (!this.categories || this.categories.length === 0) {
      this.toastr.warning('Không có dữ liệu để xuất', 'Cảnh báo');
      return;
    }

    const headers = [
      'STT',
      'Tên thành phần',
      'Giá trị thành phần',
      'Danh mục theo nhóm',
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
    const fileName = `category_export_${today.getFullYear()}-${String(
      today.getMonth() + 1,
    ).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}.csv`;

    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    this.toastr.success('Xuất file thành công', 'Thành công');
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
        return `Không xác định`;
    }
  }

  goDetail(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'detail']);
  }
}


