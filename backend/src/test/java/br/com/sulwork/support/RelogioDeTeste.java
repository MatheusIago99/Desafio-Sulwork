package br.com.sulwork.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Clock ajustável para os testes de integração simularem a passagem do tempo. */
public class RelogioDeTeste extends Clock {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Instant instante;

    public RelogioDeTeste(LocalDateTime inicio) {
        definir(inicio);
    }

    public void definir(LocalDateTime agora) {
        this.instante = agora.atZone(ZONA).toInstant();
    }

    public void avancar(Duration duracao) {
        this.instante = instante.plus(duracao);
    }

    public LocalDateTime agora() {
        return LocalDateTime.ofInstant(instante, ZONA);
    }

    @Override
    public ZoneId getZone() {
        return ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instante, zone);
    }

    @Override
    public Instant instant() {
        return instante;
    }
}
