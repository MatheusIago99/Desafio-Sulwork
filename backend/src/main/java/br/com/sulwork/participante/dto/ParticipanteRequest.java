package br.com.sulwork.participante.dto;

import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.common.validation.CpfValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipanteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @CpfValido String cpf,
        Set<Alergeno> restricoes,
        @Size(max = 500) String observacoesAlimentares) {
}
