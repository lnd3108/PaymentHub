import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryFilterValue } from '../../../../domain/category/category-filter';

export type CategoryFilterForm = CategoryFilterValue;

@Component({
  selector: 'app-category-filters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-filters.html',
  styleUrl: './category-filters.css',
})
export class CategoryFiltersComponent implements OnChanges {
  @Input() statusOptions: { value: string; label: string }[] = [];
  @Input() activeOptions: { value: string; label: string }[] = [];

  @Input() currentFilters: CategoryFilterForm = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: [],
    isActive: [],
  };

  @Output() search = new EventEmitter<CategoryFilterForm>();
  @Output() reset = new EventEmitter<void>();

  filters: CategoryFilterForm = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: [],
    isActive: [],
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentFilters']) {
      this.filters = {
        paramName: this.currentFilters?.paramName ?? '',
        paramValue: this.currentFilters?.paramValue ?? '',
        paramType: this.currentFilters?.paramType ?? '',
        status: Array.isArray(this.currentFilters?.status) ? [...this.currentFilters.status] : [],
        isActive: Array.isArray(this.currentFilters?.isActive)
          ? [...this.currentFilters.isActive]
          : [],
      };
    }
  }

  onSearch(): void {
    this.search.emit({
      paramName: this.filters.paramName.trim(),
      paramValue: this.filters.paramValue.trim(),
      paramType: this.filters.paramType.trim(),
      status: [...this.filters.status],
      isActive: [...this.filters.isActive],
    });
  }

  onReset(): void {
    this.filters = {
      paramName: '',
      paramValue: '',
      paramType: '',
      status: [],
      isActive: [],
    };

    this.reset.emit();
  }

  onToggleStatus(value: string, checked: boolean): void {
    if (!value) {
      if (checked) {
        this.filters = {
          ...this.filters,
          status: [],
        };
      }
      return;
    }

    if (checked) {
      if (!this.filters.status.includes(value)) {
        this.filters = {
          ...this.filters,
          status: [...this.filters.status, value],
        };
      }
      return;
    }

    this.filters = {
      ...this.filters,
      status: this.filters.status.filter((item) => item !== value),
    };
  }

  onToggleIsActive(value: string, checked: boolean): void {
    if (!value) {
      if (checked) {
        this.filters = {
          ...this.filters,
          isActive: [],
        };
      }
      return;
    }

    if (checked) {
      if (!this.filters.isActive.includes(value)) {
        this.filters = {
          ...this.filters,
          isActive: [...this.filters.isActive, value],
        };
      }
      return;
    }

    this.filters = {
      ...this.filters,
      isActive: this.filters.isActive.filter((item) => item !== value),
    };
  }

  isStatusChecked(value: string): boolean {
    if (!value) {
      return this.filters.status.length === 0;
    }

    return this.filters.status.includes(value);
  }

  isIsActiveChecked(value: string): boolean {
    if (!value) {
      return this.filters.isActive.length === 0;
    }

    return this.filters.isActive.includes(value);
  }
}
