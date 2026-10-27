import type {
  Categoria,
  Cobertura,
  Contribuicao,
  Evento,
  Inscricao,
  Item,
  Participante,
  Pendencias,
  ResumoRestricoes,
} from '../../api/types';

/** Tudo que a página de um evento carrega da API de uma vez. */
export interface DadosEvento {
  evento: Evento;
  cobertura: Cobertura;
  pendencias: Pendencias;
  categorias: Categoria[];
  itens: Item[];
  inscricoes: Inscricao[];
  contribuicoes: Contribuicao[];
  restricoes: ResumoRestricoes;
  participantes: Participante[];
}

/**
 * Executa uma alteração na API, exibe o erro (se houver) e recarrega os dados do evento.
 * Retorna true quando a operação deu certo.
 */
export type Executar = (acao: () => Promise<unknown>, sucesso?: string) => Promise<boolean>;
