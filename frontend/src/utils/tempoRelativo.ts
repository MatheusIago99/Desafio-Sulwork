const MINUTO = 60 * 1000;
const HORA = 60 * MINUTO;
const DIA = 24 * HORA;

/** "há 5 min", "há 3 h", "há 2 dias": quanto tempo passou desde a data informada. */
export function tempoRelativo(iso: string, agora: Date = new Date()): string {
  const diferenca = Math.max(0, agora.getTime() - new Date(iso).getTime());
  if (diferenca < MINUTO) return 'agora há pouco';
  if (diferenca < HORA) return `há ${Math.floor(diferenca / MINUTO)} min`;
  if (diferenca < DIA) return `há ${Math.floor(diferenca / HORA)} h`;
  const dias = Math.floor(diferenca / DIA);
  return `há ${dias} ${dias === 1 ? 'dia' : 'dias'}`;
}
