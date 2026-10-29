import type { StatusEvento } from '../api/types';
import { STATUS_LABEL } from '../utils/format';

/**
 * Status do evento. Um evento ABERTO cujo prazo já venceu não aceita mais alterações
 * (regra prazo × status), então o selo deixa isso explícito em vez de só dizer "Aberto".
 */
export function StatusBadge({
  status,
  prazoVencido = false,
  naCapa = false,
}: {
  status: StatusEvento;
  prazoVencido?: boolean;
  /** Pílula branca com ponto colorido, para ficar legível sobre a capa do evento. */
  naCapa?: boolean;
}) {
  const vencido = status === 'ABERTO' && prazoVencido;
  const classe = vencido ? 'prazo-encerrado' : status.toLowerCase();
  const texto = vencido ? 'Aberto · prazo encerrado' : STATUS_LABEL[status];
  if (naCapa) {
    return (
      <span className={`pilula-status pilula-status-${classe}`}>
        <i aria-hidden="true" />
        {texto}
      </span>
    );
  }
  return <span className={`badge badge-status-${classe}`}>{texto}</span>;
}
