import { useEffect, useState, type ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { Dashboard, Participante, ProximoEvento } from '../api/types';
import { Alerta } from '../components/Alerta';
import { BarraProgresso } from '../components/BarraProgresso';
import { CaixaPrazo } from '../components/CaixaPrazo';
import { CapaEvento } from '../components/CapaEvento';
import {
  IconeAlerta,
  IconeCaixa,
  IconeCalendario,
  IconeCheck,
  IconeGrafico,
  IconePessoas,
  IconeRelogio,
  IconeSeta,
} from '../components/Icones';
import { useParticipanteAtual } from '../ParticipanteAtual';
import { formatarDataHora, formatarQuantidade } from '../utils/format';
import { iniciais } from '../utils/iniciais';
import { tempoRelativo } from '../utils/tempoRelativo';

type Periodo = 'todos' | 'semana' | 'quinzena';

const PERIODOS: { valor: Periodo; rotulo: string; dias: number | null }[] = [
  { valor: 'todos', rotulo: 'Todos', dias: null },
  { valor: 'semana', rotulo: 'Esta semana', dias: 7 },
  { valor: 'quinzena', rotulo: 'Próximos 15 dias', dias: 15 },
];

const formatoHoje = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

export function DashboardPage() {
  const { participanteId } = useParticipanteAtual();
  const [dados, setDados] = useState<Dashboard | null>(null);
  const [participantes, setParticipantes] = useState<Participante[]>([]);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([api.dashboard(), api.participantes.listar()])
      .then(([dashboard, lista]) => {
        setDados(dashboard);
        setParticipantes(lista);
      })
      .catch((ex: unknown) => setErro(ex instanceof Error ? ex.message : 'Erro ao carregar a visão geral'));
  }, []);

  const nome = participantes.find((p) => p.id === participanteId)?.nome.split(' ')[0];

  return (
    <div className="pagina">
      <div className="pagina-cabecalho">
        <div>
          <h1>Visão geral</h1>
          <p className="pagina-subtitulo">
            {nome ? `Olá, ${nome}. ` : ''}Organize os eventos e acompanhe as pendências.
          </p>
        </div>
        <div className="cabecalho-acoes">
          <span className="data-hoje">
            <IconeCalendario /> {formatoHoje.format(new Date())}
          </span>
          <Link to="/eventos?novo=1" className="botao botao-primario">
            Novo evento
          </Link>
        </div>
      </div>

      {erro && <Alerta tipo="erro">{erro}</Alerta>}
      {!dados && !erro && <p className="texto-suave">Carregando...</p>}
      {dados && <Conteudo dados={dados} />}
    </div>
  );
}

