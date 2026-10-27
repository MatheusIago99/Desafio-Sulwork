package br.com.sulwork.pendencia;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** {@code quantidade} é opcional: quando ausente, o participante assume tudo o que falta do item. */
public record AssumirItemRequest(@NotNull Long participanteId, @Positive Integer quantidade) {
}
