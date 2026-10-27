package br.com.sulwork.cobertura;

import java.util.List;

/** Resultado do cálculo de cobertura de um evento. Percentuais vão de 0 a 100. */
public record Cobertura(
        int percentualGeral,
        int obrigatoriosTotal,
        int obrigatoriosCompletos,
        int metasTotal,
        int metasAtingidas,
        boolean pronto,
        List<CategoriaCobertura> categorias) {

    public enum Criterio {
        /** Categoria com meta: o progresso é o total contribuído sobre a meta. */
        META,
        /** Categoria sem meta: média ponderada dos itens (obrigatório pesa 2, opcional 1). */
        ITENS,
        /** Categoria sem itens e sem meta: não entra no cálculo. */
        IGNORADA
    }

    public record CategoriaCobertura(
            Long id,
            String nome,
            Integer meta,
            int totalNecessario,
            int totalContribuido,
            int percentual,
            Criterio criterio,
            boolean metaAtingivel,
            List<ItemCobertura> itens) {
    }

    public record ItemCobertura(
            Long id,
            String nome,
            boolean obrigatorio,
            int quantidadeNecessaria,
            int quantidadeContribuida,
            int percentual) {

        public boolean completo() {
            return quantidadeContribuida >= quantidadeNecessaria;
        }
    }
}
