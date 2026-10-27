package br.com.sulwork.alergeno;

import java.util.List;

/**
 * Visão consolidada das restrições alimentares dos inscritos e de como elas se
 * relacionam com os alérgenos dos itens do evento.
 */
public record ResumoRestricoes(
        int totalInscritos,
        List<RestricaoNoEvento> restricoes,
        List<Observacao> observacoes,
        List<ItemComAlergenos> itens) {

    /** Quantos e quais inscritos têm restrição a um alérgeno. */
    public record RestricaoNoEvento(Alergeno alergeno, String descricao, List<String> participantes) {
    }

    /** Restrições em texto livre (ex.: vegetariano), exibidas como informação. */
    public record Observacao(String participante, String texto) {
    }

    /**
     * Um item, seus alérgenos e quais inscritos são afetados por cada um.
     * {@code seguroParaTodos} é verdadeiro quando nenhum inscrito tem restrição aos alérgenos do item.
     */
    public record ItemComAlergenos(
            Long itemId,
            String itemNome,
            List<Alergeno> alergenos,
            List<Conflito> conflitos,
            int participantesAfetados,
            boolean seguroParaTodos) {
    }

    public record Conflito(Alergeno alergeno, List<String> participantes) {
    }
}
