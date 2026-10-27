import { useState, type FormEvent } from 'react';
import { api } from '../../api/client';
import { AlergenoChips } from '../../components/Alergenos';
import type { DadosEvento, Executar } from './contexto';

export function AbaParticipantes({ dados, executar }: { dados: DadosEvento; executar: Executar }) {
  const { evento, inscricoes, participantes } = dados;
  const aberto = evento.aceitaAlteracoes;
  const inscritos = new Set(inscricoes.map((i) => i.participante.id));
  const disponiveis = participantes.filter((p) => !inscritos.has(p.id));
  const [participanteId, setParticipanteId] = useState('');

  const inscrever = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await executar(() => api.inscricoes.inscrever(evento.id, Number(participanteId)), 'Inscrição realizada.');
    if (ok) setParticipanteId('');
  };

  return (
    <div>
      {aberto && (
        <form className="linha-form" onSubmit={inscrever}>
          <select aria-label="Participante a inscrever" required value={participanteId} onChange={(e) => setParticipanteId(e.target.value)}>
            <option value="">Inscrever participante...</option>
            {disponiveis.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
          <button type="submit" className="botao botao-primario">
            Inscrever
          </button>
          <span className="texto-suave">Não encontrou? Cadastre na página Participantes.</span>
        </form>
      )}

      {inscricoes.length === 0 ? (
        <p className="vazio">Nenhum participante inscrito.</p>
      ) : (
        <div className="tabela-container">
          <table className="tabela">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Restrições alimentares</th>
                {aberto && <th aria-label="Ações" />}
              </tr>
            </thead>
            <tbody>
              {inscricoes.map(({ participante }) => (
                <tr key={participante.id}>
                  <td>{participante.nome}</td>
                  <td>
                    <AlergenoChips
                      alergenos={participante.restricoes}
                      vazio={participante.observacoesAlimentares ? '' : 'Nenhuma'}
                    />
                    {participante.observacoesAlimentares && (
                      <div className="texto-suave">{participante.observacoesAlimentares}</div>
                    )}
                  </td>
                  {aberto && (
                    <td className="acoes-tabela">
                      <button
                        type="button"
                        className="botao botao-pequeno botao-perigo"
                        onClick={() =>
                          confirm(`Cancelar a inscrição de ${participante.nome}? As contribuições dele(a) também serão removidas.`) &&
                          executar(() => api.inscricoes.cancelar(evento.id, participante.id), 'Inscrição cancelada.')
                        }
                      >
                        Cancelar inscrição
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
