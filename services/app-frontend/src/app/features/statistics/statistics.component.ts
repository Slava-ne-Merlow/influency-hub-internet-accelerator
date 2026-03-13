import { Component, OnInit, inject } from '@angular/core';
import { StatisticsService } from './statistics.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/user.model';
import { forkJoin, of } from 'rxjs';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PageContainerComponent } from '../../shared/components/page-container/page-container.component';
import { StatsCardComponent } from '../../shared/components/stats-card/stats-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule, NgIf, NgFor, RouterModule, PageContainerComponent, StatsCardComponent, LoadingSpinnerComponent],
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit {
  private statsService = inject(StatisticsService);
  private authService = inject(AuthService);

  stats: any = {};
  isLoading = true;
  isOwner = this.authService.hasRole([UserRole.OWNER]);

  ngOnInit() {
    forkJoin({
      pendingRequests: this.statsService.getPendingRequestsCount(),
      chatRequests: this.isOwner ? this.statsService.getChatRequestsCount() : of(0),
      vpnSummary: this.statsService.getVpnSummary(),
      users: this.statsService.getUsersCount()
    }).subscribe({
      next: (data) => {
        this.stats = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
