package br.com.sulwork.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<CampoInvalido> campos) {

    public record CampoInvalido(String campo, String mensagem) {
    }
}
