package br.com.sulwork.item.dto;

import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.common.validation.Limites;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotBlank @Size(max = 80) String nome,
        @NotNull Long categoriaId,
        @NotNull @Positive @Max(Limites.QUANTIDADE_MAXIMA) Integer quantidadeNecessaria,
        @Size(max = 20) String unidade,
        @NotNull Boolean obrigatorio,
        Set<Alergeno> alergenos) {
}
