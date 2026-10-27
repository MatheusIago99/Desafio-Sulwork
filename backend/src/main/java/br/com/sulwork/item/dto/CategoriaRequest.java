package br.com.sulwork.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** {@code meta} é opcional: quantidade mínima de unidades que a categoria deve reunir. */
public record CategoriaRequest(
        @NotBlank @Size(max = 60) String nome,
        @Positive Integer meta) {
}
