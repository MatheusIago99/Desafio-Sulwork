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
                .andExpect(jsonPath("$.eventosAbertos").value(0))
                .andExpect(jsonPath("$.coberturaMedia").value(nullValue()))
                .andExpect(jsonPath("$.eventosPorTipo", hasSize(6)))
                .andExpect(jsonPath("$.proximosEventos", hasSize(0)));
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
                .andExpect(jsonPath("$.eventosAbertos").value(1))
                .andExpect(jsonPath("$.eventosEmOrganizacao").value(1))
                .andExpect(jsonPath("$.participantesCadastrados").value(2))
                .andExpect(jsonPath("$.inscricoesEmEventosAbertos").value(1))
                .andExpect(jsonPath("$.coberturaMedia").value(50))
                .andExpect(jsonPath("$.obrigatoriosPendentes").value(1))
                .andExpect(jsonPath("$.eventosPorTipo[0].tipo").value("CAFE_DA_MANHA"))
                .andExpect(jsonPath("$.eventosPorTipo[0].quantidade").value(3))
                // cancelado fica de fora dos próximos
                .andExpect(jsonPath("$.proximosEventos", hasSize(2)))
                .andExpect(jsonPath("$.proximosEventos[?(@.evento.nome == 'Café')].inscritos").value(1))
                .andExpect(jsonPath("$.proximosEventos[?(@.evento.nome == 'Café')].percentualCobertura").value(50));
    }

    @Test
    void eventosPassadosNaoAparecemEmProximos() throws Exception {
        criarEvento(mockMvc, "Café");
        relogio.avancar(Duration.ofDays(11));

        mockMvc.perform(get("/api/dashboard")).andExpect(jsonPath("$.proximosEventos", hasSize(0)));
    }
}
