import { HttpErrorResponse } from '@angular/common/http';
import { httpFeedback } from './http-feedback';

describe('httpFeedback', () => {
  const statuses = [0, 400, 401, 403, 404, 409, 500];

  for (const status of statuses) {
    it(`uses a safe local message for ${status}`, () => {
      const error = new HttpErrorResponse({
        status,
        error: { message: 'SQL password=secret token=private' }
      });
      const message = httpFeedback(error, 'consultar membros', status !== 400);
      expect(message).toBeTruthy();
      expect(message).not.toContain('secret');
      expect(message).not.toContain('private');
      expect(message).not.toContain('SQL');
    });
  }

  it('distinguishes permission, missing resource, conflict and connection failure', () => {
    const message = (status: number) => httpFeedback(new HttpErrorResponse({ status }), 'salvar grupo');
    expect(message(403)).toContain('permissão');
    expect(message(404)).toContain('não está disponível');
    expect(message(409)).toContain('conflito');
    expect(message(0)).toContain('Atualize a lista');
  });
});
