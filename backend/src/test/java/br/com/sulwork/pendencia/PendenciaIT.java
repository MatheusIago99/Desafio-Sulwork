package br.com.sulwork.pendencia;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criar;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static br.com.sulwork.support.Api.postJson;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PendenciaIT extends IntegrationTest {

    private long evento;
    private long pao;
    private long suco;
    private long guardanapo;
    private long bolo;
    private long ana;
    private long bruno;

    @BeforeEach
    void cenario() throws Exception {
        evento = criarEvento(mockMvc, "Café");
        long comidas = criarCategoria(mockMvc, evento, "Comidas", null);
        long bebidas = criarCategoria(mockMvc, evento, "Bebidas", 4);
        long extras = criarCategoria(mockMvc, evento, "Extras", null);
        pao = criarItem(mockMvc, evento, comidas, "Pão", 10, true, "GLUTEN");
        bolo = criarItem(mockMvc, evento, comidas, "Bolo", 2, true, "LACTOSE");
        suco = criarItem(mockMvc, evento, bebidas, "Suco", 4, false);
        guardanapo = criarItem(mockMvc, evento, extras, "Guardanapo", 1, false);
        alterarStatus(mockMvc, evento, "ABERTO");
        ana = criarParticipante(mockMvc, "Ana", "52998224725", "LACTOSE");
        bruno = criarParticipante(mockMvc, "Bruno", "11144477735");
        inscrever(mockMvc, evento, ana);
        inscrever(mockMvc, evento, bruno);
    }

    private String assumir(long item) {
        return "/api/eventos/" + evento + "/pendencias/" + item + "/assumir";
    }

    @Test
    void deveListarPendentesPriorizandoObrigatoriosEMenosCobertos() throws Exception {
        criar(mockMvc, "/api/eventos/" + evento + "/contribuicoes",
                "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 5}".formatted(bruno, pao));

        mockMvc.perform(get("/api/eventos/" + evento + "/pendencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceitaContribuicoes").value(true))
                .andExpect(jsonPath("$.totalPendentes").value(4))
                .andExpect(jsonPath("$.obrigatoriosPendentes").value(2))
                // Bolo (ALTA, 0%), Pão (ALTA, 50%), Suco (MEDIA: meta de Bebidas pendente), Guardanapo (BAIXA)
                .andExpect(jsonPath("$.itens[*].nome", contains("Bolo", "Pão", "Suco", "Guardanapo")))
                .andExpect(jsonPath("$.itens[*].prioridade", contains("ALTA", "ALTA", "MEDIA", "BAIXA")))
                .andExpect(jsonPath("$.itens[1].quantidadeFaltante").value(5))
                .andExpect(jsonPath("$.itens[0].seguroParaTodos").value(false))
                .andExpect(jsonPath("$.itens[0].conflitos[0].participantes", contains("Ana")));
    }

    @Test
    void euLevoSemQuantidadeAssumeTudoOQueFaltaERemoveDaLista() throws Exception {
        postJson(mockMvc, assumir(bolo), "{\"participanteId\": " + bruno + "}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(2));

        mockMvc.perform(get("/api/eventos/" + evento + "/pendencias"))
                .andExpect(jsonPath("$.itens[*].nome", contains("Pão", "Suco", "Guardanapo")));
    }

    @Test
    void euLevoSomaAContribuicaoExistenteDoParticipante() throws Exception {
        postJson(mockMvc, assumir(pao), "{\"participanteId\": %d, \"quantidade\": 3}".formatted(ana))
                .andExpect(status().isOk());
        postJson(mockMvc, assumir(pao), "{\"participanteId\": %d, \"quantidade\": 2}".formatted(ana))
                .andExpect(jsonPath("$.quantidade").value(5));

        mockMvc.perform(get("/api/eventos/" + evento + "/pendencias").param("participanteId", String.valueOf(ana)))
                .andExpect(jsonPath("$.itens[?(@.nome == 'Pão')].minhaContribuicao", contains(5)));
        mockMvc.perform(get("/api/eventos/" + evento + "/contribuicoes")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void euLevoRespeitaLimiteEItemCompleto() throws Exception {
        postJson(mockMvc, assumir(guardanapo), "{\"participanteId\": %d, \"quantidade\": 2}".formatted(ana))
                .andExpect(status().isUnprocessableEntity());

        postJson(mockMvc, assumir(guardanapo), "{\"participanteId\": " + ana + "}").andExpect(status().isOk());
        postJson(mockMvc, assumir(guardanapo), "{\"participanteId\": " + bruno + "}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("já está completo")));
    }

    @Test
    void euLevoRespeitaAJanelaDeAlteracoes() throws Exception {
        alterarStatus(mockMvc, evento, "ENCERRADO");

        postJson(mockMvc, assumir(suco), "{\"participanteId\": " + ana + "}")
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/eventos/" + evento + "/pendencias"))
                .andExpect(jsonPath("$.aceitaContribuicoes").value(false));
    }
}
