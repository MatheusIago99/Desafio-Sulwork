import type { Evento } from '../api/types';
import { formatarDataHora } from '../utils/format';
import { prazoProximo, tempoRestante } from '../utils/prazo';
import { IconeAmpulheta } from './Icones';

/**
 * Prazo de alterações do evento e quanto tempo falta. Enquanto o evento aceita
 * contribuições, mostra a contagem; depois, diz por que não aceita mais.
 */
export function CaixaPrazo({ evento }: { evento: Evento }) {
  const restante = evento.aceitaAlteracoes ? tempoRestante(evento.prazoAlteracoes) : null;
  let situacao: string | null = null;
  if (evento.status === 'ABERTO' && evento.prazoVencido) situacao = 'Prazo encerrado';
  else if (evento.status !== 'ABERTO' && evento.status !== 'RASCUNHO') situacao = 'Sem alterações';

  return (
    <div className="caixa-prazo">
      <IconeAmpulheta />
      <span>Alterações até {formatarDataHora(evento.prazoAlteracoes)}</span>
      {restante && (
        <span
          className={`caixa-prazo-restante ${prazoProximo(evento.prazoAlteracoes) ? 'caixa-prazo-urgente' : 'caixa-prazo-proximo'}`}
        >
          {restante}
        </span>
      )}
      {situacao && <span className="caixa-prazo-restante">{situacao}</span>}
    </div>
  );
}
