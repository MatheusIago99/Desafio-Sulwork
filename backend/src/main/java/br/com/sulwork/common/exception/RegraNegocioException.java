package br.com.sulwork.common.exception;

/**
 * Violação de uma regra de negócio (ex.: prazo vencido, transição de status inválida).
 * Mapeada para HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
