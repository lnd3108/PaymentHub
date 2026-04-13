import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

import { CategoryBatchActionResponse, CategoryService } from '../../../service/category.service';
import { Category } from '../../../models/category.models';
import { CategoryFiltersComponent } from '../components/category-filters/category-filters';
import { CategoryTableComponent } from '../components/category-table/category-table';
import { FormsModule } from '@angular/forms';
import { CategoryPermissionService } from '../../../service/category-permission.service';
import { CategoryListFacade } from './category-list.facade';
import { CategoryEntity } from '../../../domain/category/category.entity';
import { CategoryFilterValue } from '../../../domain/category/category-filter';

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
  private readonly facade = inject(CategoryListFacade);

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

  selectedFile: File | null = null;

  ngOnInit(): void {
    this.loadCategories();
  }

  get categories(): Category[] {
    return this.facade.categories;
  }

  get selectedIds(): Set<number> {
    return this.facade.selectedIds;
  }

  get loading(): boolean {
    return this.facade.loading;
  }

  get error(): string {
    return this.facade.error;
  }

  get currentFilters() {
    return this.facade.currentFilter.toValue();
  }

  get page(): number {
    return this.facade.page;
  }

  get size(): number {
    return this.facade.size;
  }

  get totalElements(): number {
    return this.facade.totalElements;
  }

  get totalPages(): number {
    return this.facade.totalPages;
  }

  get selectedItems(): Category[] {
    return this.categories.filter((item) => item.id != null && this.selectedIds.has(item.id));
  }

  get selectedCount(): number {
    return this.selectedItems.length;
  }

  get selectedStatuses(): number[] {
    return [
      ...new Set(
        this.selectedItems.map((item) => item.status).filter((v): v is number => v != null),
      ),
    ];
  }

  get hasUniformSelectedStatus(): boolean {
    return this.selectedStatuses.length === 1;
  }

  get allSelectedOnPage(): boolean {
    const ids = this.categories.map((item) => item.id).filter((id): id is number => id != null);
    return ids.length > 0 && ids.every((id) => this.selectedIds.has(id));
  }

  get someSelectedOnPage(): boolean {
    const ids = this.categories.map((item) => item.id).filter((id): id is number => id != null);
    const count = ids.filter((id) => this.selectedIds.has(id)).length;
    return count > 0 && count < ids.length;
  }

  get canBulkSubmit(): boolean {
    return (
      this.selectedCount > 0 &&
      this.hasUniformSelectedStatus &&
      this.selectedItems.every((item) => this.canSubmit(item))
    );
  }

  get canBulkApprove(): boolean {
    return (
      this.selectedCount > 0 &&
      this.hasUniformSelectedStatus &&
      this.selectedItems.every((item) => item.status === 3)
    );
  }

  get canBulkCancelApprove(): boolean {
    return (
      this.selectedCount > 0 &&
      this.hasUniformSelectedStatus &&
      this.selectedItems.every((item) => item.status === 4)
    );
  }

  loadCategories(): void {
    this.facade.load(() => {
      this.toastr.error('Không tải được danh sách category', 'Lỗi');
    });
  }

  searchCategories(): void {
    this.facade.search(undefined, () => {
      this.toastr.error('Tìm kiếm thất bại', 'Lỗi');
    });
  }

  onSearch(filters: CategoryFilterValue): void {
    this.facade.updateFilter(filters);
    this.searchCategories();
  }

  onResetFilters(): void {
    this.facade.resetFilters(() => {
      this.toastr.error('Không tải được danh sách category', 'Lỗi');
    });
  }

  onPageChange(newPage: number): void {
    this.facade.changePage(newPage);
  }

  onSizeChange(newSize: number): void {
    this.facade.changeSize(newSize);
  }

  onToggleSelectAll(checked: boolean): void {
    for (const item of this.categories) {
      if (item.id == null) continue;

      if (checked) {
        this.selectedIds.add(item.id);
      } else {
        this.selectedIds.delete(item.id);
      }
    }

    this.facade.selectedIds = new Set(this.selectedIds);
  }

  onToggleSelectItem(event: { item: Category; checked: boolean }): void {
    const id = event.item.id;
    if (id == null) return;

    if (event.checked) {
      this.selectedIds.add(id);
    } else {
      this.selectedIds.delete(id);
    }

    this.facade.selectedIds = new Set(this.selectedIds);
  }

  bulkSubmitSelected(): void {
    if (!this.ensurePermission('submit')) {
      return;
    }

    if (!this.canBulkSubmit) {
      this.toastr.warning(
        'Chỉ được gửi duyệt nhiều bản ghi khi các bản ghi cùng trạng thái hợp lệ.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.selectedItems.map((item) => item.id!).filter(Boolean);
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

    if (!this.canBulkApprove) {
      this.toastr.warning(
        'Chỉ được phê duyệt nhiều bản ghi khi tất cả đang ở trạng thái chờ phê duyệt.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.selectedItems.map((item) => item.id!).filter(Boolean);
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

    if (!this.canBulkCancelApprove) {
      this.toastr.warning(
        'Chỉ được hủy duyệt nhiều bản ghi khi tất cả đang ở trạng thái đã phê duyệt.',
        'Cảnh báo',
      );
      return;
    }

    const ids = this.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(
      ids,
      () => this.categoryService.cancelApproveBatch(ids),
      'Xác nhận hủy duyệt',
      'Hủy duyệt',
    );
  }

  goCreate(): void {
    if (!this.ensurePermission('create')) {
      return;
    }

    this.router.navigate(['/categories/create']);
  }

  goEdit(item: Category): void {
    if (!this.ensurePermission('edit')) {
      return;
    }

    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'edit']);
  }

  goCopy(item: Category): void {
    if (!this.ensurePermission('copy')) {
      return;
    }

    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'copy']);
  }

  goSubmit(item: Category): void {
    if (!this.ensurePermission('submit')) {
      return;
    }

    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'submit']);
  }

  goApprove(item: Category): void {
    if (!this.ensurePermission('approve')) {
      return;
    }

    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'approve']);
  }

  goCancelApprove(item: Category): void {
    if (!this.ensurePermission('cancelApprove')) {
      return;
    }

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
          this.facade.reloadCurrentView();
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
      this.toastr.error('Không tìm thấy id bản ghi', 'Lỗi');
      return;
    }

    if (item.isDisplay === 2) {
      this.toastr.warning('Bản ghi đã duyệt không cho phép xóa', 'Cảnh báo');
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
          this.facade.reloadCurrentView();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Bản ghi đang chờ duyệt không được phép xóa', 'Lỗi');
        },
      });
    });
  }

  exportExcel(): void {
    if (!this.ensurePermission('export')) {
      return;
    }

    this.categoryService.exportExcel(this.facade.currentFilter.toRequest()).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;

        const fileName = `category_export_${new Date().getTime()}.xlsx`;
        link.setAttribute('download', fileName);

        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Export Excel lỗi:', err);
        console.error('Export error body:', err?.error);
        this.toastr.error('Xuất Excel thất bại', 'Lỗi');
      },
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  importExcel() {
    if (!this.ensurePermission('import')) {
      return;
    }

    if (!this.selectedFile) {
      this.toastr.warning('Chọn file trước');
      return;
    }

    this.categoryService.importExcel(this.selectedFile, false).subscribe({
      next: () => {
        this.toastr.success('Import thành công');
        this.selectedFile = null;
        this.facade.reloadCurrentView();
      },
      error: () => {
        this.toastr.error('Import thất bại');
      },
    });
  }

  goDetail(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'detail']);
  }

  private canSubmit(item: Category): boolean {
    return CategoryEntity.from(item).canSubmit();
  }

  private executeBatchAction(
    ids: number[],
    requestFactory: () => any,
    title: string,
    actionLabel: string,
  ): void {
    Swal.fire({
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
      if (!result.isConfirmed) return;

      this.facade.loading = true;

      requestFactory().subscribe({
        next: (res: CategoryBatchActionResponse) => {
          this.facade.loading = false;
          this.facade.applyBatchResult(res);

          if ((res.failedCount ?? 0) > 0) {
            const firstError = res.failed?.[0]?.message || `${actionLabel} có bản ghi thất bại`;
            this.toastr.warning(
              `${actionLabel} thành công ${res.successCount}/${res.totalRequested}. ${firstError}`,
              'Cảnh báo',
            );
            return;
          }

          this.toastr.success(
            `${actionLabel} thành công ${res.successCount} bản ghi`,
            'Thành công',
          );
        },
        error: (err: any) => {
          this.facade.loading = false;
          console.error(err);
          this.toastr.error(err?.error?.message || `${actionLabel} thất bại`, 'Lỗi');
        },
      });
    });
  }

  private ensurePermission(action: Parameters<CategoryPermissionService['can']>[0]): boolean {
    if (this.permissionService.can(action)) {
      return true;
    }

    const actionLabel = this.permissionService.getDeniedMessage(action);
    console.error(`[Category] Không đủ quyền để ${actionLabel}.`, {
      action,
      authorities: this.permissionService.getGrantedAuthorities(),
    });
    this.toastr.error(`Không đủ quyền để ${actionLabel}`, 'Lỗi');
    return false;
  }
}
