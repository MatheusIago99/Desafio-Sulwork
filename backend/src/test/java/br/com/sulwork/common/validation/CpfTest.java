package br.com.sulwork.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "11144477735", "111.444.777-35"})
    void deveAceitarCpfsValidosComOuSemMascara(String cpf) {
        assertThat(Cpf.isValido(cpf)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224724",   // dígito verificador errado
            "5299822472",    // 10 dígitos
            "529982247250",  // 12 dígitos
            "11111111111",   // sequência repetida
            "00000000000",
            "abcdefghijk"
    })
    void deveRejeitarCpfsInvalidos(String cpf) {
        assertThat(Cpf.isValido(cpf)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void deveRejeitarVazio(String cpf) {
        assertThat(Cpf.isValido(cpf)).isFalse();
    }

    @Test
    void deveNormalizarRemovendoMascara() {
        assertThat(Cpf.normalizar("529.982.247-25")).isEqualTo("52998224725");
    }
}
