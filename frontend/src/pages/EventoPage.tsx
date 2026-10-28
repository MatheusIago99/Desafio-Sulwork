import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/client';
import type { EventoInput, ItemPendente, StatusEvento } from '../api/types';
import { Alerta } from '../components/Alerta';
import { CoberturaResumo } from '../components/CoberturaResumo';
import { EventoForm } from '../components/EventoForm';
import { Modal } from '../components/Modal';
import { PendenciasLista } from '../components/PendenciasLista';
import { PrazoRestante } from '../components/PrazoRestante';
import { StatusBadge } from '../components/StatusBadge';
import { useParticipanteAtual } from '../ParticipanteAtual';
import { ACAO_STATUS, TIPO_LABEL, formatarDataHora } from '../utils/format';
import { AbaContribuicoes } from './evento/AbaContribuicoes';
import { AbaItens } from './evento/AbaItens';
import { AbaParticipantes } from './evento/AbaParticipantes';
import { AbaRestricoes } from './evento/AbaRestricoes';
import type { DadosEvento, Executar } from './evento/contexto';

type Aba = 'falta' | 'itens' | 'contribuicoes' | 'participantes' | 'restricoes';

const ABAS: { id: Aba; rotulo: string }[] = [
  { id: 'falta', rotulo: 'O que falta?' },
  { id: 'itens', rotulo: 'Itens e categorias' },
  { id: 'contribuicoes', rotulo: 'Contribuições' },
  { id: 'participantes', rotulo: 'Participantes' },
  { id: 'restricoes', rotulo: 'Restrições e alérgenos' },
];

