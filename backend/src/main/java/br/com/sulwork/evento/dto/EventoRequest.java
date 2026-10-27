package br.com.sulwork.evento.dto;

import java.time.LocalDateTime;

import br.com.sulwork.evento.TipoEvento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 1000) String descricao,
        @NotNull TipoEvento tipo,
        @NotNull LocalDateTime dataHora,
        @NotNull LocalDateTime prazoAlteracoes) {
}
