package br.com.sulwork.dashboard;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criar;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class DashboardIT extends IntegrationTest {

    @Test
    void semDadosRetornaZerosSemInventarCobertura() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coberturaMedia").value(nullValue()))
                .andExpect(jsonPath("$.proximosEventos", hasSize(0)))
                .andExpect(jsonPath("$.contribuicoesTotal").value(0))
                .andExpect(jsonPath("$.ultimaContribuicaoEm").value(nullValue()))
                .andExpect(jsonPath("$.pendenciasPrioritarias", hasSize(0)))
                .andExpect(jsonPath("$.contribuicoesRecentes", hasSize(0)));
    }

    @Test
    void deveAgregarIndicadoresDosEventos() throws Exception {
        long cafe = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, cafe, "Comidas", null);
        long pao = criarItem(mockMvc, cafe, categoria, "Pão", 10, true);
        criarItem(mockMvc, cafe, categoria, "Bolo", 2, true);
        alterarStatus(mockMvc, cafe, "ABERTO");
        criarEvento(mockMvc, "Rascunho");
        long cancelado = criarEvento(mockMvc, "Cancelado");
        alterarStatus(mockMvc, cancelado, "CANCELADO");

        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        criarParticipante(mockMvc, "Bruno", "11144477735");
        inscrever(mockMvc, cafe, ana);
        criar(mockMvc, "/api/eventos/" + cafe + "/contribuicoes",
                "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 10}".formatted(ana, pao));

        // Pão 100% (peso 2) + Bolo 0% (peso 2) => 50%; Bolo continua pendente
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(jsonPath("$.coberturaMedia").value(50))
                .andExpect(jsonPath("$.obrigatoriosPendentes").value(1))
                // cancelado fica de fora dos próximos
                .andExpect(jsonPath("$.proximosEventos", hasSize(2)))
                .andExpect(jsonPath("$.proximosEventos[?(@.evento.nome == 'Café')].inscritos").value(1))
                .andExpect(jsonPath("$.proximosEventos[?(@.evento.nome == 'Café')].percentualCobertura").value(50))
                // o cancelado não conta; café (aberto) e rascunho estão a 10 dias, fora da semana
                .andExpect(jsonPath("$.proximosEventosTotal").value(2))
                .andExpect(jsonPath("$.proximosEventosNaSemana").value(0))
                .andExpect(jsonPath("$.contribuicoesTotal").value(1))
                .andExpect(jsonPath("$.ultimaContribuicaoEm").value("2026-03-10T09:00:00"))
                // só o Bolo (obrigatório) continua pendente
                .andExpect(jsonPath("$.pendenciasPrioritarias", hasSize(1)))
                .andExpect(jsonPath("$.pendenciasPrioritarias[0].itemNome").value("Bolo"))
                .andExpect(jsonPath("$.pendenciasPrioritarias[0].eventoNome").value("Café"))
                .andExpect(jsonPath("$.pendenciasPrioritarias[0].quantidadeFaltante").value(2))
                .andExpect(jsonPath("$.contribuicoesRecentes", hasSize(1)))
                .andExpect(jsonPath("$.contribuicoesRecentes[0].participanteNome").value("Ana"))
                .andExpect(jsonPath("$.contribuicoesRecentes[0].itemNome").value("Pão"))
                .andExpect(jsonPath("$.contribuicoesRecentes[0].quantidade").value(10));
    }

    @Test
    void pendenciasPrioritariasIgnoramEventosForaDoPrazo() throws Exception {
        long cafe = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, cafe, "Comidas", null);
        criarItem(mockMvc, cafe, categoria, "Pão", 10, true);
        alterarStatus(mockMvc, cafe, "ABERTO");
        relogio.avancar(Duration.ofDays(9)); // prazo vencido, evento ainda ABERTO

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(jsonPath("$.obrigatoriosPendentes").value(1))
                .andExpect(jsonPath("$.pendenciasPrioritarias", hasSize(0)));
    }

    @Test
    void eventosPassadosNaoAparecemEmProximos() throws Exception {
        criarEvento(mockMvc, "Café");
        relogio.avancar(Duration.ofDays(11));

        mockMvc.perform(get("/api/dashboard")).andExpect(jsonPath("$.proximosEventos", hasSize(0)));
    }
}
