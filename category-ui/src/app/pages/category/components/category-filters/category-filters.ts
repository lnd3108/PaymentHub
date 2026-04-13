import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CategoryFilterForm } from '../../../../domain/category/category-filter';

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
    status: '',
    isActive: '',
  };

  @Output() search = new EventEmitter<CategoryFilterForm>();
  @Output() reset = new EventEmitter<void>();

  filters: CategoryFilterForm = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: '',
    isActive: '',
  };

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentFilters']) {
      this.filters = {
        paramName: this.currentFilters.paramName ?? '',
        paramValue: this.currentFilters.paramValue ?? '',
        paramType: this.currentFilters.paramType ?? '',
        status: this.currentFilters.status ?? '',
        isActive: this.currentFilters.isActive ?? '',
      };
    }
  }

  onSearch(): void {
    this.search.emit({ ...this.filters });
  }

  onReset(): void {
    this.filters = {
      paramName: '',
      paramValue: '',
      paramType: '',
      status: '',
      isActive: '',
    };
    this.reset.emit();
  }
}
