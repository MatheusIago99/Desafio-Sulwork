package br.com.sulwork.contribuicao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ContribuicaoRequest(
        @NotNull Long participanteId,
        @NotNull Long itemId,
        @NotNull @Positive Integer quantidade) {
}
