import { prazoProximo, tempoRestante } from '../utils/prazo';

/** Selo com o tempo que falta para o prazo de alterações; nada aparece se ele já passou. */
export function PrazoRestante({ prazo }: { prazo: string }) {
  const texto = tempoRestante(prazo);
  if (!texto) return null;
  return <span className={`prazo-restante ${prazoProximo(prazo) ? 'prazo-restante-urgente' : ''}`}>{texto}</span>;
}
