package br.com.sulwork.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import br.com.sulwork.common.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

class EventoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 3, 10, 9, 0);
    private static final LocalDateTime DATA_EVENTO = AGORA.plusDays(10);
    private static final LocalDateTime PRAZO = AGORA.plusDays(8);

    private Evento novoEvento() {
        return new Evento("Café da manhã", "Café do time", TipoEvento.CAFE_DA_MANHA, DATA_EVENTO, PRAZO, AGORA);
    }

    @Test
    void deveNascerEmRascunho() {
        assertThat(novoEvento().getStatus()).isEqualTo(StatusEvento.RASCUNHO);
    }

    @Test
    void naoDeveAceitarPrazoDepoisDaDataDoEvento() {
        assertThatThrownBy(() -> new Evento("X", null, TipoEvento.OUTRO, DATA_EVENTO, DATA_EVENTO.plusHours(1), AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prazo");
    }

    @Test
    void naoDeveAceitarEventoNoPassado() {
        assertThatThrownBy(() -> new Evento("X", null, TipoEvento.OUTRO, AGORA.minusDays(1), AGORA.minusDays(2), AGORA))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void aceitaAlteracoesSomenteQuandoAbertoEDentroDoPrazo() {
        Evento evento = novoEvento();
        assertThat(evento.aceitaAlteracoesDeParticipantes(AGORA)).isFalse();

        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        assertThat(evento.aceitaAlteracoesDeParticipantes(AGORA)).isTrue();
        assertThat(evento.aceitaAlteracoesDeParticipantes(PRAZO.minusMinutes(1))).isTrue();
        assertThat(evento.aceitaAlteracoesDeParticipantes(PRAZO)).isFalse();
    }

    @Test
    void naoAceitaAlteracoesQuandoEncerradoMesmoDentroDoPrazo() {
        Evento evento = novoEvento();
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        evento.transicionarPara(StatusEvento.ENCERRADO, AGORA);

        assertThat(evento.aceitaAlteracoesDeParticipantes(AGORA)).isFalse();
        assertThatThrownBy(() -> evento.garantirJanelaDeAlteracoes(AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não está aberto");
    }

    @Test
    void garantirJanelaDeveFalharAposOPrazo() {
        Evento evento = novoEvento();
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);

        assertThatThrownBy(() -> evento.garantirJanelaDeAlteracoes(PRAZO.plusMinutes(1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prazo");
    }

    @Test
    void naoDevePermitirTransicaoForaDaTabela() {
        Evento evento = novoEvento();
        assertThatThrownBy(() -> evento.transicionarPara(StatusEvento.REALIZADO, AGORA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("RASCUNHO -> REALIZADO");
    }

    @Test
    void naoDeveAbrirComPrazoVencido() {
        Evento evento = novoEvento();
        assertThatThrownBy(() -> evento.transicionarPara(StatusEvento.ABERTO, PRAZO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prazo");
    }

    @Test
    void naoDeveReabrirAposOPrazo() {
        Evento evento = novoEvento();
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        evento.transicionarPara(StatusEvento.ENCERRADO, AGORA);

        assertThatThrownBy(() -> evento.transicionarPara(StatusEvento.ABERTO, PRAZO.plusHours(1)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void soPodeSerRealizadoAPartirDaDataDoEvento() {
        Evento evento = novoEvento();
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        evento.transicionarPara(StatusEvento.ENCERRADO, AGORA);

        assertThatThrownBy(() -> evento.transicionarPara(StatusEvento.REALIZADO, DATA_EVENTO.minusMinutes(1)))
                .isInstanceOf(RegraNegocioException.class);

        evento.transicionarPara(StatusEvento.REALIZADO, DATA_EVENTO);
        assertThat(evento.getStatus()).isEqualTo(StatusEvento.REALIZADO);
    }

    @Test
    void naoDeveEditarEventoEncerrado() {
        Evento evento = novoEvento();
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        evento.transicionarPara(StatusEvento.ENCERRADO, AGORA);

        assertThatThrownBy(() -> evento.atualizar("Novo", null, TipoEvento.OUTRO, DATA_EVENTO, PRAZO, AGORA))
                .isInstanceOf(RegraNegocioException.class);
    }
}
