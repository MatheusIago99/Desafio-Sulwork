import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { Dashboard, Participante } from '../api/types';
import { Alerta } from '../components/Alerta';
import { BarraProgresso } from '../components/BarraProgresso';
import { IconeSeta } from '../components/Icones';
import { StatusBadge } from '../components/StatusBadge';
import { useParticipanteAtual } from '../ParticipanteAtual';
import { TIPO_ICONE, TIPO_LABEL, formatarDataHora, partesData } from '../utils/format';

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
      .catch((ex: unknown) => setErro(ex instanceof Error ? ex.message : 'Erro ao carregar o dashboard'));
  }, []);

  const nome = participantes.find((p) => p.id === participanteId)?.nome.split(' ')[0];

  return (
    <div className="pagina">
      <div className="pagina-cabecalho">
        <div>
          <h1>{nome ? `Olá, ${nome}!` : 'Olá!'}</h1>
          <p className="pagina-subtitulo">Um resumo dos eventos de alimentação da Sulwork.</p>
        </div>
        <Link to="/eventos?novo=1" className="botao botao-primario">
          Novo evento
        </Link>
      </div>

      {erro && <Alerta tipo="erro">{erro}</Alerta>}
      {!dados && !erro && <p className="texto-suave">Carregando...</p>}
      {dados && <Conteudo dados={dados} />}
    </div>
  );
}

function Conteudo({ dados }: { dados: Dashboard }) {
  const maiorTipo = Math.max(1, ...dados.eventosPorTipo.map((t) => t.quantidade));
  return (
    <>
      <section className="grade-indicadores" aria-label="Indicadores">
        <Indicador
          rotulo="Eventos abertos"
          valor={String(dados.eventosAbertos)}
          detalhe={`${dados.eventosEmOrganizacao} em organização`}
        />
        <Indicador
          rotulo="Cobertura média"
          valor={dados.coberturaMedia === null ? '—' : `${dados.coberturaMedia}%`}
          detalhe={dados.coberturaMedia === null ? 'nenhum evento aberto' : 'dos eventos abertos'}
        />
        <Indicador
          rotulo="Obrigatórios sem dono"
          valor={String(dados.obrigatoriosPendentes)}
          detalhe="itens obrigatórios incompletos"
          destaque={dados.obrigatoriosPendentes > 0}
        />
        <Indicador
          rotulo="Participantes"
          valor={String(dados.participantesCadastrados)}
          detalhe={`${dados.inscricoesEmEventosAbertos} inscrições em eventos abertos`}
        />
      </section>

      <div className="grade-dashboard">
        <section className="cartao">
          <div className="linha-entre">
            <h2>Próximos eventos</h2>
            <Link to="/eventos" className="link-discreto">
              Ver todos
            </Link>
          </div>
          {dados.proximosEventos.length === 0 ? (
            <p className="vazio">
              Nenhum evento por vir. <Link to="/eventos?novo=1">Crie o primeiro.</Link>
            </p>
          ) : (
            <ul className="lista-proximos">
              {dados.proximosEventos.map(({ evento, inscritos, percentualCobertura, obrigatoriosPendentes, pronto }) => {
                const data = partesData(evento.dataHora);
                return (
                  <li key={evento.id}>
                    <Link to={`/eventos/${evento.id}`} className="proximo">
                      <span className="evento-data evento-data-claro" aria-hidden="true">
                        <strong>{data.dia}</strong>
                        <span>{data.mes}</span>
                      </span>
                      <span className="proximo-info">
                        <strong>{evento.nome}</strong>
                        <span className="texto-suave">
                          {TIPO_ICONE[evento.tipo]} {TIPO_LABEL[evento.tipo]} · {formatarDataHora(evento.dataHora)}
                        </span>
                        <span className="proximo-meta">
                          <StatusBadge status={evento.status} />
                          <span className="texto-suave">
                            {inscritos} {inscritos === 1 ? 'inscrito' : 'inscritos'}
                          </span>
                          {obrigatoriosPendentes > 0 ? (
                            <span className="texto-aviso">
                              {obrigatoriosPendentes} obrigatório(s) sem dono
                            </span>
                          ) : (
                            pronto && <span className="texto-sucesso">Pronto</span>
                          )}
                        </span>
                      </span>
                      <span className="proximo-cobertura">
                        <span>
                          <strong>{percentualCobertura}%</strong> coberto
                        </span>
                        <BarraProgresso
                          percentual={percentualCobertura}
                          rotulo={`Cobertura de ${evento.nome}`}
                          pequena
                        />
                      </span>
                      <span className="proximo-seta">
                        <IconeSeta />
                      </span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <section className="cartao">
          <h2>Eventos por tipo</h2>
          <ul className="barras-tipo" aria-label="Quantidade de eventos por tipo">
            {dados.eventosPorTipo.map(({ tipo, quantidade }) => (
              <li key={tipo} title={`${TIPO_LABEL[tipo]}: ${quantidade} evento(s)`}>
                <span className="barras-tipo-rotulo">
                  <span aria-hidden="true">{TIPO_ICONE[tipo]}</span> {TIPO_LABEL[tipo]}
                </span>
                <span className="barras-tipo-trilho">
                  <span
                    className="barras-tipo-barra"
                    style={{ width: quantidade === 0 ? 0 : `${Math.max(6, (quantidade / maiorTipo) * 100)}%` }}
                  />
                </span>
                <span className="barras-tipo-valor">{quantidade}</span>
              </li>
            ))}
          </ul>
        </section>
      </div>
    </>
  );
}

function Indicador({
  rotulo,
  valor,
  detalhe,
  destaque,
}: {
  rotulo: string;
  valor: string;
  detalhe: string;
  destaque?: boolean;
}) {
  return (
    <div className={`cartao indicador-cartao ${destaque ? 'indicador-destaque' : ''}`}>
      <span className="indicador-rotulo">{rotulo}</span>
      <span className="indicador-valor">{valor}</span>
      <span className="indicador-detalhe">{detalhe}</span>
    </div>
  );
}
