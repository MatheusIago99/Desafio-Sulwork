interface Props {
  percentual: number;
  rotulo?: string;
  pequena?: boolean;
}

export function BarraProgresso({ percentual, rotulo, pequena }: Props) {
  const valor = Math.max(0, Math.min(100, percentual));
  const faixa = valor >= 100 ? 'completo' : valor >= 50 ? 'medio' : 'baixo';
  return (
    <div
      className={`progresso ${pequena ? 'progresso-pequeno' : ''}`}
      role="progressbar"
      aria-valuenow={valor}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label={rotulo ?? 'Progresso'}
    >
      <div className={`progresso-barra progresso-${faixa}`} style={{ width: `${valor}%` }} />
    </div>
  );
}
