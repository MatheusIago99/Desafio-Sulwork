import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import type { Evento, EventoInput, StatusEvento } from '../api/types';
import { Alerta } from '../components/Alerta';
import { EventoForm } from '../components/EventoForm';
import { StatusBadge } from '../components/StatusBadge';
import { STATUS_LABEL, TIPO_ICONE, TIPO_LABEL, formatarDataHora, partesData } from '../utils/format';

export function EventosPage() {
  const navigate = useNavigate();
  const [eventos, setEventos] = useState<Evento[] | null>(null);
  const [filtro, setFiltro] = useState<StatusEvento | ''>('');
  const [criando, setCriando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  const carregar = useCallback(async () => {
    try {
      setEventos(await api.eventos.listar(filtro || undefined));
      setErro(null);
    } catch (ex) {
      setErro(ex instanceof Error ? ex.message : 'Erro ao carregar eventos');
    }
  }, [filtro]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const criar = async (dados: EventoInput) => {
    const evento = await api.eventos.criar(dados);
    navigate(`/eventos/${evento.id}`);
  };

  return (
    <div className="pagina">
      <div className="pagina-cabecalho">
        <div>
          <h1>Eventos</h1>
          <p className="pagina-subtitulo">Combine quem leva o quê e acompanhe o que ainda falta.</p>
        </div>
        {!criando && (
          <button type="button" className="botao botao-primario" onClick={() => setCriando(true)}>
            Novo evento
          </button>
        )}
      </div>

      {criando && (
        <section className="cartao">
          <h2>Novo evento</h2>
          <p className="texto-suave">
            O evento começa como rascunho. Cadastre categorias e itens e depois abra para os participantes.
          </p>
          <EventoForm onSalvar={criar} onCancelar={() => setCriando(false)} />
        </section>
      )}

      <label className="filtro">
        Status{' '}
        <select value={filtro} onChange={(e) => setFiltro(e.target.value as StatusEvento | '')}>
          <option value="">Todos</option>
          {Object.entries(STATUS_LABEL).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>
              {rotulo}
            </option>
          ))}
        </select>
      </label>

      {erro && <Alerta tipo="erro">{erro}</Alerta>}
      {eventos === null && !erro && <p className="texto-suave">Carregando...</p>}
      {eventos?.length === 0 && <p className="vazio">Nenhum evento encontrado.</p>}

      <ul className="grade-eventos">
        {eventos?.map((evento) => (
          <li key={evento.id}>
            <Link to={`/eventos/${evento.id}`} className="cartao cartao-evento">
              <div className="cartao-evento-topo">
                <span className="evento-data" aria-hidden="true">
                  <strong>{partesData(evento.dataHora).dia}</strong>
                  <span>{partesData(evento.dataHora).mes}</span>
                </span>
                <StatusBadge status={evento.status} />
              </div>
              <span className="evento-tipo">
                <span aria-hidden="true">{TIPO_ICONE[evento.tipo]}</span> {TIPO_LABEL[evento.tipo]}
              </span>
              <h2>{evento.nome}</h2>
              <p className="texto-suave">{formatarDataHora(evento.dataHora)}</p>
              <p className="cartao-evento-rodape">
                {evento.aceitaAlteracoes
                  ? `Contribuições até ${formatarDataHora(evento.prazoAlteracoes)}`
                  : evento.status === 'ABERTO' && evento.prazoVencido
                    ? 'Prazo para alterações encerrado'
                    : evento.status === 'RASCUNHO'
                      ? 'Em organização'
                      : ''}
              </p>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
