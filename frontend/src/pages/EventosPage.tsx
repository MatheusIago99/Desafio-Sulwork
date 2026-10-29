import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../api/client';
import type { Evento, EventoInput, StatusEvento, TipoEvento } from '../api/types';
import { Alerta } from '../components/Alerta';
import { EventoForm } from '../components/EventoForm';
import { CaixaPrazo } from '../components/CaixaPrazo';
import { CapaEvento } from '../components/CapaEvento';
import { IconeBusca, IconeCalendario, IconeMais, IconeSeta } from '../components/Icones';
import { Modal } from '../components/Modal';
import { TIPO_LABEL, formatarDataHora } from '../utils/format';

const ABAS_STATUS: { valor: StatusEvento | ''; rotulo: string }[] = [
  { valor: '', rotulo: 'Todos' },
  { valor: 'RASCUNHO', rotulo: 'Rascunho' },
  { valor: 'ABERTO', rotulo: 'Abertos' },
  { valor: 'ENCERRADO', rotulo: 'Encerrados' },
  { valor: 'REALIZADO', rotulo: 'Realizados' },
  { valor: 'CANCELADO', rotulo: 'Cancelados' },
];

/** Busca sem diferenciar maiúsculas nem acentos. */
function normalizar(texto: string): string {
  return texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}

export function EventosPage() {
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const [eventos, setEventos] = useState<Evento[] | null>(null);
  const [busca, setBusca] = useState('');
  const [tipo, setTipo] = useState<TipoEvento | ''>('');
  const [status, setStatus] = useState<StatusEvento | ''>('');
  const [erro, setErro] = useState<string | null>(null);
  const criando = params.get('novo') === '1';

  useEffect(() => {
    api.eventos
      .listar()
      .then(setEventos)
      .catch((ex: unknown) => setErro(ex instanceof Error ? ex.message : 'Erro ao carregar eventos'));
  }, []);

  const filtrados = useMemo(() => {
    const termo = normalizar(busca.trim());
    return (eventos ?? []).filter(
      (e) =>
        (!tipo || e.tipo === tipo) &&
        (!status || e.status === status) &&
        (!termo || normalizar(`${e.nome} ${e.descricao ?? ''}`).includes(termo)),
    );
  }, [eventos, busca, tipo, status]);

  const fecharModal = () => setParams({}, { replace: true });

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
        <button type="button" className="botao botao-primario" onClick={() => setParams({ novo: '1' })}>
          <IconeMais /> Novo evento
        </button>
      </div>

      {criando && (
        <Modal titulo="Novo evento" onFechar={fecharModal} largo>
          <p className="texto-suave modal-intro">
            O evento começa como rascunho. Cadastre categorias e itens e depois abra para os participantes.
          </p>
          <EventoForm onSalvar={criar} onCancelar={fecharModal} />
        </Modal>
      )}

      <div className="abas-status" role="tablist" aria-label="Filtrar por status">
        {ABAS_STATUS.map((aba) => {
          const quantidade = aba.valor ? (eventos ?? []).filter((e) => e.status === aba.valor).length : (eventos ?? []).length;
          return (
            <button
              key={aba.rotulo}
              type="button"
              role="tab"
              aria-selected={status === aba.valor}
              className={`aba-status ${status === aba.valor ? 'aba-status-ativa' : ''}`}
              onClick={() => setStatus(aba.valor)}
            >
              {aba.rotulo} <span className="aba-status-contagem">{quantidade}</span>
            </button>
          );
        })}
      </div>

      <div className="barra-filtros">
        <label className="campo-busca">
          <IconeBusca />
          <input
            type="search"
            placeholder="Buscar por nome ou descrição"
            aria-label="Buscar eventos"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </label>
        <select aria-label="Filtrar por tipo" value={tipo} onChange={(e) => setTipo(e.target.value as TipoEvento | '')}>
          <option value="">Todos os tipos</option>
          {Object.entries(TIPO_LABEL).map(([valor, rotulo]) => (
            <option key={valor} value={valor}>
              {rotulo}
            </option>
          ))}
        </select>
      </div>

      {erro && <Alerta tipo="erro">{erro}</Alerta>}
      {eventos === null && !erro && <p className="texto-suave">Carregando...</p>}
      {eventos !== null && filtrados.length === 0 && (
        <p className="vazio">
          {eventos.length === 0 ? 'Nenhum evento cadastrado ainda.' : 'Nenhum evento corresponde aos filtros.'}
        </p>
      )}

      {filtrados.length > 0 && (
        <ul className="grade-eventos">
          {filtrados.map((evento) => (
            <li key={evento.id} className="cartao cartao-evento">
              <CapaEvento tipo={evento.tipo} status={evento.status} prazoVencido={evento.prazoVencido}>
                <span>
                  <IconeCalendario /> {formatarDataHora(evento.dataHora)}
                </span>
              </CapaEvento>
              <div className="cartao-evento-conteudo">
                <div>
                  <h3>
                    <Link to={`/eventos/${evento.id}`}>{evento.nome}</Link>
                  </h3>
                  {evento.descricao && <p className="cartao-evento-descricao">{evento.descricao}</p>}
                </div>
                <CaixaPrazo evento={evento} />
                <span className="cartao-evento-acao" aria-hidden="true">
                  Ver evento <IconeSeta />
                </span>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
