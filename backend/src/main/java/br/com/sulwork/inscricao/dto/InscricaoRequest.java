package br.com.sulwork.inscricao.dto;

import jakarta.validation.constraints.NotNull;

public record InscricaoRequest(@NotNull Long participanteId) {
}
