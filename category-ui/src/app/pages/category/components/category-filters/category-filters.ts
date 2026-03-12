import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-filters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-filters.html',
  styleUrl: './category-filters.css',
})
export class CategoryFiltersComponent {
  @Input() statusOptions: { value: string; label: string }[] = [];
  @Input() activeOptions: { value: string; label: string }[] = [];

  @Output() search = new EventEmitter<{
    paramName: string;
    paramValue: string;
    paramType: string;
    status: string;
    isActive: string;
  }>();

  @Output() reset = new EventEmitter<void>();

  filters = {
    paramName: '',
    paramValue: '',
    paramType: '',
    status: '',
    isActive: '',
  };

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
