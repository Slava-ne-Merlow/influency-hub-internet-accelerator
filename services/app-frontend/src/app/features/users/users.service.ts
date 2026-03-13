import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDto } from '../../core/models/user.model';
import { API_URL } from '../../core/tokens/api.token';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private http = inject(HttpClient);
  private apiUrl = inject(API_URL);

  getAllUsers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${this.apiUrl}/api/users`);
  }

  grantManualAccess(id: string, accessType: 'THREE_MONTHS' | 'FOREVER'): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.apiUrl}/api/users/${id}/manual-access/grant`, { accessType });
  }

  revokeManualAccess(id: string): Observable<UserDto> {
    return this.http.post<UserDto>(`${this.apiUrl}/api/users/${id}/manual-access/revoke`, {});
  }
}
