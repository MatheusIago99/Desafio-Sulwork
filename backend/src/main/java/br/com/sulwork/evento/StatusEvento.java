package br.com.sulwork.evento;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Status do evento e suas transições permitidas (máquina de estados).
 *
 * <pre>
 * RASCUNHO  -> ABERTO, CANCELADO
 * ABERTO    -> ENCERRADO, CANCELADO
 * ENCERRADO -> ABERTO (reabrir), REALIZADO, CANCELADO
 * REALIZADO -> (final)
 * CANCELADO -> (final)
 * </pre>
 */
public enum StatusEvento {
    RASCUNHO,
    ABERTO,
    ENCERRADO,
    REALIZADO,
    CANCELADO;

    private static final Map<StatusEvento, Set<StatusEvento>> TRANSICOES = new EnumMap<>(StatusEvento.class);

    static {
        TRANSICOES.put(RASCUNHO, EnumSet.of(ABERTO, CANCELADO));
        TRANSICOES.put(ABERTO, EnumSet.of(ENCERRADO, CANCELADO));
        TRANSICOES.put(ENCERRADO, EnumSet.of(ABERTO, REALIZADO, CANCELADO));
        TRANSICOES.put(REALIZADO, EnumSet.noneOf(StatusEvento.class));
        TRANSICOES.put(CANCELADO, EnumSet.noneOf(StatusEvento.class));
    }

    public Set<StatusEvento> transicoesPermitidas() {
        return Collections.unmodifiableSet(TRANSICOES.get(this));
    }

    public boolean podeTransicionarPara(StatusEvento destino) {
        return TRANSICOES.get(this).contains(destino);
    }

    public boolean isFinal() {
        return TRANSICOES.get(this).isEmpty();
    }

    /** Dados do evento, categorias e itens só podem ser editados enquanto o evento está sendo organizado. */
    public boolean permiteOrganizacao() {
        return this == RASCUNHO || this == ABERTO;
    }
}
