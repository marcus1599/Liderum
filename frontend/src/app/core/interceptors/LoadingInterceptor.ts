import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoadingService } from '../../services/LoadingService';
import { SHOW_GLOBAL_LOADING } from './loading-context';

let activeRequests = 0;

export const LoadingInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.context.get(SHOW_GLOBAL_LOADING)) return next(req);
  const loadingService = inject(LoadingService);

  activeRequests++;
  loadingService.setLoading(true);

  return next(req).pipe(
    finalize(() => {
      activeRequests--;

      if (activeRequests === 0) {
        loadingService.setLoading(false);
      }
    })
  );
};
