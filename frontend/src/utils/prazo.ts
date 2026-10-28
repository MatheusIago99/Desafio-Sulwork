const MINUTO = 60 * 1000;
const HORA = 60 * MINUTO;
const DIA = 24 * HORA;

/**
 * Texto curto com o tempo que falta até o prazo ("Encerra em 2 dias", "Encerra em 5 h").
 * Retorna null se o prazo já passou. É só informativo: quem decide se ainda dá para
 * alterar é o backend, pelo relógio do servidor.
 */
export function tempoRestante(prazoIso: string, agora: Date = new Date()): string | null {
  const diferenca = new Date(prazoIso).getTime() - agora.getTime();
  if (diferenca <= 0) return null;
  if (diferenca >= DIA) {
    const dias = Math.floor(diferenca / DIA);
    return `Encerra em ${dias} ${dias === 1 ? 'dia' : 'dias'}`;
  }
  if (diferenca >= HORA) return `Encerra em ${Math.floor(diferenca / HORA)} h`;
  return `Encerra em ${Math.max(1, Math.floor(diferenca / MINUTO))} min`;
}

/** Menos de 24 horas para o prazo: vale destacar. */
export function prazoProximo(prazoIso: string, agora: Date = new Date()): boolean {
  const diferenca = new Date(prazoIso).getTime() - agora.getTime();
  return diferenca > 0 && diferenca < DIA;
}
