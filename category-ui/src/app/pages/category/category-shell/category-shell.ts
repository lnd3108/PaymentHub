import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
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

  protected readonly user = this.authService.user;

  protected logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
