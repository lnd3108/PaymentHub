import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import { CategoryEntity } from '../../../domain/category/category.entity';
import { CategoryFilterForm } from '../../../domain/category/category-filter';
import { Category } from '../../../models/category.models';
import {
  CategoryPermissionService,
  CategoryAction,
} from '../../../service/category-permission.service';
import { CategoryBatchActionResponse, CategoryService } from '../../../service/category.service';
import { CategoryFiltersComponent } from '../components/category-filters/category-filters';
import { CategoryTableComponent } from '../components/category-table/category-table';
import { CategoryListFacade } from './category-list.facade';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, FormsModule, CategoryFiltersComponent, CategoryTableComponent],
  templateUrl: './category-list.html',
  styleUrl: './category-list.css',
  providers: [CategoryListFacade],
})
export class CategoryList implements OnInit {
  private readonly categoryService = inject(CategoryService);
  private readonly toastr = inject(ToastrService);
  private readonly router = inject(Router);
  private readonly permissionService = inject(CategoryPermissionService);
  readonly facade = inject(CategoryListFacade);

  ngOnInit(): void {
    this.facade.loadCategories();
  }

  onSearch(filters: CategoryFilterForm): void {
    this.facade.applySearch(filters);
  }

  onResetFilters(): void {
    this.facade.resetFilters();
  }

  onPageChange(newPage: number): void {
    this.facade.changePage(newPage);
  }

  onSizeChange(newSize: number): void {
    this.facade.changeSize(newSize);
  }

  onToggleSelectAll(checked: boolean): void {
    this.facade.toggleSelectAll(checked);
  }

  onToggleSelectItem(event: { item: Category; checked: boolean }): void {
    this.facade.toggleSelectItem(event.item, event.checked);
  }

