import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CategoryService } from '../../../services/category.service';
import { Category } from '../../../models/category.models';

type FormMode = 'create' | 'edit' | 'copy';

@Component({
  selector: 'app-category-form-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './category-form-page.html',
  styleUrl: './category-form-page.css',
})
export class CategoryFormPage implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private toastr = inject(ToastrService);
  private categoryService = inject(CategoryService);

  mode: FormMode = 'create';
  id: number | null = null;
  loading = false;

  form = this.fb.group({
    paramName: ['', Validators.required],
    paramValue: ['', Validators.required],
    paramType: ['', Validators.required],
    componentCode: ['', Validators.required],
    effectiveDate: ['', Validators.required],
    endEffectiveDate: [''],
    description: [''],

    status: [1],
    isActive: [1],
    isDisplay: [1],
  });

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] ?? 'create') as FormMode;
    const idParam = this.route.snapshot.paramMap.get('id');
    this.id = idParam ? Number(idParam) : null;

    if (this.mode !== 'create' && this.id) {
      this.loadDetail(this.id);
    }
  }

  loadDetail(id: number): void {
    this.loading = true;
    this.categoryService.getById(id).subscribe({
      next: (data) => {
        this.form.patchValue({
          paramName: this.mode === 'copy' ? '' : (data.paramName ?? ''),
          paramValue: data.paramValue ?? '',
          paramType: data.paramType ?? '',
          componentCode: data.componentCode ?? '',
          effectiveDate: data.effectiveDate ?? '',
          endEffectiveDate: data.endEffectiveDate ?? '',
          description: data.description ?? '',
          status: data.status ?? 1,
          isActive: data.isActive ?? 1,
          isDisplay: data.isDisplay ?? 1,
        });
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.toastr.error('Không tải được chi tiết', 'Lỗi');
      },
    });
  }

  goBack(): void{
    this.router.navigate(['/categories']);
  }

  save(): void{
    if(this.form.invalid){
      this.form.markAllAsTouched();
      this.toastr.warning('Vui lòng nhập đầy đủ các trường bắt buộc', 'Caảnh báo');
      return;
    }

    const payload = this.form.getRawValue() as Category;

    if(this.mode === 'create' || this.mode === 'copy'){
      this.categoryService.create(payload).subscribe({
        next: () => {
          this.toastr.success('Lưu thành công', 'Thành công');
          this.router.navigate(['/categories']);
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Lưu thất bại', 'Lỗi');
        },
      });
      return;
    }

    if(this.id){
      this.categoryService.update(this.id, payload).subscribe({
        next: () => {
          this.toastr.success('Cập nhật thành công', 'Thành công');
          this.router.navigate(['/categories']);
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Cập nhật thất bại', 'Lỗi');
        }
      })
    }


  }

  saveAndSubmit(): void{
    if(this.form.invalid){
      this.form.markAllAsTouched();
      this.toastr.warning('Vui lòng nhập đầy đủ các trường bắt buộc', 'Cảnh báo');
      return;
    }

    const payload = this.form.getRawValue() as Category;

    if (this.mode === 'create' || this.mode === 'copy') {
      this.categoryService.create(payload).subscribe({
        next: (created) => {
          if (!created.id) {
            this.toastr.success('Lưu thành công', 'Thành công');
            this.router.navigate(['/categories']);
            return;
          }

          this.categoryService.submit(created.id).subscribe({
            next: () => {
              this.toastr.success('Lưu và gửi duyệt thành công', 'Thành công');
              this.router.navigate(['/categories']);
            },
            error: (err) => {
              console.error(err);
              this.toastr.error('Gửi duyệt thất bại', 'Lỗi');
            },
          });
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Lưu thất bại', 'Lỗi');
        },
      });
      return;
    }

    if(this.id){
      this.categoryService.update(this.id, payload).subscribe({
        next: () => {
          this.categoryService.submit(this.id!).subscribe({
            next: () => {
              this.toastr.success('Cập nhật và gửi duyệt thành công', 'Thành công');
              this.router.navigate(['/categories']);
            },
            error: (err) => {
              console.error(err);
              this.toastr.error('Gửi duyệt thất bại', 'Lỗi');
            },
          });
        },
        error: (err) => {
          console.error(err);
          this.toastr.error('Cập nhật thất bại', 'Lỗi');
        },
      });
    }
  }

  get title(): string{
    if(this.mode === 'edit') return 'Sửa tham số theo nhóm';
    return 'Thêm mới tham số danh mục theo nhóm';
  }
}
