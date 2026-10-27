import type { Cobertura } from '../api/types';
import { BarraProgresso } from './BarraProgresso';

const CRITERIO_LABEL = {
  META: 'pela meta',
  ITENS: 'pelos itens',
  IGNORADA: 'sem itens',
} as const;

export function CoberturaResumo({ cobertura }: { cobertura: Cobertura }) {
  const consideradas = cobertura.categorias.filter((c) => c.criterio !== 'IGNORADA');
  return (
    <section className="cartao cobertura" aria-label="Cobertura do evento">
      <div className="cobertura-topo">
        <div>
          <span className="cobertura-numero">{cobertura.percentualGeral}%</span>
          <span className="texto-suave"> das necessidades atendidas</span>
        </div>
        {cobertura.pronto ? (
          <span className="badge badge-pronto">Pronto para acontecer</span>
        ) : (
          <span className="badge badge-pendente">Ainda falta coisa</span>
        )}
      </div>
      <BarraProgresso percentual={cobertura.percentualGeral} rotulo="Cobertura geral" />
      <div className="cobertura-indicadores">
        <span>
          Obrigatórios completos:{' '}
          <strong>
            {cobertura.obrigatoriosCompletos}/{cobertura.obrigatoriosTotal}
          </strong>
        </span>
        {cobertura.metasTotal > 0 && (
          <span>
            Metas atingidas:{' '}
            <strong>
              {cobertura.metasAtingidas}/{cobertura.metasTotal}
            </strong>
          </span>
        )}
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