export function EventoPage() {
  const id = Number(useParams().id);
  const navigate = useNavigate();
  const { participanteId } = useParticipanteAtual();
  const [dados, setDados] = useState<DadosEvento | null>(null);
  const [aba, setAba] = useState<Aba | null>(null);
  const [editando, setEditando] = useState(false);
  const [mensagem, setMensagem] = useState<{ tipo: 'erro' | 'sucesso'; texto: string } | null>(null);

  const carregar = useCallback(async () => {
    try {
      const [evento, cobertura, pendencias, categorias, itens, inscricoes, contribuicoes, restricoes, participantes] =
        await Promise.all([
          api.eventos.buscar(id),
          api.cobertura(id),
          api.pendencias.listar(id, participanteId ?? undefined),
          api.categorias.listar(id),
          api.itens.listar(id),
          api.inscricoes.listar(id),
          api.contribuicoes.listar(id),
          api.restricoes(id),
          api.participantes.listar(),
        ]);
      setDados({ evento, cobertura, pendencias, categorias, itens, inscricoes, contribuicoes, restricoes, participantes });
    } catch (ex) {
      setMensagem({ tipo: 'erro', texto: ex instanceof Error ? ex.message : 'Erro ao carregar o evento' });
    }
  }, [id, participanteId]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const executar: Executar = async (acao, sucesso) => {
    try {
      await acao();
      setMensagem(sucesso ? { tipo: 'sucesso', texto: sucesso } : null);
      await carregar();
      return true;
    } catch (ex) {
      setMensagem({ tipo: 'erro', texto: ex instanceof Error ? ex.message : 'Erro inesperado' });
      return false;
    }
  };

  if (!dados) {
    return (
      <div className="pagina">
        {mensagem ? <Alerta tipo="erro">{mensagem.texto}</Alerta> : <p className="texto-suave">Carregando...</p>}
      </div>
    );
  }

  const { evento } = dados;
  // Sem escolha explícita, rascunhos abrem na organização de itens e os demais em "O que falta?".
  const abaAtual: Aba = aba ?? (evento.status === 'RASCUNHO' ? 'itens' : 'falta');
  const participanteInscrito = dados.inscricoes.some((i) => i.participante.id === participanteId);

  const alterarStatus = (status: StatusEvento) => {
    if (status === 'CANCELADO' && !confirm('Cancelar o evento? Essa ação não pode ser desfeita.')) return;
    void executar(() => api.eventos.alterarStatus(evento.id, status), 'Status atualizado.');
  };

  const salvarEdicao = async (input: EventoInput) => {
    await api.eventos.atualizar(evento.id, input);
    setEditando(false);
    setMensagem({ tipo: 'sucesso', texto: 'Evento atualizado.' });
    await carregar();
  };

  const excluir = async () => {
    if (!confirm('Excluir este rascunho?')) return;
    if (await executar(() => api.eventos.excluir(evento.id))) navigate('/eventos');
  };

  const assumir = async (item: ItemPendente, quantidade?: number) => {
    if (participanteId === null) return;
    await executar(
      () => api.pendencias.assumir(evento.id, item.itemId, participanteId, quantidade),
      `Contribuição registrada: ${quantidade ?? item.quantidadeFaltante} × ${item.nome}.`,
    );
  };

  return (
    <div className="pagina">
      <Link to="/eventos" className="voltar">
        ← Voltar para a lista
      </Link>

      <header className="evento-cabecalho">
        <div>
          <div className="linha-inline">
            <StatusBadge status={evento.status} prazoVencido={evento.prazoVencido} />
            <span className="evento-tipo">{TIPO_LABEL[evento.tipo]}</span>
          </div>
          <h1>{evento.nome}</h1>
          {evento.descricao && <p>{evento.descricao}</p>}
          <p>
            <strong>{formatarDataHora(evento.dataHora)}</strong>
            <span className="texto-suave"> · alterações até {formatarDataHora(evento.prazoAlteracoes)}</span>{' '}
            {evento.aceitaAlteracoes && <PrazoRestante prazo={evento.prazoAlteracoes} />}
          </p>
        </div>
        <div className="evento-acoes">
          {evento.transicoesPermitidas.map((status) => (
            <button
              key={status}
              type="button"
              className={`botao ${status === 'CANCELADO' ? 'botao-perigo' : 'botao-primario'}`}
              onClick={() => alterarStatus(status)}
            >
              {evento.status === 'ENCERRADO' && status === 'ABERTO' ? 'Reabrir' : ACAO_STATUS[status]}
            </button>
          ))}
          {(evento.status === 'RASCUNHO' || evento.status === 'ABERTO') && !editando && (
            <button type="button" className="botao" onClick={() => setEditando(true)}>
              Editar
            </button>
          )}
          {evento.status === 'RASCUNHO' && (
            <button type="button" className="botao botao-perigo" onClick={excluir}>
              Excluir
            </button>
          )}
        </div>
      </header>

      {editando && (
        <Modal titulo="Editar evento" onFechar={() => setEditando(false)} largo>
          <EventoForm inicial={evento} onSalvar={salvarEdicao} onCancelar={() => setEditando(false)} />
        </Modal>
      )}

      <AvisoJanela dados={dados} />
      {mensagem && (
        <Alerta tipo={mensagem.tipo} onFechar={() => setMensagem(null)}>
          {mensagem.texto}
        </Alerta>
      )}

      <CoberturaResumo cobertura={dados.cobertura} />

      <nav className="abas" role="tablist">
        {ABAS.map((a) => (
          <button
            key={a.id}
            type="button"
            role="tab"
            aria-selected={abaAtual === a.id}
            className={`aba ${abaAtual === a.id ? 'aba-ativa' : ''}`}
            onClick={() => setAba(a.id)}
          >
            {a.rotulo}
            {a.id === 'falta' && dados.pendencias.totalPendentes > 0 && (
              <span className="contador">{dados.pendencias.totalPendentes}</span>
            )}
          </button>
        ))}
      </nav>

      <section className="aba-conteudo" role="tabpanel">
        {abaAtual === 'falta' && (
          <PendenciasLista
            pendencias={dados.pendencias}
            participanteSelecionado={participanteId !== null}
            participanteInscrito={participanteInscrito}
            onAssumir={assumir}
          />
        )}
        {abaAtual === 'itens' && <AbaItens dados={dados} executar={executar} />}
        {abaAtual === 'contribuicoes' && (
          <AbaContribuicoes dados={dados} executar={executar} participanteAtualId={participanteId} />
        )}
        {abaAtual === 'participantes' && <AbaParticipantes dados={dados} executar={executar} />}
        {abaAtual === 'restricoes' && <AbaRestricoes resumo={dados.restricoes} />}
      </section>
    </div>
  );
}

/** Explica por que as alterações estão ou não liberadas, conforme a regra prazo x status. */
function AvisoJanela({ dados }: { dados: DadosEvento }) {
  const { evento } = dados;
  if (evento.status === 'RASCUNHO') {
    return (
      <Alerta tipo="info">
        Evento em rascunho: organize categorias e itens e depois clique em “Abrir para contribuições”.
      </Alerta>
    );
  }
  if (evento.status === 'ABERTO' && evento.prazoVencido) {
    return (
      <Alerta tipo="aviso">
        O prazo para alterações venceu. Inscrições e contribuições estão bloqueadas; o organizador pode estender o
        prazo editando o evento ou encerrá-lo.
      </Alerta>
    );
  }
  if (evento.status === 'ENCERRADO') {
    return <Alerta tipo="aviso">Contribuições encerradas: a lista de quem leva o quê está fechada.</Alerta>;
  }
  if (evento.status === 'REALIZADO' || evento.status === 'CANCELADO') {
    return <Alerta tipo="info">Evento {evento.status === 'REALIZADO' ? 'realizado' : 'cancelado'}; nada mais pode ser alterado.</Alerta>;
  }
  return null;
}
