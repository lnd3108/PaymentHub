import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

import {
  CategoryBatchActionResponse,
  CategoryPageResponse,
  CategoryService,
} from '../../../service/category.service';
import { Category } from '../../../models/category.models';
import { CategoryFiltersComponent } from '../components/category-filters/category-filters';
import { CategoryTableComponent } from '../components/category-table/category-table';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../service/auth.service';

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
  private authService = inject(AuthService);

  private readonly adminRoles = new Set(['ROLE_ADMIN', 'ADMIN', 'SUPER_ADMIN']);
  private readonly categoryScopeKeywords = ['CATEGORY', 'CATEGORIES', 'DANH_MUC', 'THAM_SO', 'PARAM'];
  private readonly actionKeywords: Record<string, string[]> = {
    create: ['CREATE', 'ADD'],
    edit: ['EDIT', 'UPDATE'],
    copy: ['COPY', 'CREATE', 'ADD'],
    submit: ['SUBMIT'],
    approve: ['APPROVE'],
    cancelApprove: ['CANCEL_APPROVE', 'CANCEL-APPROVE', 'CANCELAPPROVE'],
    delete: ['DELETE', 'REMOVE'],
    export: ['EXPORT'],
    import: ['IMPORT'],
  };

  categories: Category[] = [];
  selectedIds = new Set<number>();

  loading = false;
  error = '';

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
    this.loading = true;
    this.error = '';

    this.categoryService.getAll(this.page, this.size).subscribe({
      next: (res) => {
        this.applyPageResponse(res);
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

    this.loading = true;
    this.error = '';

    this.categoryService.search(request, this.page, this.size).subscribe({
      next: (res: any) => {
        this.applyPageResponse({
          content: res?.content ?? [],
          totalElements: res?.totalElements ?? 0,
          totalPages: res?.totalPages ?? 0,
          page: res?.page ?? res?.number ?? 0,
          size: res?.size ?? this.size,
          first: !!res?.first,
          last: !!res?.last,
          empty: !!res?.empty,
        });
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

  onToggleSelectAll(checked: boolean): void {
    for (const item of this.categories) {
      if (item.id == null) continue;

      if (checked) {
        this.selectedIds.add(item.id);
      } else {
        this.selectedIds.delete(item.id);
      }
    }

    this.selectedIds = new Set(this.selectedIds);
  }

  onToggleSelectItem(event: { item: Category; checked: boolean }): void {
    const id = event.item.id;
    if (id == null) return;

    if (event.checked) {
      this.selectedIds.add(id);
    } else {
      this.selectedIds.delete(id);
    }

    this.selectedIds = new Set(this.selectedIds);
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
    if (!this.ensurePermission('delete')) {
      return;
    }

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
          this.toastr.error('Bản ghi đang chờ duyệt không được phép xóa', 'Lỗi');
        },
      });
    });
  }

  //xuất excel
  exportExcel(): void {
    if (!this.ensurePermission('export')) {
      return;
    }

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

    this.categoryService.exportExcel(request).subscribe({
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

  selectedFile: File | null = null;

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
        this.loadCategories();
      },
      error: () => {
        this.toastr.error('Import thất bại');
      },
    });
  }
  private canSubmit(item: Category): boolean {
    return item.status === 1 || item.status === 5 || item.status === 6 || item.status === 7;
  }

  private clearSelection(): void {
    this.selectedIds.clear();
    this.selectedIds = new Set();
  }

  private applyPageResponse(res: CategoryPageResponse): void {
    this.categories = res?.content ?? [];
    this.totalElements = res?.totalElements ?? 0;
    this.totalPages = res?.totalPages ?? 0;
    this.page = res?.page ?? 0;
    this.size = res?.size ?? this.size;
    this.clearSelection();
  }

  private shouldRemainInCurrentList(item: Category): boolean {
    if (this.currentFilters.status !== '' && item.status !== Number(this.currentFilters.status)) {
      return false;
    }

    if (
      this.currentFilters.isActive !== '' &&
      item.isActive !== Number(this.currentFilters.isActive)
    ) {
      return false;
    }

    return true;
  }

  private applyUpdatedStatusRows(updatedRows: { id: number; status: number }[]): void {
    const updatedMap = new Map<number, number>();
    updatedRows.forEach((row) => updatedMap.set(row.id, row.status));

    const touchedRows: Category[] = [];
    const untouchedRows: Category[] = [];
    let removedCount = 0;

    for (const item of this.categories) {
      if (item.id == null || !updatedMap.has(item.id)) {
        untouchedRows.push(item);
        continue;
      }

      const merged: Category = {
        ...item,
        status: updatedMap.get(item.id),
      };

      if (this.shouldRemainInCurrentList(merged)) {
        touchedRows.push(merged);
      } else {
        removedCount += 1;
      }
    }

    this.categories = [...touchedRows, ...untouchedRows];

    if (removedCount > 0) {
      this.totalElements = Math.max(this.totalElements - removedCount, 0);
      this.totalPages = this.totalElements > 0 ? Math.ceil(this.totalElements / this.size) : 0;
    }

    this.clearSelection();
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

      this.loading = true;

      requestFactory().subscribe({
        next: (res: CategoryBatchActionResponse) => {
          this.loading = false;

          this.applyUpdatedStatusRows(res.updated ?? []);

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
          this.loading = false;
          console.error(err);
          this.toastr.error(err?.error?.message || `${actionLabel} thất bại`, 'Lỗi');
        },
      });
    });
  }

  goDetail(item: Category): void {
    if (!item.id) return;
    this.router.navigate(['/categories', item.id, 'detail']);
  }

  private ensurePermission(action: keyof CategoryList['actionKeywords']): boolean {
    if (this.hasPermission(action)) {
      return true;
    }

    const actionLabel = this.getActionLabel(action);
    console.error(`[Category] Không dủ quyền để ${actionLabel}.`, {
      action,
      authorities: this.getGrantedAuthorities(),
      user: this.authService.user(),
    });
    this.toastr.error(`Không dủ quyền để ${actionLabel}`, 'Lỗi');
    return false;
  }

  private hasPermission(action: keyof CategoryList['actionKeywords']): boolean {
    const user = this.authService.user();
    if (!user) {
      return false;
    }

    const normalizedRoles = (user.roles ?? []).map((role) => role.toUpperCase());
    if (normalizedRoles.some((role) => this.adminRoles.has(role))) {
      return true;
    }

    const authorities = this.getGrantedAuthorities();
    if (authorities.length === 0) {
      return true;
    }

    const actionTokens = this.actionKeywords[action] ?? [];

    return authorities.some((authority) => {
      const inCategoryScope = this.categoryScopeKeywords.some((keyword) => authority.includes(keyword));
      const matchesAction = actionTokens.some((token) => authority.includes(token));
      return inCategoryScope && matchesAction;
    });
  }

  private getGrantedAuthorities(): string[] {
    const user = this.authService.user();

    return [...(user?.roles ?? []), ...(user?.authorities ?? [])]
      .filter((value): value is string => !!value)
      .map((value) => value.toUpperCase());
  }

  private handleForbiddenError(err: any, actionLabel: string): boolean {
    if (err?.status !== 403) {
      return false;
    }

    console.error(`[Category] Không đủ quyền để ${actionLabel}.`, err);
    this.toastr.error(`Không dủ quyền để ${actionLabel}`, 'Lỗi');
    return true;
  }

  private getActionLabel(action: keyof CategoryList['actionKeywords']): string {
    switch (action) {
      case 'create':
        return 'thêm mới';
      case 'edit':
        return 'chỉnh sửa';
      case 'copy':
        return 'sao chép';
      case 'submit':
        return 'gửi duyệt';
      case 'approve':
        return 'phê duyệt';
      case 'cancelApprove':
        return 'hủy duyệt';
      case 'delete':
        return 'xóa';
      case 'export':
        return 'xuất Excel';
      case 'import':
        return 'import Excel';
      default:
        return 'thao tác';
    }
  }
}
