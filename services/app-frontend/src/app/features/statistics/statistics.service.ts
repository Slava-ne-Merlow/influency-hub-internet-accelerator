import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VpnSummaryDto } from '../../core/models/user.model';
import { map } from 'rxjs/operators';
import { API_URL } from '../../core/tokens/api.token';

@Injectable({ providedIn: 'root' })
export class StatisticsService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getPendingRequestsCount(): Observable<number> {
    return this.http.get<any[]>(`${this.apiUrl}/api/requests/pending`).pipe(
      map(requests => requests.length)
    );
  }

  getChatRequestsCount(): Observable<number> {
    return this.http.get<any[]>(`${this.apiUrl}/api/chat-requests`).pipe(
      map(requests => requests.length)
    );
  }

  getUsersCount(): Observable<{total: number}> {
    return this.http.get<any[]>(`${this.apiUrl}/api/users`).pipe(
      map(users => ({ total: users.length }))
    );
  }

  getVpnSummary(): Observable<VpnSummaryDto> {
    return this.http.get<VpnSummaryDto>(`${this.apiUrl}/api/vpn/summary`);
  }
}
