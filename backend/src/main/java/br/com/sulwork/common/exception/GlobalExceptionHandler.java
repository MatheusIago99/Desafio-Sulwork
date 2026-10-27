package br.com.sulwork.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ApiError> conflito(ConflitoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiError> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex) {
        List<ApiError.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ApiError.CampoInvalido(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> requisicaoMalFormada(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição mal formada ou com valores inválidos", List.of());
    }

    /**
     * Última barreira para violações de constraint do banco (ex.: corrida entre duas
     * requisições que passaram pela validação do service ao mesmo tempo).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade", ex);
        return resposta(HttpStatus.CONFLICT, "Operação viola uma restrição de integridade dos dados", List.of());
    }

    private ResponseEntity<ApiError> resposta(HttpStatus status, String mensagem, List<ApiError.CampoInvalido> campos) {
        ApiError corpo = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
