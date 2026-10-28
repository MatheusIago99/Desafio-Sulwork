package br.com.sulwork.common.validation;

/**
 * Utilitário de CPF: normalização (remove máscara) e validação dos dígitos verificadores.
 */
public final class Cpf {

    private Cpf() {
    }

    public static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("[.\\-\\s]", "");
    }

    /**
     * Mascara o CPF para exibição, mostrando só os seis dígitos do meio
     * (ex.: 52998224725 -> ***.982.247-**), padrão usado em publicações do governo
     * para não expor o número completo.
     */
    public static String mascarar(String cpf) {
        String digitos = normalizar(cpf);
        if (digitos == null || digitos.length() != 11) {
            return "***.***.***-**";
        }
        return "***." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-**";
    }

    public static boolean isValido(String cpf) {
        String digitos = normalizar(cpf);
        if (digitos == null || !digitos.matches("\\d{11}")) {
            return false;
        }
        // Sequências repetidas (000.000.000-00, 111...) passam no cálculo, mas não são CPFs válidos.
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(digitos, 9) == digitos.charAt(9) - '0'
                && digitoVerificador(digitos, 10) == digitos.charAt(10) - '0';
    }

    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
