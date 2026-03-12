import {
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { Category } from '../../../../models/category.models';

@Component({
  selector: 'app-category-form-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category-form-modal.html',
  styleUrl: './category-form-modal.css',
})
export class CategoryFormModalComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private toastr = inject(ToastrService);

  @Input() isOpen = false;
  @Input() mode: 'create' | 'update' | 'copy' = 'create';
  @Input() category: Category | null = null;

  @Output() close = new EventEmitter<void>();
  @Output() save = new EventEmitter<Category>();
  @Output() saveAndSubmit = new EventEmitter<Category>();

  categoryForm = this.fb.group({
    paramName: ['', [Validators.required]],
    paramValue: ['', [Validators.required]],
    paramType: ['', [Validators.required]],
    componentCode: ['', [Validators.required]],
    effectiveDate: ['', [Validators.required]],
    endEffectiveDate: [''],
    description: [''],

    status: [1],
    isActive: [1],
    isDisplay: [1],
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['category'] || changes['isOpen'] || changes['mode']) {
      if (this.isOpen) {
        this.patchForm();
      }
    }
  }

  patchForm(): void {
    if (this.category) {
      this.categoryForm.patchValue({
        paramName: this.category.paramName ?? '',
        paramValue: this.category.paramValue ?? '',
        paramType: this.category.paramType ?? '',
        componentCode: this.category.componentCode ?? '',
        effectiveDate: this.category.effectiveDate ?? '',
        endEffectiveDate: this.category.endEffectiveDate ?? '',
        description: this.category.description ?? '',
        status: this.category.status ?? 1,
        isActive: this.category.isActive ?? 1,
        isDisplay: this.category.isDisplay ?? 1,
      });

      if (this.mode === 'copy') {
        this.categoryForm.patchValue({
          status: 1,
          isDisplay: 1,
        });
      }
    } else {
      this.categoryForm.reset({
        paramName: '',
        paramValue: '',
        paramType: '',
        componentCode: '',
        effectiveDate: '',
        endEffectiveDate: '',
        description: '',
        status: 1,
        isActive: 1,
        isDisplay: 1,
      });
    }

    this.categoryForm.markAsPristine();
    this.categoryForm.markAsUntouched();
  }

  onClose(): void {
    this.close.emit();
  }

  onSave(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.toastr.warning('Vui lòng nhập đầy đủ các trường bắt buộc', 'Cảnh báo');
      return;
    }

    this.save.emit(this.categoryForm.getRawValue() as Category);
  }

  onSaveAndSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.toastr.warning('Vui lòng nhập đầy đủ các trường bắt buộc', 'Cảnh báo');
      return;
    }

    this.saveAndSubmit.emit(this.categoryForm.getRawValue() as Category);
  }

  get title(): string {
    if (this.mode === 'copy') return 'Thêm mới tham số danh mục theo nhóm';
    if (this.mode === 'update') return 'Sửa tham số theo nhóm';
    return 'Thêm mới tham số danh mục theo nhóm';
  }
}
