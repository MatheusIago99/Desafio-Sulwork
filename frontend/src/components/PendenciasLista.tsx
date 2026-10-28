import { useState } from 'react';
import type { ItemPendente, Pendencias } from '../api/types';
import { ALERGENO_LABEL, PRIORIDADE_LABEL, formatarQuantidade } from '../utils/format';
import { AlergenoChips } from './Alergenos';
import { BarraProgresso } from './BarraProgresso';

interface Props {
  pendencias: Pendencias;
  participanteSelecionado: boolean;
  participanteInscrito: boolean;
  onAssumir: (item: ItemPendente, quantidade?: number) => Promise<void>;
}

/**
 * Feature livre "O que falta?": mostra só o que ainda precisa de alguém, já priorizado,
 * e permite assumir o item com um clique ("Eu levo").
 */
export function PendenciasLista({ pendencias, participanteSelecionado, participanteInscrito, onAssumir }: Props) {
  if (pendencias.totalPendentes === 0) {
    return <p className="vazio">Todos os itens já têm responsável.</p>;
  }

  let motivoBloqueio: string | null = null;
  if (!pendencias.aceitaContribuicoes) motivoBloqueio = 'O evento não está aceitando contribuições.';
  else if (!participanteSelecionado) motivoBloqueio = 'Selecione seu nome no campo "Você é" para assumir itens.';
  else if (!participanteInscrito) motivoBloqueio = 'Inscreva-se no evento (aba Participantes) para assumir itens.';

  return (
    <div>
      <p className="texto-suave">
        {pendencias.totalPendentes} {pendencias.totalPendentes === 1 ? 'item' : 'itens'} sem quem leve
        {pendencias.obrigatoriosPendentes > 0 && (
          <>
            , sendo <strong>{pendencias.obrigatoriosPendentes} obrigatório(s)</strong>
          </>
        )}
        .
      </p>
      {motivoBloqueio && <p className="texto-aviso">{motivoBloqueio}</p>}
      <ul className="lista-pendencias">
        {pendencias.itens.map((item) => (
          <PendenciaItem key={item.itemId} item={item} bloqueado={motivoBloqueio !== null} onAssumir={onAssumir} />
        ))}
      </ul>
    </div>
  );
}

function PendenciaItem({
  item,
  bloqueado,
  onAssumir,
}: {
  item: ItemPendente;
  bloqueado: boolean;
  onAssumir: Props['onAssumir'];
}) {
  const [quantidade, setQuantidade] = useState('');
  const [enviando, setEnviando] = useState(false);

  const assumir = async (qtd?: number) => {
    setEnviando(true);
    try {
      await onAssumir(item, qtd);
      setQuantidade('');
    } finally {
      setEnviando(false);
    }
  };

  const quantidadeNumero = Number(quantidade);
  const quantidadeValida = Number.isInteger(quantidadeNumero) && quantidadeNumero >= 1 && quantidadeNumero <= item.quantidadeFaltante;

  return (
    <li className={`pendencia pendencia-${item.prioridade.toLowerCase()}`} data-testid="pendencia">
      <div className="pendencia-cabecalho">
        <div>
          <span className={`badge badge-prioridade-${item.prioridade.toLowerCase()}`}>
            {PRIORIDADE_LABEL[item.prioridade]}
          </span>{' '}
          <strong>{item.nome}</strong> <span className="texto-suave">· {item.categoria}</span>
        </div>
        <span>
          faltam <strong>{formatarQuantidade(item.quantidadeFaltante, item.unidade)}</strong> de{' '}
          {item.quantidadeNecessaria}
        </span>
      </div>
      <BarraProgresso percentual={item.percentual} rotulo={`Progresso de ${item.nome}`} pequena />
      <div className="pendencia-detalhes">
        <AlergenoChips alergenos={item.alergenos} />
        {item.conflitos.map((c) => (
          <span key={c.alergeno} className="texto-aviso">
            {ALERGENO_LABEL[c.alergeno]}: restrição de {c.participantes.join(', ')}
          </span>
        ))}
        {item.minhaContribuicao !== null && (
          <span className="texto-sucesso">Você já leva {formatarQuantidade(item.minhaContribuicao, item.unidade)}</span>
        )}
      </div>
      <div className="pendencia-acoes">
        <button type="button" className="botao botao-primario" disabled={bloqueado || enviando} onClick={() => assumir()}>
          Eu levo {item.quantidadeFaltante > 1 ? `os ${item.quantidadeFaltante}` : ''}
        </button>
        {item.quantidadeFaltante > 1 && (
          <span className="grupo-inline">
            <input
              type="number"
              min={1}
              max={item.quantidadeFaltante}
              placeholder="Qtd."
              aria-label={`Quantidade de ${item.nome}`}
              value={quantidade}
              onChange={(e) => setQuantidade(e.target.value)}
              disabled={bloqueado || enviando}
            />
            <button
              type="button"
              className="botao"
              disabled={bloqueado || enviando || !quantidadeValida}
              onClick={() => assumir(quantidadeNumero)}
            >
              Levar só essa quantidade
            </button>
          </span>
        )}
      </div>
    </li>
  );
}
