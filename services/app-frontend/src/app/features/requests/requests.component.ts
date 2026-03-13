import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RequestsService } from './requests.service';
import { AccessRequestDto, ChatDto } from '../../core/models/user.model';
import { PageContainerComponent } from '../../shared/components/page-container/page-container.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/user.model';

@Component({
  selector: 'app-requests',
  standalone: true,
  imports: [CommonModule, DatePipe, PageContainerComponent, LoadingSpinnerComponent],
  templateUrl: './requests.component.html',
  styleUrls: ['./requests.component.scss']
})
export class RequestsComponent implements OnInit {
  private requestsService = inject(RequestsService);
  private authService = inject(AuthService);

  activeTab: 'ACCESS' | 'CHATS' = 'ACCESS';
  isOwner = this.authService.hasRole([UserRole.OWNER]);
  
  isLoading = true;
  accessRequests: AccessRequestDto[] = [];
  chatRequests: ChatDto[] = [];

  ngOnInit() {
    this.loadAccessRequests();
  }

  setTab(tab: 'ACCESS' | 'CHATS') {
    this.activeTab = tab;
    if (tab === 'CHATS' && this.chatRequests.length === 0) {
      this.loadChatRequests();
    }
  }

  private loadAccessRequests() {
    this.isLoading = true;
    this.requestsService.getPendingAccessRequests().subscribe({
      next: (reqs) => {
        this.accessRequests = reqs;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  private loadChatRequests() {
    if (!this.isOwner) return;
    this.isLoading = true;
    this.requestsService.getPendingChatRequests().subscribe({
      next: (reqs) => {
        this.chatRequests = reqs;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  approveAccess(id: string, type: 'APPROVE_3_MONTH' | 'APPROVE_FOREVER') {
    this.requestsService.approveAccessRequest(id, type).subscribe(() => {
      this.accessRequests = this.accessRequests.filter(r => r.id !== id);
    });
  }

  rejectAccess(id: string) {
    this.requestsService.rejectAccessRequest(id).subscribe(() => {
      this.accessRequests = this.accessRequests.filter(r => r.id !== id);
    });
  }

  approveChat(id: string) {
    this.requestsService.approveChatRequest(id).subscribe(() => {
      this.chatRequests = this.chatRequests.filter(r => r.id !== id);
    });
  }

  rejectChat(id: string) {
    this.requestsService.rejectChatRequest(id).subscribe(() => {
      this.chatRequests = this.chatRequests.filter(r => r.id !== id);
    });
  }
}
