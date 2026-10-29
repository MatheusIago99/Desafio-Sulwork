package br.com.sulwork.common.validation;

/** Limites de entrada compartilhados pelas requisições. */
public final class Limites {

    /**
     * Teto para quantidades e metas. Evento de alimentação não chega perto disso, e o teto
     * impede valores absurdos que distorceriam a cobertura (somas perto do limite de int).
     */
    public static final int QUANTIDADE_MAXIMA = 100_000;

    private Limites() {
    }
}
