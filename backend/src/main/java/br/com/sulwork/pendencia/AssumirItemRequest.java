package br.com.sulwork.pendencia;

import br.com.sulwork.common.validation.Limites;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** {@code quantidade} é opcional: quando ausente, o participante assume tudo o que falta do item. */
public record AssumirItemRequest(@NotNull Long participanteId, @Positive @Max(Limites.QUANTIDADE_MAXIMA) Integer quantidade) {
}
