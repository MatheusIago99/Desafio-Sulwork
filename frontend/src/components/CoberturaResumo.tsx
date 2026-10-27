import type { Cobertura } from '../api/types';
import { BarraProgresso } from './BarraProgresso';

const CRITERIO_LABEL = {
  META: 'pela meta',
  ITENS: 'pelos itens',
  IGNORADA: 'sem itens',
} as const;

const RAIO = 52;
const CIRCUNFERENCIA = 2 * Math.PI * RAIO;

function AnelProgresso({ percentual }: { percentual: number }) {
  const valor = Math.max(0, Math.min(100, percentual));
  const faixa = valor >= 100 ? 'completo' : valor >= 50 ? 'medio' : 'baixo';
  return (
    <div
      className={`anel anel-${faixa}`}
      role="progressbar"
      aria-valuenow={valor}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label="Cobertura geral"
    >
      <svg viewBox="0 0 120 120" aria-hidden="true">
        <circle className="anel-trilho" cx="60" cy="60" r={RAIO} />
        <circle
          className="anel-valor"
          cx="60"
          cy="60"
          r={RAIO}
          strokeDasharray={CIRCUNFERENCIA}
          strokeDashoffset={CIRCUNFERENCIA * (1 - valor / 100)}
        />
      </svg>
      <span className="anel-numero">{valor}%</span>
    </div>
  );
}

export function CoberturaResumo({ cobertura }: { cobertura: Cobertura }) {
  const consideradas = cobertura.categorias.filter((c) => c.criterio !== 'IGNORADA');
  return (
    <section className="cartao cobertura" aria-label="Cobertura do evento">
      <div className="cobertura-topo">
        <AnelProgresso percentual={cobertura.percentualGeral} />
        <div className="cobertura-resumo">
          {cobertura.pronto ? (
            <span className="badge badge-pronto">Pronto para acontecer</span>
          ) : (
            <span className="badge badge-pendente">Ainda falta coisa</span>
          )}
          <h2>Cobertura do evento</h2>
          <p className="texto-suave">das necessidades já têm quem leve</p>
          <div className="cobertura-indicadores">
            <span className="indicador">
              <strong>
                {cobertura.obrigatoriosCompletos}/{cobertura.obrigatoriosTotal}
              </strong>
              <span>obrigatórios completos</span>
            </span>
            {cobertura.metasTotal > 0 && (
              <span className="indicador">
                <strong>
                  {cobertura.metasAtingidas}/{cobertura.metasTotal}
                </strong>
                <span>metas atingidas</span>
              </span>
            )}
          </div>
        </div>
      </div>
      {consideradas.length > 0 && (
        <ul className="cobertura-categorias">
          {consideradas.map((c) => (
            <li key={c.id}>
              <div className="linha-entre">
                <span>
                  {c.nome}{' '}
                  <span className="texto-suave">
                    ({CRITERIO_LABEL[c.criterio]}
                    {c.meta !== null && `: ${c.totalContribuido}/${c.meta}`})
                  </span>
                </span>
                <strong>{c.percentual}%</strong>
              </div>
              <BarraProgresso percentual={c.percentual} rotulo={`Cobertura de ${c.nome}`} pequena />
              {!c.metaAtingivel && (
                <small className="texto-aviso">
                  Meta maior que a soma dos itens ({c.totalNecessario}); ela nunca será atingida.
                </small>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
