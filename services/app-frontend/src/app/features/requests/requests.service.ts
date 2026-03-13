import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccessRequestDto, ChatDto } from '../../core/models/user.model';
import { API_URL } from '../../core/tokens/api.token';

@Injectable({ providedIn: 'root' })
export class RequestsService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getPendingAccessRequests(): Observable<AccessRequestDto[]> {
    return this.http.get<AccessRequestDto[]>(`${this.apiUrl}/api/requests/pending`);
  }

  approveAccessRequest(id: string, decisionType: 'APPROVE_3_MONTH' | 'APPROVE_FOREVER'): Observable<AccessRequestDto> {
    return this.http.post<AccessRequestDto>(`${this.apiUrl}/api/requests/${id}/approve`, { decisionType });
  }

  rejectAccessRequest(id: string): Observable<AccessRequestDto> {
    return this.http.post<AccessRequestDto>(`${this.apiUrl}/api/requests/${id}/reject`, {});
  }

  getPendingChatRequests(): Observable<ChatDto[]> {
    return this.http.get<ChatDto[]>(`${this.apiUrl}/api/chat-requests`);
  }

  approveChatRequest(id: string): Observable<ChatDto> {
    return this.http.post<ChatDto>(`${this.apiUrl}/api/chat-requests/${id}/approve`, {});
  }

  rejectChatRequest(id: string): Observable<ChatDto> {
    return this.http.post<ChatDto>(`${this.apiUrl}/api/chat-requests/${id}/reject`, {});
  }
}
