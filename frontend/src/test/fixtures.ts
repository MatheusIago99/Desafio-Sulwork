import type { Cobertura, ItemPendente, Pendencias } from '../api/types';

export function itemPendente(sobrescrever: Partial<ItemPendente> = {}): ItemPendente {
  return {
    itemId: 1,
    nome: 'Bolo de cenoura',
    categoria: 'Comidas',
    unidade: null,
    obrigatorio: true,
    prioridade: 'ALTA',
    quantidadeNecessaria: 2,
    quantidadeContribuida: 0,
    quantidadeFaltante: 2,
    percentual: 0,
    alergenos: [],
    conflitos: [],
    seguroParaTodos: true,
    minhaContribuicao: null,
    ...sobrescrever,
  };
}

export function pendencias(itens: ItemPendente[], aceitaContribuicoes = true): Pendencias {
  return {
    aceitaContribuicoes,
    totalPendentes: itens.length,
    obrigatoriosPendentes: itens.filter((i) => i.obrigatorio).length,
    itens,
  };
}

export function cobertura(sobrescrever: Partial<Cobertura> = {}): Cobertura {
  return {
    percentualGeral: 0,
    obrigatoriosTotal: 0,
    obrigatoriosCompletos: 0,
    metasTotal: 0,
    metasAtingidas: 0,
    pronto: false,
    categorias: [],
    ...sobrescrever,
  };
}
