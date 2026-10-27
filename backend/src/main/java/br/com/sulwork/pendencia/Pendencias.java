package br.com.sulwork.pendencia;

import java.util.List;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.alergeno.ResumoRestricoes.Conflito;

/**
 * Lista "O que falta?": itens que ainda precisam de contribuição, já priorizados.
 */
public record Pendencias(
        boolean aceitaContribuicoes,
        int totalPendentes,
        int obrigatoriosPendentes,
        List<ItemPendente> itens) {

    public record ItemPendente(
            Long itemId,
            String nome,
            String categoria,
            String unidade,
            boolean obrigatorio,
            Prioridade prioridade,
            int quantidadeNecessaria,
            int quantidadeContribuida,
            int quantidadeFaltante,
            int percentual,
            List<Alergeno> alergenos,
            List<Conflito> conflitos,
            boolean seguroParaTodos,
            Integer minhaContribuicao) {
    }
}
