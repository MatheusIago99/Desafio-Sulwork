package br.com.sulwork.contribuicao.dto;

import java.time.LocalDateTime;

import br.com.sulwork.contribuicao.Contribuicao;

public record ContribuicaoResponse(
        Long id,
        Long itemId,
        String itemNome,
        String unidade,
        Long participanteId,
        String participanteNome,
        int quantidade,
        LocalDateTime atualizadoEm) {

    public static ContribuicaoResponse de(Contribuicao c) {
        return new ContribuicaoResponse(c.getId(), c.getItem().getId(), c.getItem().getNome(),
                c.getItem().getUnidade(), c.getParticipante().getId(), c.getParticipante().getNome(),
                c.getQuantidade(), c.getAtualizadoEm());
    }
}
