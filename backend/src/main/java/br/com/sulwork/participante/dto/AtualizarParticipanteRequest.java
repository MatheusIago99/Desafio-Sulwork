package br.com.sulwork.participante.dto;

import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Atualização do participante. O CPF não entra: ele identifica o colaborador e não muda. */
public record AtualizarParticipanteRequest(
        @NotBlank @Size(max = 120) String nome,
        Set<Alergeno> restricoes,
        @Size(max = 500) String observacoesAlimentares) {
}
