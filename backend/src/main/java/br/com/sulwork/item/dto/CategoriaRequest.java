package br.com.sulwork.item.dto;

import br.com.sulwork.common.validation.Limites;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** {@code meta} é opcional: quantidade mínima de unidades que a categoria deve reunir. */
public record CategoriaRequest(
        @NotBlank @Size(max = 60) String nome,
        @Positive @Max(Limites.QUANTIDADE_MAXIMA) Integer meta) {
}
