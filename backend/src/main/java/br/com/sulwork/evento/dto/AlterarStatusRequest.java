package br.com.sulwork.evento.dto;

import br.com.sulwork.evento.StatusEvento;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(@NotNull StatusEvento status) {
}
