package br.com.sulwork.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Atalhos para montar cenários nos testes de integração via API REST. */
public final class Api {

    private Api() {
    }

    public static ResultActions postJson(MockMvc mvc, String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    public static ResultActions patchJson(MockMvc mvc, String url, String json) throws Exception {
        return mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    public static long criar(MockMvc mvc, String url, String json) throws Exception {
        String corpo = postJson(mvc, url, json)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    /** Evento com data em 10 dias e prazo em 8 dias a partir de {@link IntegrationTest#AGORA}. */
    public static long criarEvento(MockMvc mvc, String nome) throws Exception {
        return criar(mvc, "/api/eventos", """
                {"nome": "%s", "descricao": "Evento de teste", "tipo": "CAFE_DA_MANHA",
                 "dataHora": "2026-03-20T09:00:00", "prazoAlteracoes": "2026-03-18T18:00:00"}
                """.formatted(nome));
    }

    public static void alterarStatus(MockMvc mvc, long eventoId, String novoStatus) throws Exception {
        patchJson(mvc, "/api/eventos/" + eventoId + "/status", "{\"status\": \"" + novoStatus + "\"}")
                .andExpect(status().isOk());
    }

    public static long criarParticipante(MockMvc mvc, String nome, String cpf, String... restricoes) throws Exception {
        String lista = restricoes.length == 0 ? "" : "\"" + String.join("\",\"", restricoes) + "\"";
        return criar(mvc, "/api/participantes", """
                {"nome": "%s", "cpf": "%s", "restricoes": [%s]}
                """.formatted(nome, cpf, lista));
    }

    public static void inscrever(MockMvc mvc, long eventoId, long participanteId) throws Exception {
        postJson(mvc, "/api/eventos/" + eventoId + "/inscricoes", "{\"participanteId\": " + participanteId + "}")
                .andExpect(status().isCreated());
    }
}
