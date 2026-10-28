import type { StatusEvento } from '../api/types';
import { STATUS_LABEL } from '../utils/format';

/**
 * Status do evento. Um evento ABERTO cujo prazo já venceu não aceita mais alterações
 * (regra prazo × status), então o selo deixa isso explícito em vez de só dizer "Aberto".
 */
export function StatusBadge({ status, prazoVencido = false }: { status: StatusEvento; prazoVencido?: boolean }) {
  if (status === 'ABERTO' && prazoVencido) {
    return <span className="badge badge-status-prazo-encerrado">Aberto · prazo encerrado</span>;
  }
  return <span className={`badge badge-status-${status.toLowerCase()}`}>{STATUS_LABEL[status]}</span>;
}
