import { CategorySearch } from '../../models/category-search.models';
import { Category } from '../../models/category.models';

export interface CategoryFilterValue {
  paramName: string;
  paramValue: string;
  paramType: string;
  status: string[];
  isActive: string[];
}

export class CategoryFilter {
  static empty(): CategoryFilter {
    return new CategoryFilter();
  }

  constructor(
    readonly paramName = '',
    readonly paramValue = '',
    readonly paramType = '',
    readonly status: string[] = [],
    readonly isActive: string[] = [],
  ) {}

  static fromValue(value?: Partial<CategoryFilterValue> | null): CategoryFilter {
    return new CategoryFilter(
      value?.paramName ?? '',
      value?.paramValue ?? '',
      value?.paramType ?? '',
      Array.isArray(value?.status) ? [...value.status] : [],
      Array.isArray(value?.isActive) ? [...value.isActive] : [],
    );
  }

  toValue(): CategoryFilterValue {
    return {
      paramName: this.paramName,
      paramValue: this.paramValue,
      paramType: this.paramType,
      status: [...this.status],
      isActive: [...this.isActive],
    };
  }

  toRequest(): Partial<CategorySearch> {
    const request: Record<string, unknown> = {};

    if (this.paramName.trim()) {
      request['paramName'] = this.paramName.trim();
    }
    if (this.paramValue.trim()) {
      request['paramValue'] = this.paramValue.trim();
    }
    if (this.paramType.trim()) {
      request['paramType'] = this.paramType.trim();
    }
    if (this.status.length > 0) {
      request['status'] = this.status.map(Number);
    }
    if (this.isActive.length > 0) {
      request['isActive'] = this.isActive.map(Number);
    }

    return request as Partial<CategorySearch>;
  }

  matches(item: Category): boolean {
    const contains = (source: string | undefined, query: string) =>
      !query.trim() || (source ?? '').toLowerCase().includes(query.trim().toLowerCase());

    if (!contains(item.paramName, this.paramName)) {
      return false;
    }
    if (!contains(item.paramValue, this.paramValue)) {
      return false;
    }
    if (!contains(item.paramType, this.paramType)) {
      return false;
    }
    if (this.status.length > 0 && !this.status.some((value) => item.status === Number(value))) {
      return false;
    }
    if (
      this.isActive.length > 0 &&
      !this.isActive.some((value) => item.isActive === Number(value))
    ) {
      return false;
    }

    return true;
  }

  get isEmpty(): boolean {
    return (
      !this.paramName.trim() &&
      !this.paramValue.trim() &&
      !this.paramType.trim() &&
      this.status.length === 0 &&
      this.isActive.length === 0
    );
  }
}
