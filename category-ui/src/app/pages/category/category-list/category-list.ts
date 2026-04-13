import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import { CategoryEntity } from '../../../domain/category/category.entity';
import { CategoryFilterForm } from '../../../domain/category/category-filter';
import { Category } from '../../../models/category.models';
import { CategoryPermissionService, CategoryAction } from '../../../service/category-permission.service';
import {
  CategoryBatchActionResponse,
  CategoryService,
} from '../../../service/category.service';
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
        'Chi duoc gui duyet nhieu ban ghi khi cac ban ghi cung trang thai hop le.',
        'Canh bao',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(ids, () => this.categoryService.submitBatch(ids), 'Xac nhan gui duyet', 'Gui duyet');
  }

  bulkApproveSelected(): void {
    if (!this.ensurePermission('approve')) {
      return;
    }

    if (!this.facade.canBulkApprove) {
      this.toastr.warning(
        'Chi duoc phe duyet nhieu ban ghi khi tat ca dang o trang thai cho phe duyet.',
        'Canh bao',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(ids, () => this.categoryService.approveBatch(ids), 'Xac nhan phe duyet', 'Phe duyet');
  }

  bulkCancelApproveSelected(): void {
    if (!this.ensurePermission('cancelApprove')) {
      return;
    }

    if (!this.facade.canBulkCancelApprove) {
      this.toastr.warning(
        'Chi duoc huy duyet nhieu ban ghi khi tat ca dang o trang thai da phe duyet.',
        'Canh bao',
      );
      return;
    }

    const ids = this.facade.selectedItems.map((item) => item.id!).filter(Boolean);
    this.executeBatchAction(
      ids,
      () => this.categoryService.cancelApproveBatch(ids),
      'Xac nhan huy duyet',
      'Huy duyet',
    );
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
      this.toastr.error('Khong tim thay id ban ghi', 'Loi');
      return;
    }

    void Swal.fire({
      title: 'Xac nhan huy duyet',
      text: 'Ban ghi nay se chuyen sang trang thai huy duyet.',
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Huy',
      confirmButtonText: 'Xac nhan',
      reverseButtons: true,
      confirmButtonColor: '#007c7a',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.categoryService.cancelApprove(id).subscribe({
        next: () => {
          this.toastr.success('Huy duyet thanh cong', 'Thanh cong');
          this.facade.loadCategories();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error(err?.error?.message || 'Huy duyet that bai', 'Loi');
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
      this.toastr.error('Khong tim thay id ban ghi', 'Loi');
      return;
    }

    if (!CategoryEntity.fromModel(item).canDelete()) {
      this.toastr.warning('Ban ghi da duyet khong cho phep xoa', 'Canh bao');
      return;
    }

    void Swal.fire({
      title: 'Xac nhan xoa',
      text: 'Ban ghi nay se bi xoa khoi he thong.',
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Huy',
      confirmButtonText: 'Xoa',
      reverseButtons: true,
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
    }).then((result) => {
      if (!result.isConfirmed) {
        return;
      }

      this.categoryService.delete(id).subscribe({
        next: () => {
          this.toastr.success('Xoa thanh cong', 'Thanh cong');
          this.facade.loadCategories();
        },
        error: (err) => {
          console.error(err);
          this.toastr.error(err?.error?.message || 'Xoa that bai', 'Loi');
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
        console.error('Export Excel loi:', err);
        this.toastr.error('Xuat Excel that bai', 'Loi');
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
      this.toastr.warning('Chon file truoc');
      return;
    }

    this.categoryService.importExcel(this.facade.selectedFile, false).subscribe({
      next: () => {
        this.toastr.success('Import thanh cong');
        this.facade.clearSelectedFile();
        this.facade.loadCategories();
      },
      error: () => {
        this.toastr.error('Import that bai');
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
      text: `Ban dang thao tac ${ids.length} ban ghi.`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Huy',
      confirmButtonText: 'Xac nhan',
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
            const firstError = response.failed?.[0]?.message || `${actionLabel} co ban ghi that bai`;
            this.toastr.warning(
              `${actionLabel} thanh cong ${response.successCount}/${response.totalRequested}. ${firstError}`,
              'Canh bao',
            );
            return;
          }

          this.toastr.success(`${actionLabel} thanh cong ${response.successCount} ban ghi`, 'Thanh cong');
        },
        error: (err) => {
          this.facade.loading = false;
          console.error(err);
          this.toastr.error(err?.error?.message || `${actionLabel} that bai`, 'Loi');
        },
      });
    });
  }

  private ensurePermission(action: CategoryAction): boolean {
    if (this.permissionService.can(action)) {
      return true;
    }

    const actionLabel = this.permissionService.getActionLabel(action);
    console.error(`[Category] Khong du quyen de ${actionLabel}.`, {
      action,
      authorities: this.permissionService.getGrantedAuthorities(),
    });
    this.toastr.error(`Khong du quyen de ${actionLabel}`, 'Loi');
    return false;
  }
}
