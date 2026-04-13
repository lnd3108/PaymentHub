import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../service/auth.service';

@Component({
  selector: 'app-category-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './category-shell.html',
  styleUrl: './category-shell.css',
})
export class CategoryShell {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastr = inject(ToastrService);

  protected readonly user = this.authService.user;
  protected isLoggingOut = false;

  protected logout(): void {
    if (this.isLoggingOut) {
      return;
    }

    this.isLoggingOut = true;

    this.authService.logout().subscribe({
      next: () => {
        this.toastr.success('Đăng xuất thành công', 'Thành công');
        void this.router.navigate(['/login']);
      },
      error: (error) => {
        const message = error?.error?.message || 'Phiên làm việc đã được xóa ở máy khách.';
        this.toastr.warning(message, 'Đăng xuất có cảnh báo');
        void this.router.navigate(['/login']);
      },
      complete: () => {
        this.isLoggingOut = false;
      },
    });
  }
}
