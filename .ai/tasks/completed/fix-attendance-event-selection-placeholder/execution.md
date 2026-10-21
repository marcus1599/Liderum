# Execution — Attendance Event Selection Placeholder

- Normalizada a entrada do `ngModelChange`: vazio, `null` e string `null` agora representam ausência de seleção.
- IDs positivos inteiros continuam carregando presenças normalmente.
- Nenhum backend, contrato HTTP, RBAC ou tenant foi alterado.
- Spec direcionada: 11/11 SUCCESS.
- Suíte Angular: 97/97 SUCCESS.
- Build Angular: SUCCESS; warnings Sass e budgets preexistentes não bloqueantes.
