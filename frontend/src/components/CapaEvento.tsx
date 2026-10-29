import type { ReactNode } from 'react';
import type { StatusEvento, TipoEvento } from '../api/types';
import { TIPO_LABEL } from '../utils/format';
import { IconeChama, IconeEstrela, IconeLanche, IconeTacas, IconeTalheres, IconeXicara } from './Icones';
import { StatusBadge } from './StatusBadge';

const ICONE_TIPO: Record<TipoEvento, (p: { tamanho?: number }) => JSX.Element> = {
  CAFE_DA_MANHA: IconeXicara,
  ALMOCO: IconeTalheres,
  LANCHE: IconeLanche,
  CHURRASCO: IconeChama,
  CONFRATERNIZACAO: IconeTacas,
  OUTRO: IconeEstrela,
};

interface Props {
  tipo: TipoEvento;
  status: StatusEvento;
  prazoVencido?: boolean;
  /** Informações sobrepostas na base da capa (ex.: data e inscritos). */
  children?: ReactNode;
  variante?: 'cartao' | 'destaque' | 'linha';
}

/**
 * Capa ilustrativa do evento: gradiente da marca com o ícone do tipo. Não é uma foto
 * (o sistema não guarda imagens), e o rótulo acessível deixa isso claro.
 * Rascunhos e cancelados usam o gradiente prata.
 */
export function CapaEvento({ tipo, status, prazoVencido, children, variante = 'cartao' }: Props) {
  const Icone = ICONE_TIPO[tipo];
  const apagada = status === 'RASCUNHO' || status === 'CANCELADO';
  return (
    <div
      className={`capa capa-${variante} ${apagada ? 'capa-apagada' : ''}`}
      role="img"
      aria-label={`Capa ilustrativa: ${TIPO_LABEL[tipo]}`}
    >
      <span className="capa-icone" aria-hidden="true">
        <Icone tamanho={variante === 'destaque' ? 72 : 56} />
      </span>
      <div className="capa-selos">
        <span className="capa-tipo">{TIPO_LABEL[tipo]}</span>
        <StatusBadge status={status} prazoVencido={prazoVencido} naCapa />
      </div>
      {children && <div className="capa-rodape">{children}</div>}
    </div>
  );
}
