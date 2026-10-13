import { HttpErrorResponse } from '@angular/common/http';

export function httpFeedback(error: HttpErrorResponse, action: string, reading = false): string {
  if (error.status === 401) return 'Sua sessão expirou. Faça login novamente.';
  if (error.status === 403) return `Você não tem permissão para ${action}.`;
  if (error.status === 404) return 'O recurso solicitado não está disponível.';
  if (error.status === 400) return 'Confira os dados informados e tente novamente.';
  if (error.status === 409) return 'Os dados mudaram ou estão em conflito. Atualize a lista e confira antes de repetir.';
  if (error.status === 0) return reading
    ? 'Sem conexão para carregar os dados. Tente novamente.'
    : 'Não foi possível confirmar a operação. Atualize a lista antes de repetir.';
  return reading
    ? 'Não foi possível carregar os dados agora. Tente novamente.'
    : 'Não foi possível confirmar a operação. Atualize a lista antes de repetir.';
}