function Conteudo({ dados }: { dados: Dashboard }) {
  const [periodo, setPeriodo] = useState<Periodo>('todos');
  const limite = PERIODOS.find((p) => p.valor === periodo)!.dias;
  const proximos = dados.proximosEventos.filter(
    ({ evento }) => limite === null || new Date(evento.dataHora).getTime() - Date.now() <= limite * 24 * 60 * 60 * 1000,
  );

  return (
    <>
      <section className="grade-indicadores" aria-label="Indicadores">
        <Indicador rotulo="Próximos eventos" icone={<IconeCalendario />} valor={String(dados.proximosEventosTotal)}>
          <span className="selo-indicador">{dados.proximosEventosNaSemana} nesta semana</span>
        </Indicador>
        <Indicador rotulo="Contribuições" icone={<IconeCheck />} valor={String(dados.contribuicoesTotal)}>
          <span className="texto-suave">
            {dados.ultimaContribuicaoEm ? `Última ${tempoRelativo(dados.ultimaContribuicaoEm)}` : 'Nenhuma ainda'}
          </span>
        </Indicador>
        <Indicador
          rotulo="Itens pendentes"
          icone={<IconeAlerta />}
          valor={String(dados.obrigatoriosPendentes)}
          tom={dados.obrigatoriosPendentes > 0 ? 'aviso' : undefined}
        >
          <span className={dados.obrigatoriosPendentes > 0 ? 'selo-indicador selo-aviso' : 'texto-suave'}>
            {dados.obrigatoriosPendentes > 0 ? 'Obrigatórios sem dono' : 'Nada pendente'}
          </span>
        </Indicador>
        <Indicador
          rotulo="Cobertura geral"
          icone={<IconeGrafico />}
          valor={dados.coberturaMedia === null ? '—' : `${dados.coberturaMedia}%`}
          tom="destaque"
        >
          {dados.coberturaMedia === null ? (
            <span className="texto-suave">Nenhum evento aberto</span>
          ) : (
            <BarraProgresso percentual={dados.coberturaMedia} rotulo="Cobertura média dos eventos abertos" pequena />
          )}
        </Indicador>
      </section>

      <div className="grade-dashboard">
        <section aria-label="Próximos eventos">
          <div className="secao-cabecalho">
            <h2>
              Próximos eventos <span className="contagem-secao">{dados.proximosEventos.length}</span>
            </h2>
            <div className="segmentado" role="tablist" aria-label="Período">
              {PERIODOS.map((p) => (
                <button
                  key={p.valor}
                  type="button"
                  role="tab"
                  aria-selected={periodo === p.valor}
                  className={periodo === p.valor ? 'ativo' : ''}
                  onClick={() => setPeriodo(p.valor)}
                >
                  {p.rotulo}
                </button>
              ))}
            </div>
          </div>
          {proximos.length === 0 ? (
            <p className="vazio">
              {dados.proximosEventos.length === 0 ? (
                <>
                  Nenhum evento por vir. <Link to="/eventos?novo=1">Crie o primeiro.</Link>
                </>
              ) : (
                'Nenhum evento neste período.'
              )}
            </p>
          ) : (
            <ul className="lista-cartoes-evento">
              {proximos.map((p) => (
                <CartaoEvento key={p.evento.id} proximo={p} />
              ))}
            </ul>
          )}
        </section>

        <section className="cartao pendencias-prioritarias" aria-label="Pendências prioritárias">
          <div className="linha-entre">
            <h2>Pendências prioritárias</h2>
            {dados.obrigatoriosPendentes > 0 && (
              <span className="selo-indicador selo-aviso">{dados.obrigatoriosPendentes} obrigatórios</span>
            )}
          </div>
          <p className="texto-suave texto-pequeno">Itens obrigatórios ainda sem responsável nos eventos abertos.</p>
          {dados.pendenciasPrioritarias.length === 0 ? (
            <p className="texto-suave">Nenhuma pendência nos eventos que aceitam contribuições.</p>
          ) : (
            <ul className="lista-pendencias-prioritarias">
              {dados.pendenciasPrioritarias.map((p) => (
                <li key={`${p.eventoId}-${p.itemId}`}>
                  <span className="icone-quadrado" aria-hidden="true">
                    <IconeCaixa />
                  </span>
                  <span className="pendencia-prioritaria-info">
                    <strong>{p.itemNome}</strong>
                    <span className="texto-suave texto-pequeno">{p.eventoNome}</span>
                    <span className="selo-indicador selo-aviso">
                      Faltam {formatarQuantidade(p.quantidadeFaltante, p.unidade)}
                    </span>
                  </span>
                  <Link to={`/eventos/${p.eventoId}`} className="botao botao-pequeno">
                    Contribuir
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      <section aria-label="Contribuições recentes">
        <div className="secao-cabecalho">
          <h2>Contribuições recentes</h2>
        </div>
        {dados.contribuicoesRecentes.length === 0 ? (
          <p className="vazio">Nenhuma contribuição registrada ainda.</p>
        ) : (
          <ul className="grade-recentes">
            {dados.contribuicoesRecentes.map((c, i) => (
              <li key={i} className="cartao recente">
                <span className="avatar avatar-claro" aria-hidden="true">
                  {iniciais(c.participanteNome)}
                </span>
                <span>
                  <strong>{c.participanteNome}</strong> vai levar {formatarQuantidade(c.quantidade, c.unidade)} de{' '}
                  {c.itemNome} para o <Link to={`/eventos/${c.eventoId}`}>{c.eventoNome}</Link>
                  <span className="recente-quando texto-suave">
                    <IconeRelogio /> {tempoRelativo(c.atualizadoEm)}
                  </span>
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}

function CartaoEvento({ proximo }: { proximo: ProximoEvento }) {
  const { evento, inscritos, percentualCobertura, obrigatoriosPendentes } = proximo;
  return (
    <li className="cartao linha-evento">
      <CapaEvento tipo={evento.tipo} status={evento.status} prazoVencido={evento.prazoVencido} variante="linha" />
      <div className="linha-evento-corpo">
        <div>
          <h3>{evento.nome}</h3>
          <div className="cartao-evento-meta">
            <span>
              <IconeRelogio /> {formatarDataHora(evento.dataHora)}
            </span>
            <span>
              <IconePessoas /> {inscritos} {inscritos === 1 ? 'inscrito' : 'inscritos'}
            </span>
          </div>
        </div>
        <CaixaPrazo evento={evento} />
        <div>
          <div className="cartao-evento-cobertura">
            <span className="texto-suave">
              Cobertura dos itens
              {obrigatoriosPendentes > 0 && ` · ${obrigatoriosPendentes} obrigatório(s) sem dono`}
            </span>
            <strong>{percentualCobertura}% atendido</strong>
          </div>
          <BarraProgresso percentual={percentualCobertura} rotulo={`Cobertura de ${evento.nome}`} pequena />
        </div>
        <Link to={`/eventos/${evento.id}`} className="cartao-evento-acao">
          Ver evento <IconeSeta />
        </Link>
      </div>
    </li>
  );
}

function Indicador({
  rotulo,
  icone,
  valor,
  tom,
  children,
}: {
  rotulo: string;
  icone: ReactNode;
  valor: string;
  tom?: 'aviso' | 'destaque';
  children: ReactNode;
}) {
  return (
    <div className={`cartao indicador-cartao ${tom ? `indicador-${tom}` : ''}`}>
      <div className="indicador-topo">
        <span className="indicador-rotulo">{rotulo}</span>
        <span className="icone-quadrado" aria-hidden="true">
          {icone}
        </span>
      </div>
      <span className="indicador-valor">{valor}</span>
      <div className="indicador-detalhe">{children}</div>
    </div>
  );
}