  bulkSubmitSelected(): void {
    if (!this.ensurePermission('submit')) {
      return;
    }

    if (!this.facade.canBulkSubmit) {
      this.toastr.warning(
        'Chỉ được gửi duyệt nhiều bản ghi khi các bản ghi cùng trạng thái hợp lệ.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(
      ids,
      () => this.categoryService.submitBatch(ids),
      'Xác nhận gửi duyệt',
      'Gửi duyệt',
    );
  }

  bulkApproveSelected(): void {
    if (!this.ensurePermission('approve')) {
      return;
    }

    if (!this.facade.canBulkApprove) {
      this.toastr.warning(
        'Chỉ được phê duyệt nhiều bản ghi khi tất cả đang ở trạng thái chờ phê duyệt.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(
      ids,
      () => this.categoryService.approveBatch(ids),
      'Xác nhận phê duyệt',
      'Phê duyệt',
    );
  }

  bulkCancelApproveSelected(): void {
    if (!this.ensurePermission('cancelApprove')) {
      return;
    }

    if (!this.facade.canBulkCancelApprove) {
      this.toastr.warning(
        'Chỉ được hủy duyệt nhiều bản ghi khi tất cả đang ở trạng thái đã phê duyệt.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(
      ids,
      () => this.categoryService.cancelApproveBatch(ids),
      'Xác nhận hủy duyệt',
      'Hủy duyệt',
    );
  }

  bulkDeleteSelected(): void {
    if (!this.ensurePermission('delete')) {
      return;
    }

    if (!this.facade.canBulkDelete) {
      this.toastr.warning(
        'Chỉ được xóa nhiều bản ghi hợp lệ, không bao gồm bản ghi đã duyệt.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);

    void Swal.fire({
      title: 'Xác nhận xóa hàng loạt',
      text: `Bản ghi đã chọn sẽ bị xóa khỏi hệ thống (${ids.length} bản ghi).`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Hủy',
      confirmButtonText: 'Xóa',
      reverseButtons: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.facade.loading = true;

      this.categoryService.deleteBatch(ids).subscribe({
        next: (response: CategoryBatchActionResponse) => {
          this.facade.loading = false;
          this.facade.loadCategories();

          if ((response.failedCount ?? 0) > 0) {
            const firstError = response.failed?.[0]?.message || 'Xóa có bản ghi thất bại';
            this.toastr.warning(
              `Xóa thành công ${response.successCount}/${response.totalRequested}. ${firstError}`,
              'Cảnh báo',
            );
            return;
          }

          this.toastr.success(`Xóa thành công ${response.successCount} bản ghi`, 'Thành công');
        },
        error: (err) => {
          this.facade.loading = false;
          console.error(err);
          this.toastr.error(err?.error?.message || 'Xóa hàng loạt thất bại', 'Lỗi');
        },
      });
    });
  }

  goCreate(): void {
    if (!this.ensurePermission('create')) {
      return;
    }

    void this.router.navigate(['/categories/create']);
  }

  goEdit(item: Category): void {
    if (!this.ensurePermission('edit') || !item.id) {
      return;
    }

    void this.router.navigate(['/categories', item.id, 'edit']);
  }

  goCopy(item: Category): void {
    if (!this.ensurePermission('copy') || !item.id) {
      return;
    }

    void this.router.navigate(['/categories', item.id, 'copy']);
  }

  goSubmit(item: Category): void {
    if (!this.ensurePermission('submit') || !item.id) {
      return;
    }

    void this.router.navigate(['/categories', item.id, 'submit']);
  }

  goApprove(item: Category): void {
    if (!this.ensurePermission('approve') || !item.id) {
      return;
    }

    void this.router.navigate(['/categories', item.id, 'approve']);
  }

  goCancelApprove(item: Category): void {
    if (!this.ensurePermission('cancelApprove')) {
      return;
    }

    const id = item.id;
    if (id == null) {
      this.toastr.error('Không tìm thấy ID bản ghi', 'Lỗi');
      return;
    }

    void Swal.fire({
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
      if (!result.isConfirmed) {
        return;
      }

      this.categoryService.cancelApprove(id).subscribe({
        next: () => {
          this.toastr.success('Hủy duyệt thành công', 'Thành công');
          this.facade.loadCategories();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error(err?.error?.message || 'Hủy duyệt thất bại', 'Lỗi');
        },
      });
    });
  }

  deleteCategory(item: Category): void {
    if (!this.ensurePermission('delete')) {
      return;
    }

    const id = item.id;
    if (id == null) {
      this.toastr.error('Không tìm thấy ID bản ghi', 'Lỗi');
      return;
    }

    if (!CategoryEntity.fromModel(item).canDelete()) {
      this.toastr.warning('Bản ghi đã duyệt không cho phép xóa', 'Cảnh báo');
      return;
    }

    void Swal.fire({
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
      if (!result.isConfirmed) {
        return;
      }

      this.categoryService.delete(id).subscribe({
        next: () => {
          this.toastr.success('Xóa thành công', 'Thành công');
          this.facade.loadCategories();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error(err?.error?.message || 'Xóa thất bại', 'Lỗi');
        },
      });
    });
  }

  exportExcel(): void {
    if (!this.ensurePermission('export')) {
      return;
    }

    this.categoryService.exportExcel(this.facade.currentFilter.toSearchRequest()).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `category_export_${Date.now()}.xlsx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Export Excel lỗi:', err);
        this.toastr.error('Xuất Excel thất bại', 'Lỗi');
      },
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.facade.setSelectedFile(input.files?.[0] ?? null);
  }

  importExcel(): void {
    if (!this.ensurePermission('import')) {
      return;
    }

    if (!this.facade.selectedFile) {
      this.toastr.warning('Chọn file trước');
      return;
    }

    this.categoryService.importExcel(this.facade.selectedFile, false).subscribe({
      next: () => {
        this.toastr.success('Import thành công');
        this.facade.clearSelectedFile();
        this.facade.loadCategories();
      },
      error: () => {
        this.toastr.error('Import thất bại');
      },
    });
  }

  goDetail(item: Category): void {
    if (!item.id) {
      return;
    }

    void this.router.navigate(['/categories', item.id, 'detail']);
  }

  private executeBatchAction(
    ids: number[],
    requestFactory: () => ReturnType<CategoryService['submitBatch']>,
    title: string,
    actionLabel: string,
  ): void {
    void Swal.fire({
      title,
      text: `Bạn đang thao tác ${ids.length} bản ghi.`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Hủy',
      confirmButtonText: 'Xác nhận',
      reverseButtons: true,
      confirmButtonColor: '#007c7a',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.facade.loading = true;

      requestFactory().subscribe({
        next: (response: CategoryBatchActionResponse) => {
          this.facade.loading = false;
          this.facade.applyBatchResponse(response);

          if ((response.failedCount ?? 0) > 0) {
            const firstError =
              response.failed?.[0]?.message || `${actionLabel} có bản ghi thất bại`;
            this.toastr.warning(
              `${actionLabel} thành công ${response.successCount}/${response.totalRequested}. ${firstError}`,
              'Cảnh báo',
            );
            return;
          }

          this.toastr.success(
            `${actionLabel} thành công ${response.successCount} bản ghi`,
            'Thành công',
          );
        },
        error: (err) => {
          this.facade.loading = false;
          console.error(err);
          this.toastr.error(err?.error?.message || `${actionLabel} thất bại`, 'Lỗi');
        },
      });
    });
  }

  private ensurePermission(action: CategoryAction): boolean {
    if (this.permissionService.can(action)) {
      return true;
    }

    const actionLabel = this.permissionService.getActionLabel(action);
    console.error(`[Category] Không đủ quyền để ${actionLabel}.`, {
      action,
      authorities: this.permissionService.getGrantedAuthorities(),
    });
    this.toastr.error(`Không đủ quyền để ${actionLabel}`, 'Lỗi');
    return false;
  }
}
