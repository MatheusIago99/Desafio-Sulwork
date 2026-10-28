import type { Alergeno, Prioridade, StatusEvento, TipoEvento } from '../api/types';

export const STATUS_LABEL: Record<StatusEvento, string> = {
  RASCUNHO: 'Rascunho',
  ABERTO: 'Aberto',
  ENCERRADO: 'Encerrado',
  REALIZADO: 'Realizado',
  CANCELADO: 'Cancelado',
};

/** Rótulo do botão que leva o evento a cada status. */
export const ACAO_STATUS: Record<StatusEvento, string> = {
  RASCUNHO: 'Voltar a rascunho',
  ABERTO: 'Abrir para contribuições',
  ENCERRADO: 'Encerrar contribuições',
  REALIZADO: 'Marcar como realizado',
  CANCELADO: 'Cancelar evento',
};

export const TIPO_LABEL: Record<TipoEvento, string> = {
  CAFE_DA_MANHA: 'Café da manhã',
  ALMOCO: 'Almoço',
  LANCHE: 'Lanche',
  CHURRASCO: 'Churrasco',
  CONFRATERNIZACAO: 'Confraternização',
  OUTRO: 'Outro',
};

export const ALERGENO_LABEL: Record<Alergeno, string> = {
  GLUTEN: 'Glúten',
  LACTOSE: 'Lactose',
  OVO: 'Ovo',
  AMENDOIM: 'Amendoim',
  CASTANHAS: 'Castanhas e nozes',
  SOJA: 'Soja',
  PEIXE: 'Peixe',
  CRUSTACEOS: 'Crustáceos e frutos do mar',
  GERGELIM: 'Gergelim',
};

export const ALERGENOS = Object.keys(ALERGENO_LABEL) as Alergeno[];

export const PRIORIDADE_LABEL: Record<Prioridade, string> = {
  ALTA: 'Obrigatório',
  MEDIA: 'Meta pendente',
  BAIXA: 'Opcional',
};

const formatoDataHora = new Intl.DateTimeFormat('pt-BR', {
  weekday: 'short',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

/** Datas vêm da API como LocalDateTime (sem fuso), no horário de São Paulo. */
export function formatarDataHora(iso: string): string {
  return formatoDataHora.format(new Date(iso));
}

/** Dia e mês abreviado, para o bloco de data dos cartões (ex.: { dia: '03', mes: 'nov' }). */
export function partesData(iso: string): { dia: string; mes: string } {
  const data = new Date(iso);
  return {
    dia: String(data.getDate()).padStart(2, '0'),
    mes: data.toLocaleDateString('pt-BR', { month: 'short' }).replace('.', ''),
  };
}

/** Converte o valor da API para o formato aceito por <input type="datetime-local">. */
export function paraInputDataHora(iso: string): string {
  return iso.slice(0, 16);
}

export function formatarQuantidade(quantidade: number, unidade: string | null): string {
  return unidade ? `${quantidade} ${unidade}` : String(quantidade);
}

export function formatarCpf(cpf: string): string {
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}
