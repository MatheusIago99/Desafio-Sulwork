package br.com.sulwork.support;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes de integração: sobe a aplicação inteira contra um PostgreSQL real
 * (Testcontainers), com as migrations do Flyway, e um relógio controlável.
 * O container é compartilhado entre as classes de teste para acelerar a suíte.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTest.Config.class)
public abstract class IntegrationTest {

    public static final LocalDateTime AGORA = LocalDateTime.of(2026, 3, 10, 9, 0);

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RelogioDeTeste relogio;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparBancoEReiniciarRelogio() {
        relogio.definir(AGORA);
        jdbcTemplate.execute("""
                TRUNCATE TABLE evento, participante RESTART IDENTITY CASCADE
                """);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Config {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return POSTGRES;
        }

        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste(AGORA);
        }
    }
}
