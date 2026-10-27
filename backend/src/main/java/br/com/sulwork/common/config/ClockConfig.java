package br.com.sulwork.common.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expõe o relógio da aplicação como bean. Toda regra que depende de "agora"
 * (prazo para alterações, realização do evento) usa este Clock, o que permite
 * controlar o tempo nos testes.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${sulwork.timezone:America/Sao_Paulo}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
