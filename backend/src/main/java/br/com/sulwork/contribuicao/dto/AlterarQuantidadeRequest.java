package br.com.sulwork.contribuicao.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AlterarQuantidadeRequest(@NotNull @Positive Integer quantidade) {
}
