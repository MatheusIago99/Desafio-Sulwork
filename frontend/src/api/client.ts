import type {
  Categoria,
  Cobertura,
  Contribuicao,
  Dashboard,
  Evento,
  EventoInput,
  Inscricao,
  Item,
  ItemInput,
  Participante,
  ParticipanteInput,
  Pendencias,
  ResumoRestricoes,
  StatusEvento,
} from './types';

const BASE_URL = (import.meta.env.VITE_API_URL as string | undefined) ?? '/api';

export interface CampoInvalido {
  campo: string;
  mensagem: string;
}

/** Erro devolvido pela API no formato padronizado do backend. */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly campos: CampoInvalido[] = [],
  ) {
    super(message);
    this.name = 'ApiError';
  }

  /** Mensagem amigável incluindo os campos inválidos, quando houver. */
  detalhada(): string {
    if (this.campos.length === 0) return this.message;
    return `${this.message}: ${this.campos.map((c) => `${c.campo} ${c.mensagem}`).join('; ')}`;
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(
      0,
      'Não foi possível conectar à API. Se a aplicação estiver hospedada em plano gratuito, o servidor pode levar até 1 minuto para iniciar; tente novamente em instantes.',
    );
  }

  if (response.status === 204) return undefined as T;

  const texto = await response.text();
  const dados = texto ? JSON.parse(texto) : undefined;
  if (!response.ok) {
    throw new ApiError(response.status, dados?.mensagem ?? `Erro ${response.status}`, dados?.campos ?? []);
  }
  return dados as T;
}

const get = <T>(path: string) => request<T>('GET', path);
const post = <T>(path: string, body: unknown) => request<T>('POST', path, body);
const put = <T>(path: string, body: unknown) => request<T>('PUT', path, body);
const patch = <T>(path: string, body: unknown) => request<T>('PATCH', path, body);
const del = (path: string) => request<void>('DELETE', path);

export const api = {
  dashboard: () => get<Dashboard>('/dashboard'),
  eventos: {
    listar: (status?: StatusEvento) => get<Evento[]>(`/eventos${status ? `?status=${status}` : ''}`),
    buscar: (id: number) => get<Evento>(`/eventos/${id}`),
    criar: (dados: EventoInput) => post<Evento>('/eventos', dados),
    atualizar: (id: number, dados: EventoInput) => put<Evento>(`/eventos/${id}`, dados),
    alterarStatus: (id: number, status: StatusEvento) => patch<Evento>(`/eventos/${id}/status`, { status }),
    excluir: (id: number) => del(`/eventos/${id}`),
  },
  participantes: {
    listar: (busca?: string) =>
      get<Participante[]>(`/participantes${busca ? `?busca=${encodeURIComponent(busca)}` : ''}`),
    criar: (dados: ParticipanteInput) => post<Participante>('/participantes', dados),
    atualizar: (id: number, dados: ParticipanteInput) => put<Participante>(`/participantes/${id}`, dados),
    excluir: (id: number) => del(`/participantes/${id}`),
  },
  inscricoes: {
    listar: (eventoId: number) => get<Inscricao[]>(`/eventos/${eventoId}/inscricoes`),
    inscrever: (eventoId: number, participanteId: number) =>
      post<Inscricao>(`/eventos/${eventoId}/inscricoes`, { participanteId }),
    cancelar: (eventoId: number, participanteId: number) => del(`/eventos/${eventoId}/inscricoes/${participanteId}`),
  },
  categorias: {
    listar: (eventoId: number) => get<Categoria[]>(`/eventos/${eventoId}/categorias`),
    criar: (eventoId: number, dados: { nome: string; meta: number | null }) =>
      post<Categoria>(`/eventos/${eventoId}/categorias`, dados),
    atualizar: (eventoId: number, id: number, dados: { nome: string; meta: number | null }) =>
      put<Categoria>(`/eventos/${eventoId}/categorias/${id}`, dados),
    excluir: (eventoId: number, id: number) => del(`/eventos/${eventoId}/categorias/${id}`),
  },
  itens: {
    listar: (eventoId: number) => get<Item[]>(`/eventos/${eventoId}/itens`),
    criar: (eventoId: number, dados: ItemInput) => post<Item>(`/eventos/${eventoId}/itens`, dados),
    atualizar: (eventoId: number, id: number, dados: ItemInput) => put<Item>(`/eventos/${eventoId}/itens/${id}`, dados),
    excluir: (eventoId: number, id: number) => del(`/eventos/${eventoId}/itens/${id}`),
  },
  contribuicoes: {
    listar: (eventoId: number) => get<Contribuicao[]>(`/eventos/${eventoId}/contribuicoes`),
    criar: (eventoId: number, dados: { participanteId: number; itemId: number; quantidade: number }) =>
      post<Contribuicao>(`/eventos/${eventoId}/contribuicoes`, dados),
    alterar: (eventoId: number, id: number, quantidade: number) =>
      put<Contribuicao>(`/eventos/${eventoId}/contribuicoes/${id}`, { quantidade }),
    remover: (eventoId: number, id: number) => del(`/eventos/${eventoId}/contribuicoes/${id}`),
  },
  cobertura: (eventoId: number) => get<Cobertura>(`/eventos/${eventoId}/cobertura`),
  restricoes: (eventoId: number) => get<ResumoRestricoes>(`/eventos/${eventoId}/restricoes`),
  pendencias: {
    listar: (eventoId: number, participanteId?: number) =>
      get<Pendencias>(`/eventos/${eventoId}/pendencias${participanteId ? `?participanteId=${participanteId}` : ''}`),
    assumir: (eventoId: number, itemId: number, participanteId: number, quantidade?: number) =>
      post<Contribuicao>(`/eventos/${eventoId}/pendencias/${itemId}/assumir`, { participanteId, quantidade }),
  },
};
