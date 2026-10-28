import type { ResumoRestricoes } from '../../api/types';
import { AlergenoChips } from '../../components/Alergenos';
import { ALERGENO_LABEL } from '../../utils/format';

export function AbaRestricoes({ resumo }: { resumo: ResumoRestricoes }) {
  const itensComConflito = resumo.itens.filter((i) => !i.seguroParaTodos);
  const itensSeguros = resumo.itens.filter((i) => i.seguroParaTodos);

  return (
    <div className="grade-duas-colunas">
      <section className="cartao">
        <h3>Restrições dos inscritos</h3>
        {resumo.restricoes.length === 0 && resumo.observacoes.length === 0 ? (
          <p className="texto-suave">Nenhum inscrito informou restrições.</p>
        ) : (
          <ul className="lista-simples">
            {resumo.restricoes.map((r) => (
              <li key={r.alergeno}>
                <strong>{r.descricao}</strong>: {r.participantes.length} de {resumo.totalInscritos}{' '}
                <span className="texto-suave">({r.participantes.join(', ')})</span>
              </li>
            ))}
            {resumo.observacoes.map((o) => (
              <li key={o.participante}>
                <strong>{o.participante}</strong>: <span className="texto-suave">{o.texto}</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="cartao">
        <h3>Itens que afetam alguém</h3>
        {itensComConflito.length === 0 ? (
          <p className="texto-suave">Nenhum item conflita com as restrições dos inscritos.</p>
        ) : (
          <ul className="lista-simples">
            {itensComConflito.map((item) => (
              <li key={item.itemId}>
                <strong>{item.itemNome}</strong>{' '}
                <span className="texto-aviso">afeta {item.participantesAfetados} pessoa(s)</span>
                <ul>
                  {item.conflitos.map((c) => (
                    <li key={c.alergeno}>
                      {ALERGENO_LABEL[c.alergeno]}: {c.participantes.join(', ')}
                    </li>
                  ))}
                </ul>
              </li>
            ))}
          </ul>
        )}
        {itensComConflito.length > 0 && (
          <p className="texto-suave">
            Considere incluir alternativas sem esses alérgenos.
          </p>
        )}
      </section>

      <section className="cartao campo-largo">
        <h3>Seguros para todos os inscritos</h3>
        {itensSeguros.length === 0 ? (
          <p className="texto-suave">Nenhum item.</p>
        ) : (
          <ul className="lista-simples">
            {itensSeguros.map((item) => (
              <li key={item.itemId}>
                {item.itemNome} <AlergenoChips alergenos={item.alergenos} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
