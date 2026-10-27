package br.com.sulwork.evento;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.patchJson;
import static br.com.sulwork.support.Api.postJson;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class EventoIT extends IntegrationTest {

    @Test
    void deveCriarEventoEmRascunho() throws Exception {
        long id = criarEvento(mockMvc, "Café da manhã");

        mockMvc.perform(get("/api/eventos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.aceitaAlteracoes").value(false))
                .andExpect(jsonPath("$.transicoesPermitidas", containsInAnyOrder("ABERTO", "CANCELADO")));
    }

    @Test
    void deveValidarCamposObrigatorios() throws Exception {
        postJson(mockMvc, "/api/eventos", "{\"nome\": \"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(4)));
    }

    @Test
    void deveRejeitarPrazoPosteriorAoEvento() throws Exception {
        postJson(mockMvc, "/api/eventos", """
                {"nome": "Café", "tipo": "CAFE_DA_MANHA",
                 "dataHora": "2026-03-20T09:00:00", "prazoAlteracoes": "2026-03-21T09:00:00"}
                """)
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoDeveAbrirEventoSemItens() throws Exception {
        long id = criarEvento(mockMvc, "Café");

        patchJson(mockMvc, "/api/eventos/" + id + "/status", "{\"status\": \"ABERTO\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("ao menos um item")));
    }

    @Test
    void devePercorrerCicloDeVidaCompleto() throws Exception {
        long id = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, id, "Bebidas", null);
        criarItem(mockMvc, id, categoria, "Suco", 3, true);

        alterarStatus(mockMvc, id, "ABERTO");
        alterarStatus(mockMvc, id, "ENCERRADO");
        relogio.avancar(Duration.ofDays(10));
        alterarStatus(mockMvc, id, "REALIZADO");

        mockMvc.perform(get("/api/eventos/" + id))
                .andExpect(jsonPath("$.status").value("REALIZADO"))
                .andExpect(jsonPath("$.transicoesPermitidas", hasSize(0)));
    }

    @Test
    void deveRejeitarTransicaoInvalida() throws Exception {
        long id = criarEvento(mockMvc, "Café");

        patchJson(mockMvc, "/api/eventos/" + id + "/status", "{\"status\": \"REALIZADO\"}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("RASCUNHO -> REALIZADO")));
    }

    @Test
    void deveFiltrarPorStatus() throws Exception {
        criarEvento(mockMvc, "Rascunho");
        long cancelado = criarEvento(mockMvc, "Cancelado");
        alterarStatus(mockMvc, cancelado, "CANCELADO");

        mockMvc.perform(get("/api/eventos").param("status", "CANCELADO"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Cancelado"));
    }

    @Test
    void soDeveExcluirRascunho() throws Exception {
        long rascunho = criarEvento(mockMvc, "Rascunho");
        long cancelado = criarEvento(mockMvc, "Cancelado");
        alterarStatus(mockMvc, cancelado, "CANCELADO");

        mockMvc.perform(delete("/api/eventos/" + rascunho)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/eventos/" + cancelado)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar404ParaEventoInexistente() throws Exception {
        mockMvc.perform(get("/api/eventos/999"))
                .andExpect(status().isNotFound());
    }
}
