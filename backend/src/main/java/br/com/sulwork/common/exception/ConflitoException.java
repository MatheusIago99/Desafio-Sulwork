package br.com.sulwork.common.exception;

/**
 * Conflito com o estado atual dos dados (ex.: CPF ou inscrição duplicada).
 * Mapeada para HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
