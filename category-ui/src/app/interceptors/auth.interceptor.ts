import {
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { AuthService } from '../service/auth.service';

const ACCESS_TOKEN_HEADER = 'X-Access-Token';
const AUTH_BYPASS_PATHS = ['/auth/login', '/auth/register'];

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();
  const shouldSkipAuthorization = AUTH_BYPASS_PATHS.some((path) => req.url.includes(path));

  const request = token && !shouldSkipAuthorization
    ? req.clone({
        withCredentials: true,
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    : req.clone({
        withCredentials: true,
      });

  return next(request).pipe(
    tap((event) => {
      if (!(event instanceof HttpResponse)) {
        return;
      }

      const refreshedAccessToken = event.headers.get(ACCESS_TOKEN_HEADER);
      if (refreshedAccessToken) {
        authService.updateAccessToken(refreshedAccessToken);
      }
    }),
  );
};
