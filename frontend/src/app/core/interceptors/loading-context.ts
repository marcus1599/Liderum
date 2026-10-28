import { HttpContext, HttpContextToken } from '@angular/common/http';

export const SHOW_GLOBAL_LOADING = new HttpContextToken<boolean>(() => true);

export function localLoadingContext(): HttpContext {
  return new HttpContext().set(SHOW_GLOBAL_LOADING, false);
}
