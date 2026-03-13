import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { UserRole } from '../../../core/models/user.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth-init',
  standalone: true,
  imports: [CommonModule, LoadingSpinnerComponent],
  templateUrl: './auth-init.component.html',
  styleUrls: ['./auth-init.component.scss']
})
export class AuthInitComponent implements OnInit {
  private authService = inject(AuthService);
  private tgService = inject(TelegramService);
  private router = inject(Router);

  isLoading = true;
  error = false;

  ngOnInit() {
    this.tgService.ready();
    this.authService.login().subscribe({
      next: (res) => {
        if (res.user.role === UserRole.USER) {
          this.router.navigate(['/no-access']);
        } else {
          this.router.navigate(['/statistics']);
        }
      },
      error: () => {
        this.isLoading = false;
        this.error = true;
      }
    });
  }
}
