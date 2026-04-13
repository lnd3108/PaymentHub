import { Category } from '../../models/category.models';
import { CategorySearch } from '../../models/category-search.models';

export interface CategoryFilterForm {
  paramName: string;
  paramValue: string;
  paramType: string;
  status: string;
  isActive: string;
}

export type CategorySearchRequest = Partial<
  Omit<CategorySearch, 'status' | 'isActive'>
> & {
  status?: number[];
  isActive?: number[];
};

export class CategoryFilter {
  constructor(
    readonly paramName = '',
    readonly paramValue = '',
    readonly paramType = '',
    readonly status = '',
    readonly isActive = '',
  ) {}

  static empty(): CategoryFilter {
    return new CategoryFilter();
  }

  static fromForm(form: Partial<CategoryFilterForm> | null | undefined): CategoryFilter {
    return new CategoryFilter(
      form?.paramName ?? '',
      form?.paramValue ?? '',
      form?.paramType ?? '',
      form?.status ?? '',
      form?.isActive ?? '',
    );
  }

  toForm(): CategoryFilterForm {
    return {
      paramName: this.paramName,
      paramValue: this.paramValue,
      paramType: this.paramType,
      status: this.status,
      isActive: this.isActive,
    };
  }

  toSearchRequest(): CategorySearchRequest {
    const request: CategorySearchRequest = {};

    if (this.paramName.trim()) {
      request.paramName = this.paramName.trim();
    }

    if (this.paramValue.trim()) {
      request.paramValue = this.paramValue.trim();
    }

    if (this.paramType.trim()) {
      request.paramType = this.paramType.trim();
    }

    if (this.status !== '') {
      request.status = [Number(this.status)];
    }

    if (this.isActive !== '') {
      request.isActive = [Number(this.isActive)];
    }

    return request;
  }

  matches(category: Category): boolean {
    if (this.status !== '' && category.status !== Number(this.status)) {
      return false;
    }

    if (this.isActive !== '' && category.isActive !== Number(this.isActive)) {
      return false;
    }

    return true;
  }
}
