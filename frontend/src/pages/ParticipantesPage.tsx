import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Participante, ParticipanteInput } from '../api/types';
import { Alerta } from '../components/Alerta';
import { AlergenoChips } from '../components/Alergenos';
import { ParticipanteForm } from '../components/ParticipanteForm';
import { formatarCpf } from '../utils/format';

export function ParticipantesPage() {
  const [participantes, setParticipantes] = useState<Participante[] | null>(null);
  const [busca, setBusca] = useState('');
  const [editando, setEditando] = useState<Participante | 'novo' | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    try {
      setParticipantes(await api.participantes.listar(busca.trim() || undefined));
      setErro(null);
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro ao carregar participantes');
    }
  }, [busca]);

  useEffect(() => {
    const timer = setTimeout(() => void carregar(), 250);
    return () => clearTimeout(timer);
  }, [carregar]);

  const salvar = async (dados: ParticipanteInput) => {
    if (editando === 'novo') await api.participantes.criar(dados);
    else if (editando) await api.participantes.atualizar(editando.id, dados);
    setEditando(null);
    await carregar();
  };

  const excluir = async (p: Participante) => {
    if (!confirm(`Excluir ${p.nome}?`)) return;
    try {
      await api.participantes.excluir(p.id);
      await carregar();
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro ao excluir');
    }
  };

  return (
    <div className="pagina">
      <div className="pagina-cabecalho">
        <h1>Participantes</h1>
        {editando === null && (
          <button type="button" className="botao botao-primario" onClick={() => setEditando('novo')}>
            Novo participante
          </button>
        )}
      </div>

      {editando !== null && (
        <section className="cartao">
          <h2>{editando === 'novo' ? 'Novo participante' : `Editar ${editando.nome}`}</h2>
          <ParticipanteForm
            key={editando === 'novo' ? 'novo' : editando.id}
            inicial={editando === 'novo' ? undefined : editando}
            onSalvar={salvar}
            onCancelar={() => setEditando(null)}
          />
        </section>
      )}

      <input
        type="search"
        className="busca"
        placeholder="Buscar por nome ou CPF"
        aria-label="Buscar participantes"
        value={busca}
        onChange={(e) => setBusca(e.target.value)}
      />

      {erro && (
        <Alerta tipo="erro" onFechar={() => setErro(null)}>
          {erro}
        </Alerta>
      )}
      {participantes?.length === 0 && <p className="vazio">Nenhum participante encontrado.</p>}

      {participantes && participantes.length > 0 && (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>Restrições</th>
                <th aria-label="Ações" />
              </tr>
            </thead>
            <tbody>
              {participantes.map((p) => (
                <tr key={p.id}>
                  <td>{p.nome}</td>
                  <td className="nowrap">{formatarCpf(p.cpf)}</td>
                  <td>
                    <AlergenoChips alergenos={p.restricoes} vazio={p.observacoesAlimentares ? '' : 'Nenhuma'} />
                    {p.observacoesAlimentares && <div className="texto-suave">{p.observacoesAlimentares}</div>}
                  </td>
                  <td className="acoes-tabela">
                    <button type="button" className="botao botao-pequeno" onClick={() => setEditando(p)}>
                      Editar
                    </button>
                    <button type="button" className="botao botao-pequeno botao-perigo" onClick={() => excluir(p)}>
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
