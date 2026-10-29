import type { ReactNode } from 'react';

/** Cartão de indicador: rótulo, ícone, valor em destaque e um detalhe abaixo. */
export function Indicador({
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
