import { routes } from './app.routes';

describe('Application routes', () => {
  it('exposes Guild registration as a public route', () => {
    const register = routes.find(route => route.path === 'register');
    expect(register).toBeDefined();
    expect(register?.canActivate).toBeUndefined();
  });

  it('keeps every authenticated domain area, including dashboard, under the shared guarded layout', () => {
    const authenticatedLayout = routes.find(route => route.component?.name === 'AuthenticatedLayoutComponent');
    const paths = authenticatedLayout?.children?.map(route => route.path);

    expect(authenticatedLayout?.canActivate).toBeDefined();
    expect(paths).toEqual(jasmine.arrayContaining(['dashboard', 'users', 'members', 'teams', 'events', 'attendance']));
    expect(routes.some(route => route.path === 'dashboard')).toBeFalse();
  });
});
