package br.com.sulwork.evento;

import static br.com.sulwork.evento.StatusEvento.ABERTO;
import static br.com.sulwork.evento.StatusEvento.CANCELADO;
import static br.com.sulwork.evento.StatusEvento.ENCERRADO;
import static br.com.sulwork.evento.StatusEvento.RASCUNHO;
import static br.com.sulwork.evento.StatusEvento.REALIZADO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatusEventoTest {

    @ParameterizedTest(name = "{0} -> {1} = {2}")
    @CsvSource({
            "RASCUNHO, ABERTO, true",
            "RASCUNHO, CANCELADO, true",
            "RASCUNHO, ENCERRADO, false",
            "RASCUNHO, REALIZADO, false",
            "ABERTO, ENCERRADO, true",
            "ABERTO, CANCELADO, true",
            "ABERTO, RASCUNHO, false",
            "ABERTO, REALIZADO, false",
            "ENCERRADO, ABERTO, true",
            "ENCERRADO, REALIZADO, true",
            "ENCERRADO, CANCELADO, true",
            "REALIZADO, CANCELADO, false",
            "REALIZADO, ABERTO, false",
            "CANCELADO, ABERTO, false",
            "CANCELADO, RASCUNHO, false"
    })
    void deveRespeitarTabelaDeTransicoes(StatusEvento origem, StatusEvento destino, boolean permitido) {
        assertThat(origem.podeTransicionarPara(destino)).isEqualTo(permitido);
    }

    @Test
    void realizadoECanceladoSaoFinais() {
        assertThat(REALIZADO.isFinal()).isTrue();
        assertThat(CANCELADO.isFinal()).isTrue();
        assertThat(ABERTO.isFinal()).isFalse();
    }

    @Test
    void apenasRascunhoEAbertoPermitemOrganizacao() {
        assertThat(RASCUNHO.permiteOrganizacao()).isTrue();
        assertThat(ABERTO.permiteOrganizacao()).isTrue();
        assertThat(ENCERRADO.permiteOrganizacao()).isFalse();
        assertThat(REALIZADO.permiteOrganizacao()).isFalse();
        assertThat(CANCELADO.permiteOrganizacao()).isFalse();
    }
}
