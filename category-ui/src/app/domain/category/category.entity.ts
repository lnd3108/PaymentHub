import { Category } from '../../models/category.models';
import {
  CategoryStatus,
  getCategoryStatusClass,
  getCategoryStatusLabel,
} from './category-status';

export class CategoryEntity {
  constructor(private readonly data: Category) {}

  static from(data: Category): CategoryEntity {
    return new CategoryEntity(data);
  }

  toJSON(): Category {
    return { ...this.data };
  }

  get id(): number | undefined {
    return this.data.id;
  }

  get status(): number | undefined {
    return this.data.status;
  }

  get isActive(): number | undefined {
    return this.data.isActive;
  }

  canSubmit(): boolean {
    return (
      this.status === CategoryStatus.Draft ||
      this.status === CategoryStatus.Rejected ||
      this.status === 6 ||
      this.status === CategoryStatus.CancelApproved
    );
  }

  canApprove(): boolean {
    return this.status === CategoryStatus.PendingApproval;
  }

  canCancelApprove(): boolean {
    return this.status === CategoryStatus.Approved;
  }

  get statusLabel(): string {
    return getCategoryStatusLabel(this.status);
  }

  get statusClass(): string {
    return getCategoryStatusClass(this.status);
  }

  get parsedNewData(): Category | null {
    try {
      return this.data.newData ? (JSON.parse(this.data.newData) as Category) : null;
    } catch {
      return null;
    }
  }
}
