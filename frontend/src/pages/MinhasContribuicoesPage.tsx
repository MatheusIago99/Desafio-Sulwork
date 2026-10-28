import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { MinhaContribuicao } from '../api/types';
import { Alerta } from '../components/Alerta';
import { PrazoRestante } from '../components/PrazoRestante';
import { StatusBadge } from '../components/StatusBadge';
import { useParticipanteAtual } from '../ParticipanteAtual';
import { formatarDataHora, formatarQuantidade } from '../utils/format';

interface GrupoEvento {
  eventoId: number;
  contribuicoes: MinhaContribuicao[];
}

/** Agrupa por evento, mantendo a ordem da API (eventos mais próximos primeiro). */
function agruparPorEvento(lista: MinhaContribuicao[]): GrupoEvento[] {
  const grupos = new Map<number, GrupoEvento>();
  for (const c of lista) {
    if (!grupos.has(c.eventoId)) grupos.set(c.eventoId, { eventoId: c.eventoId, contribuicoes: [] });
    grupos.get(c.eventoId)!.contribuicoes.push(c);
  }
  return [...grupos.values()];
}

export function MinhasContribuicoesPage() {
  const { participanteId } = useParticipanteAtual();
  const [lista, setLista] = useState<MinhaContribuicao[] | null>(null);
  const [mensagem, setMensagem] = useState<{ tipo: 'erro' | 'sucesso'; texto: string } | null>(null);

  const carregar = useCallback(async () => {
    if (participanteId === null) return;
    try {
      setLista(await api.participantes.contribuicoes(participanteId));
    } catch (ex) {
      setMensagem({ tipo: 'erro', texto: ex instanceof Error ? ex.message : 'Erro ao carregar as contribuições' });
    }
  }, [participanteId]);

  useEffect(() => {
    setLista(null);
    void carregar();
  }, [carregar]);

  const executar = async (acao: () => Promise<unknown>, sucesso: string) => {
    try {
      await acao();
      await carregar();
      setMensagem({ tipo: 'sucesso', texto: sucesso });
    } catch (ex) {
      setMensagem({ tipo: 'erro', texto: ex instanceof Error ? ex.message : 'Erro inesperado' });
    }
  };

  const grupos = lista ? agruparPorEvento(lista) : [];

  return (
    <div className="pagina">
      <div className="pagina-cabecalho">
        <div>
          <h1>Minhas contribuições</h1>
          <p className="pagina-subtitulo">O que você vai levar em cada evento.</p>
        </div>
      </div>

      {mensagem && (
        <Alerta tipo={mensagem.tipo} onFechar={() => setMensagem(null)}>
          {mensagem.texto}
        </Alerta>
      )}

      {participanteId === null ? (
        <p className="vazio">Selecione seu nome no campo "Você é" para ver o que você vai levar.</p>
      ) : lista === null ? (
        <p className="texto-suave">Carregando...</p>
      ) : lista.length === 0 ? (
        <p className="vazio">
          Você ainda não assumiu nenhum item. Veja o que falta nos <Link to="/eventos">eventos</Link>.
        </p>
      ) : (
        <>
          <p className="texto-suave">
            {lista.length} {lista.length === 1 ? 'item' : 'itens'} em {grupos.length}{' '}
            {grupos.length === 1 ? 'evento' : 'eventos'}.
          </p>
          {grupos.map((grupo) => (
            <GrupoContribuicoes key={grupo.eventoId} grupo={grupo} executar={executar} />
          ))}
        </>
      )}
    </div>
  );
}

function GrupoContribuicoes({
  grupo,
  executar,
}: {
  grupo: GrupoEvento;
  executar: (acao: () => Promise<unknown>, sucesso: string) => Promise<void>;
}) {
  const evento = grupo.contribuicoes[0];
  return (
    <section className="cartao grupo-contribuicoes" aria-label={evento.eventoNome}>
      <div className="grupo-cabecalho">
        <div>
          <div className="linha-inline">
            <StatusBadge status={evento.eventoStatus} prazoVencido={evento.prazoVencido} />
            {evento.aceitaAlteracoes && <PrazoRestante prazo={evento.prazoAlteracoes} />}
          </div>
          <h2>
            <Link to={`/eventos/${evento.eventoId}`} className="link-evento">
              {evento.eventoNome}
            </Link>
          </h2>
          <p className="texto-suave texto-pequeno">
            {formatarDataHora(evento.eventoDataHora)} · alterações até {formatarDataHora(evento.prazoAlteracoes)}
          </p>
        </div>
      </div>
      {!evento.aceitaAlteracoes && (
        <p className="grupo-bloqueado texto-pequeno">
          Alterações encerradas para este evento. As contribuições ficam disponíveis só para consulta.
        </p>
      )}
      <div className="tabela-rolagem">
        <table className="tabela">
          <thead>
            <tr>
              <th>Item</th>
              <th>Eu levo</th>
              <th>Total do item</th>
              {evento.aceitaAlteracoes && <th aria-label="Ações" />}
            </tr>
          </thead>
          <tbody>
            {grupo.contribuicoes.map((c) => (
              <LinhaContribuicao key={c.contribuicaoId} contribuicao={c} executar={executar} />
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function LinhaContribuicao({
  contribuicao: c,
  executar,
}: {
  contribuicao: MinhaContribuicao;
  executar: (acao: () => Promise<unknown>, sucesso: string) => Promise<void>;
}) {
  const [editando, setEditando] = useState(false);
  const [quantidade, setQuantidade] = useState(String(c.quantidade));
  // O limite é o que ainda falta no item mais o que esta pessoa já leva.
  const maximo = c.quantidadeNecessaria - c.quantidadeContribuidaTotal + c.quantidade;

  const salvar = async () => {
    await executar(
      () => api.contribuicoes.alterar(c.eventoId, c.contribuicaoId, Number(quantidade)),
      `Quantidade de ${c.itemNome} atualizada.`,
    );
    setEditando(false);
  };

  return (
    <tr>
      <td>
        <span className="item-nome">{c.itemNome}</span>{' '}
        <span className={`badge ${c.obrigatorio ? 'badge-prioridade-alta' : 'badge-neutro'}`}>
          {c.obrigatorio ? 'Obrigatório' : 'Opcional'}
        </span>
      </td>
      <td>
        {editando ? (
          <input
            type="number"
            min={1}
            max={maximo}
            aria-label={`Nova quantidade de ${c.itemNome}`}
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
        ) : (
          <strong>{formatarQuantidade(c.quantidade, c.unidade)}</strong>
        )}
      </td>
      <td className="texto-suave">
        {c.quantidadeContribuidaTotal} de {formatarQuantidade(c.quantidadeNecessaria, c.unidade)}
      </td>
      {c.aceitaAlteracoes && (
        <td className="acoes-tabela">
          {editando ? (
            <>
              <button type="button" className="botao botao-pequeno botao-primario" onClick={salvar}>
                Salvar
              </button>
              <button type="button" className="botao botao-pequeno" onClick={() => setEditando(false)}>
                Cancelar
              </button>
            </>
          ) : (
            <>
              <button type="button" className="botao botao-pequeno" onClick={() => setEditando(true)}>
                Alterar
              </button>
              <button
                type="button"
                className="botao botao-pequeno botao-perigo"
                onClick={() =>
                  confirm(`Remover sua contribuição de ${c.itemNome}?`) &&
                  executar(() => api.contribuicoes.remover(c.eventoId, c.contribuicaoId), 'Contribuição removida.')
                }
              >
                Remover
              </button>
            </>
          )}
        </td>
      )}
    </tr>
  );
}
