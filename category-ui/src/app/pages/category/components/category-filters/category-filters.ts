import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type CategoryFilterForm = {
  paramName: string;
  paramValue: string;
  paramType: string;
  status: string;
  isActive: string;
};

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

  // nhận filter từ parent để sync khi reset / set lại từ ngoài
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
        paramName: this.currentFilters?.paramName ?? '',
        paramValue: this.currentFilters?.paramValue ?? '',
        paramType: this.currentFilters?.paramType ?? '',
        status: this.currentFilters?.status ?? '',
        isActive: this.currentFilters?.isActive ?? '',
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
