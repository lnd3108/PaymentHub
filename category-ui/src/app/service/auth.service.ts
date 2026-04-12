import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { map, tap } from 'rxjs';
import { LoginRequest, LoginResponse, MeResponse } from '../models/auth.models';
import { ApiResponse } from './category.service';

const AUTH_STORAGE_KEY = 'paymenthub.auth';

export interface AuthSession {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: MeResponse;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = 'http://localhost:8094/api/jpa/auth';
  private readonly sessionState = signal<AuthSession | null>(this.readSession());

  readonly session = this.sessionState.asReadonly();
  readonly user = computed(() => this.sessionState()?.user ?? null);
  readonly isAuthenticated = computed(() => !!this.sessionState()?.accessToken);

  login(payload: LoginRequest) {
    return this.http
      .post<ApiResponse<LoginResponse> | LoginResponse>(`${this.apiBaseUrl}/login`, payload)
      .pipe(
        map((response) => ('data' in response ? response.data : response)),
        tap((response) => this.persistSession(response)),
      );
  }

  logout(): void {
    this.sessionState.set(null);
    localStorage.removeItem(AUTH_STORAGE_KEY);
  }

  getAccessToken(): string | null {
    return this.sessionState()?.accessToken ?? null;
  }

  private persistSession(response: LoginResponse): void {
    const session: AuthSession = {
      accessToken: response.accessToken,
      tokenType: response.tokenType,
      expiresIn: response.expiresIn,
      user: response.user,
    };

    this.sessionState.set(session);
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
  }

  private readSession(): AuthSession | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const rawSession = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!rawSession) {
      return null;
    }

    try {
      return JSON.parse(rawSession) as AuthSession;
    } catch {
      localStorage.removeItem(AUTH_STORAGE_KEY);
      return null;
    }
  }
}
