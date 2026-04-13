import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../service/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.html',
  styleUrl: './login-page.css',
})
export class LoginPage {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toastr = inject(ToastrService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected isSubmitting = false;
  protected showPassword = false;

  protected submit(): void {
    if (this.form.invalid || this.isSubmitting) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    this.authService
      .login(this.form.getRawValue())
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.toastr.success('Đăng nhập thành công', 'Thành công');
          const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');
          const safeRedirect = redirectTo?.startsWith('/') ? redirectTo : '/categories';
          void this.router.navigateByUrl(safeRedirect);
        },
        error: (error) => {
          const message = error?.error?.message || 'Đăng nhập thất bại, vui lòng thử lại!';
          this.toastr.error(message, 'Lỗi đăng nhập thất bại');
        },
      });
  }

  protected togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  protected hasError(controlName: 'email' | 'password', errorCode: string): boolean {
    return this.control(controlName).hasError(errorCode) && this.control(controlName).touched;
  }

  private control(controlName: 'email' | 'password'): AbstractControl {
    return this.form.controls[controlName];
  }
}
