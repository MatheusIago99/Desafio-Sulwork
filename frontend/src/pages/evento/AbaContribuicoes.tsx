import { useState, type FormEvent } from 'react';
import { api } from '../../api/client';
import type { Contribuicao } from '../../api/types';
import { formatarQuantidade } from '../../utils/format';
import type { DadosEvento, Executar } from './contexto';

export function AbaContribuicoes({
  dados,
  executar,
  participanteAtualId,
}: {
  dados: DadosEvento;
  executar: Executar;
  participanteAtualId: number | null;
}) {
  const { evento, contribuicoes, inscricoes, itens } = dados;
  const aberto = evento.aceitaAlteracoes;
  const inscritoAtual = inscricoes.some((i) => i.participante.id === participanteAtualId);

  const [participanteId, setParticipanteId] = useState<string>(inscritoAtual ? String(participanteAtualId) : '');
  const [itemId, setItemId] = useState('');
  const [quantidade, setQuantidade] = useState('1');

  const itensDisponiveis = itens.filter((i) => i.quantidadeFaltante > 0);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await executar(
      () =>
        api.contribuicoes.criar(evento.id, {
          participanteId: Number(participanteId),
          itemId: Number(itemId),
          quantidade: Number(quantidade),
        }),
      'Contribuição registrada.',
    );
    if (ok) {
      setItemId('');
      setQuantidade('1');
    }
  };

  return (
    <div>
      {aberto && inscricoes.length > 0 && (
        <form className="linha-form" onSubmit={enviar} aria-label="Nova contribuição">
          <select aria-label="Participante" required value={participanteId} onChange={(e) => setParticipanteId(e.target.value)}>
            <option value="">Quem leva?</option>
            {inscricoes.map((i) => (
              <option key={i.participante.id} value={i.participante.id}>
                {i.participante.nome}
              </option>
            ))}
          </select>
          <select aria-label="Item" required value={itemId} onChange={(e) => setItemId(e.target.value)}>
            <option value="">O quê?</option>
            {itensDisponiveis.map((i) => (
              <option key={i.id} value={i.id}>
                {i.nome} (faltam {formatarQuantidade(i.quantidadeFaltante, i.unidade)})
              </option>
            ))}
          </select>
          <input
            aria-label="Quantidade"
            type="number"
            min={1}
            required
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
          <button type="submit" className="botao botao-primario">
            Registrar
          </button>
        </form>
      )}
      {aberto && inscricoes.length === 0 && (
        <p className="texto-aviso">Ninguém se inscreveu ainda. Inscreva participantes para registrar contribuições.</p>
      )}

      {contribuicoes.length === 0 ? (
        <p className="vazio">Nenhuma contribuição ainda.</p>
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Quem</th>
                <th>Leva</th>
                <th>Quantidade</th>
                {aberto && <th aria-label="Ações" />}
              </tr>
            </thead>
            <tbody>
              {contribuicoes.map((c) => (
                <LinhaContribuicao key={c.id} eventoId={evento.id} contribuicao={c} editavel={aberto} executar={executar} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function LinhaContribuicao({
  eventoId,
  contribuicao,
  editavel,
  executar,
}: {
  eventoId: number;
  contribuicao: Contribuicao;
  editavel: boolean;
  executar: Executar;
}) {
  const [editando, setEditando] = useState(false);
  const [quantidade, setQuantidade] = useState(String(contribuicao.quantidade));

  const salvar = async () => {
    const ok = await executar(
      () => api.contribuicoes.alterar(eventoId, contribuicao.id, Number(quantidade)),
      'Quantidade atualizada.',
    );
    if (ok) setEditando(false);
  };

  return (
    <tr>
      <td>{contribuicao.participanteNome}</td>
      <td>{contribuicao.itemNome}</td>
      <td>
        {editando ? (
          <input
            aria-label={`Nova quantidade de ${contribuicao.itemNome}`}
            type="number"
            min={1}
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
        ) : (
          formatarQuantidade(contribuicao.quantidade, contribuicao.unidade)
        )}
      </td>
      {editavel && (
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
                onClick={() => executar(() => api.contribuicoes.remover(eventoId, contribuicao.id), 'Contribuição removida.')}
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
