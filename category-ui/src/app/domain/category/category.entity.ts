import { Category } from '../../models/category.models';
import { CategoryFilter } from './category-filter';
import { CategoryStatusPolicy } from './category-status';

export class CategoryEntity {
  constructor(private readonly snapshot: Category) {}

  static fromModel(category: Category): CategoryEntity {
    return new CategoryEntity(category);
  }

  get id(): number | null {
    return this.snapshot.id ?? null;
  }

  get raw(): Category {
    return { ...this.snapshot };
  }

  canSubmit(): boolean {
    return CategoryStatusPolicy.canSubmit(this.snapshot.status);
  }

  canApprove(): boolean {
    return CategoryStatusPolicy.canApprove(this.snapshot.status);
  }

  canCancelApprove(): boolean {
    return CategoryStatusPolicy.canCancelApprove(this.snapshot.status);
  }

  canDelete(): boolean {
    return this.snapshot.isDisplay !== 2;
  }

  shouldRemainVisible(filter: CategoryFilter): boolean {
    return filter.matches(this.snapshot);
  }

  withStatus(status: number): CategoryEntity {
    return new CategoryEntity({
      ...this.snapshot,
      status,
    });
  }

  parseNewData(): Category | null {
    try {
      return this.snapshot.newData ? (JSON.parse(this.snapshot.newData) as Category) : null;
    } catch {
      return null;
    }
  }
}
