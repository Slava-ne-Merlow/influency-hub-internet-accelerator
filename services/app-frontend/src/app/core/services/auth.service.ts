import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, of, throwError } from 'rxjs';
import { UserDto, UserRole } from '../models/user.model';
import { TelegramService } from './telegram.service';
import { API_URL } from '../tokens/api.token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private tgService = inject(TelegramService);
  private apiUrl = inject(API_URL);
  private readonly TOKEN_KEY = 'access_token';

  private user$ = new BehaviorSubject<UserDto | null>(null);
  currentUser$ = this.user$.asObservable();

  login(): Observable<{accessToken: string, user: UserDto}> {
    const initData = this.tgService.initData;
    // For local development when not in Telegram, we might need a fallback or just fail
    return this.http.post<{accessToken: string, user: UserDto}>(`${this.apiUrl}/api/auth/telegram`, { initData }).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.accessToken);
        this.user$.next(res.user);
      })
    );
  }
  
  fetchMe(): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}/api/users/me`).pipe(
      tap(user => this.user$.next(user))
    );
  }

  get token(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  hasRole(roles: UserRole[]): boolean {
    const user = this.user$.value;
    return user ? roles.includes(user.role) : false;
  }
  
  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    this.user$.next(null);
  }
}
