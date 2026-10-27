package br.com.sulwork.pendencia;

public enum Prioridade {
    /** Item obrigatório incompleto: o evento não fica pronto sem ele. */
    ALTA,
    /** Item opcional de uma categoria cuja meta ainda não foi atingida. */
    MEDIA,
    /** Item opcional sem meta pendente: bom ter, mas não bloqueia nada. */
    BAIXA
}
