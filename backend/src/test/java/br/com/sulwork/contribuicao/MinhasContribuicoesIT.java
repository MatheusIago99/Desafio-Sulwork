package br.com.sulwork.contribuicao;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criar;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import br.com.sulwork.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

class MinhasContribuicoesIT extends IntegrationTest {

    private long eventoAberto(String nome, String item, int quantidade) throws Exception {
        long evento = criarEvento(mockMvc, nome);
        long categoria = criarCategoria(mockMvc, evento, "Geral", null);
        criarItem(mockMvc, evento, categoria, item, quantidade, true);
        alterarStatus(mockMvc, evento, "ABERTO");
        return evento;
    }

    private long primeiroItem(long evento) throws Exception {
        String corpo = mockMvc.perform(get("/api/eventos/" + evento + "/itens")).andReturn().getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(corpo, "$[0].id")).longValue();
    }

    private void contribuir(long evento, long participante, long item, int quantidade) throws Exception {
        criar(mockMvc, "/api/eventos/" + evento + "/contribuicoes",
                "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": %d}".formatted(participante, item, quantidade));
    }

    @Test
    void deveListarAsContribuicoesDoParticipanteEmTodosOsEventos() throws Exception {
        long cafe = eventoAberto("Café", "Pão", 10);
        long almoco = eventoAberto("Almoço", "Arroz", 4);
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        long bruno = criarParticipante(mockMvc, "Bruno", "11144477735");
        for (long evento : new long[] {cafe, almoco}) {
            inscrever(mockMvc, evento, ana);
        }
        inscrever(mockMvc, cafe, bruno);
        long pao = primeiroItem(cafe);
        contribuir(cafe, ana, pao, 3);
        contribuir(cafe, bruno, pao, 5);
        contribuir(almoco, ana, primeiroItem(almoco), 2);

        mockMvc.perform(get("/api/participantes/" + ana + "/contribuicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].itemNome", contains("Pão", "Arroz")))
                .andExpect(jsonPath("$[0].eventoNome").value("Café"))
                .andExpect(jsonPath("$[0].quantidade").value(3))
                .andExpect(jsonPath("$[0].quantidadeNecessaria").value(10))
                // total do item soma as contribuições de todos, não só as da Ana
                .andExpect(jsonPath("$[0].quantidadeContribuidaTotal").value(8))
                .andExpect(jsonPath("$[0].aceitaAlteracoes").value(true));
    }

    @Test
    void deveIndicarQueNaoAceitaMaisAlteracoesAposOPrazo() throws Exception {
        long cafe = eventoAberto("Café", "Pão", 10);
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        inscrever(mockMvc, cafe, ana);
        contribuir(cafe, ana, primeiroItem(cafe), 3);
        relogio.avancar(Duration.ofDays(9));

        mockMvc.perform(get("/api/participantes/" + ana + "/contribuicoes"))
                .andExpect(jsonPath("$[0].prazoVencido").value(true))
                .andExpect(jsonPath("$[0].aceitaAlteracoes").value(false));
    }

    @Test
    void participanteSemContribuicoesRetornaListaVazia() throws Exception {
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        mockMvc.perform(get("/api/participantes/" + ana + "/contribuicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void participanteInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/participantes/999/contribuicoes")).andExpect(status().isNotFound());
    }
}
