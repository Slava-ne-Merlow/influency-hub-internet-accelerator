import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';
import { map, take } from 'rxjs';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRoles = route.data['roles'] as UserRole[];

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (user && expectedRoles.includes(user.role)) {
        return true;
      }
      return router.createUrlTree(['/no-access']);
    })
  );
};
