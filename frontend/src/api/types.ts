export type StatusEvento = 'RASCUNHO' | 'ABERTO' | 'ENCERRADO' | 'REALIZADO' | 'CANCELADO';
export type TipoEvento = 'CAFE_DA_MANHA' | 'ALMOCO' | 'LANCHE' | 'CHURRASCO' | 'CONFRATERNIZACAO' | 'OUTRO';
export type Alergeno =
  | 'GLUTEN'
  | 'LACTOSE'
  | 'OVO'
  | 'AMENDOIM'
  | 'CASTANHAS'
  | 'SOJA'
  | 'PEIXE'
  | 'CRUSTACEOS'
  | 'GERGELIM';

export interface Evento {
  id: number;
  nome: string;
  descricao: string | null;
  tipo: TipoEvento;
  dataHora: string;
  prazoAlteracoes: string;
  status: StatusEvento;
  prazoVencido: boolean;
  aceitaAlteracoes: boolean;
  transicoesPermitidas: StatusEvento[];
  criadoEm: string;
}

export interface EventoInput {
  nome: string;
  descricao: string;
  tipo: TipoEvento;
  dataHora: string;
  prazoAlteracoes: string;
}

export interface Participante {
  id: number;
  nome: string;
  cpf: string;
  restricoes: Alergeno[];
  observacoesAlimentares: string | null;
}

export interface ParticipanteInput {
  nome: string;
  cpf: string;
  restricoes: Alergeno[];
  observacoesAlimentares: string;
}

export interface Inscricao {
  id: number;
  participante: Participante;
  inscritoEm: string;
}

export interface Categoria {
  id: number;
  nome: string;
  meta: number | null;
}

export interface Item {
  id: number;
  nome: string;
  categoria: Categoria;
  quantidadeNecessaria: number;
  unidade: string | null;
  obrigatorio: boolean;
  alergenos: Alergeno[];
  quantidadeContribuida: number;
  quantidadeFaltante: number;
}

export interface ItemInput {
  nome: string;
  categoriaId: number;
  quantidadeNecessaria: number;
  unidade: string;
  obrigatorio: boolean;
  alergenos: Alergeno[];
}

export interface Contribuicao {
  id: number;
  itemId: number;
  itemNome: string;
  unidade: string | null;
  participanteId: number;
  participanteNome: string;
  quantidade: number;
  atualizadoEm: string;
}

export type Criterio = 'META' | 'ITENS' | 'IGNORADA';

export interface ItemCobertura {
  id: number;
  nome: string;
  obrigatorio: boolean;
  quantidadeNecessaria: number;
  quantidadeContribuida: number;
  percentual: number;
}

export interface CategoriaCobertura {
  id: number;
  nome: string;
  meta: number | null;
  totalNecessario: number;
  totalContribuido: number;
  percentual: number;
  criterio: Criterio;
  metaAtingivel: boolean;
  itens: ItemCobertura[];
}

export interface Cobertura {
  percentualGeral: number;
  obrigatoriosTotal: number;
  obrigatoriosCompletos: number;
  metasTotal: number;
  metasAtingidas: number;
  pronto: boolean;
  categorias: CategoriaCobertura[];
}

export interface Conflito {
  alergeno: Alergeno;
  participantes: string[];
}

export interface ResumoRestricoes {
  totalInscritos: number;
  restricoes: { alergeno: Alergeno; descricao: string; participantes: string[] }[];
  observacoes: { participante: string; texto: string }[];
  itens: {
    itemId: number;
    itemNome: string;
    alergenos: Alergeno[];
    conflitos: Conflito[];
    participantesAfetados: number;
    seguroParaTodos: boolean;
  }[];
}

export interface MinhaContribuicao {
  contribuicaoId: number;
  eventoId: number;
  eventoNome: string;
  eventoDataHora: string;
  prazoAlteracoes: string;
  eventoStatus: StatusEvento;
  prazoVencido: boolean;
  aceitaAlteracoes: boolean;
  itemId: number;
  itemNome: string;
  unidade: string | null;
  obrigatorio: boolean;
  quantidade: number;
  quantidadeNecessaria: number;
  quantidadeContribuidaTotal: number;
}

export interface ProximoEvento {
  evento: Evento;
  inscritos: number;
  percentualCobertura: number;
  obrigatoriosPendentes: number;
  pronto: boolean;
}

export interface Dashboard {
  coberturaMedia: number | null;
  obrigatoriosPendentes: number;
  proximosEventosTotal: number;
  proximosEventosNaSemana: number;
  contribuicoesTotal: number;
  ultimaContribuicaoEm: string | null;
  proximosEventos: ProximoEvento[];
  pendenciasPrioritarias: {
    eventoId: number;
    eventoNome: string;
    itemId: number;
    itemNome: string;
    categoria: string;
    quantidadeFaltante: number;
    unidade: string | null;
  }[];
  contribuicoesRecentes: {
    eventoId: number;
    eventoNome: string;
    participanteNome: string;
    itemNome: string;
    quantidade: number;
    unidade: string | null;
    atualizadoEm: string;
  }[];
}

export type Prioridade = 'ALTA' | 'MEDIA' | 'BAIXA';

export interface ItemPendente {
  itemId: number;
  nome: string;
  categoria: string;
  unidade: string | null;
  obrigatorio: boolean;
  prioridade: Prioridade;
  quantidadeNecessaria: number;
  quantidadeContribuida: number;
  quantidadeFaltante: number;
  percentual: number;
  alergenos: Alergeno[];
  conflitos: Conflito[];
  seguroParaTodos: boolean;
  minhaContribuicao: number | null;
}

export interface Pendencias {
  aceitaContribuicoes: boolean;
  totalPendentes: number;
  obrigatoriosPendentes: number;
  itens: ItemPendente[];
}
