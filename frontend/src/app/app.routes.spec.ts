import { routes } from './app.routes';

describe('Application routes', () => {
  it('exposes Guild registration as a public route', () => {
    const register = routes.find(route => route.path === 'register');
    expect(register).toBeDefined();
    expect(register?.canActivate).toBeUndefined();
  });
});
