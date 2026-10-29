package br.com.sulwork.contribuicao.dto;

import br.com.sulwork.common.validation.Limites;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlterarQuantidadeRequest(@NotNull @Positive @Max(Limites.QUANTIDADE_MAXIMA) Integer quantidade) {
}
