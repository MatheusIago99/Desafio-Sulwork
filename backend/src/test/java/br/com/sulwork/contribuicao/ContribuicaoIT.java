package br.com.sulwork.contribuicao;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criar;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static br.com.sulwork.support.Api.postJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ContribuicaoIT extends IntegrationTest {

    private long evento;
    private long suco;
    private long ana;
    private long bruno;

    @BeforeEach
    void cenario() throws Exception {
        evento = criarEvento(mockMvc, "Café");
        long bebidas = criarCategoria(mockMvc, evento, "Bebidas", null);
        suco = criarItem(mockMvc, evento, bebidas, "Suco", 10, true);
        alterarStatus(mockMvc, evento, "ABERTO");
        ana = criarParticipante(mockMvc, "Ana", "52998224725");
        bruno = criarParticipante(mockMvc, "Bruno", "11144477735");
        inscrever(mockMvc, evento, ana);
        inscrever(mockMvc, evento, bruno);
    }

    private String url() {
        return "/api/eventos/" + evento + "/contribuicoes";
    }

    private String corpo(long participante, long item, int quantidade) {
        return """
                {"participanteId": %d, "itemId": %d, "quantidade": %d}
                """.formatted(participante, item, quantidade);
    }

    @Test
    void variasPessoasPodemContribuirComOMesmoItemAteOLimite() throws Exception {
        criar(mockMvc, url(), corpo(ana, suco, 6));
        criar(mockMvc, url(), corpo(bruno, suco, 4));

        mockMvc.perform(get("/api/eventos/" + evento + "/itens"))
                .andExpect(jsonPath("$[0].quantidadeContribuida").value(10))
                .andExpect(jsonPath("$[0].quantidadeFaltante").value(0));
        mockMvc.perform(get(url())).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void naoDeveUltrapassarAQuantidadeNecessaria() throws Exception {
        criar(mockMvc, url(), corpo(ana, suco, 6));

        postJson(mockMvc, url(), corpo(bruno, suco, 5))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("restam 4 de 10")));
    }

    @Test
    void deveAlterarERemoverContribuicaoDentroDoPrazo() throws Exception {
        long id = criar(mockMvc, url(), corpo(ana, suco, 6));

        mockMvc.perform(put(url() + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\": 8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(8));
        mockMvc.perform(delete(url() + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(url())).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void naoDevePermitirAlteracoesAposOPrazo() throws Exception {
        long id = criar(mockMvc, url(), corpo(ana, suco, 6));
        relogio.avancar(Duration.ofDays(9));

        postJson(mockMvc, url(), corpo(bruno, suco, 1)).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(put(url() + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\": 1}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(delete(url() + "/" + id)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoDevePermitirAlteracoesComEventoEncerradoMesmoDentroDoPrazo() throws Exception {
        long id = criar(mockMvc, url(), corpo(ana, suco, 6));
        alterarStatus(mockMvc, evento, "ENCERRADO");

        postJson(mockMvc, url(), corpo(bruno, suco, 1))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("ENCERRADO")));
        mockMvc.perform(delete(url() + "/" + id)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoDeveReduzirQuantidadeNecessariaAbaixoDoContribuido() throws Exception {
        criar(mockMvc, url(), corpo(ana, suco, 6));
        long categoria = criarCategoria(mockMvc, evento, "Outros", null);

        mockMvc.perform(put("/api/eventos/" + evento + "/itens/" + suco).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Suco", "categoriaId": %d, "quantidadeNecessaria": 5, "obrigatorio": true}
                                """.formatted(categoria)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelarInscricaoRemoveAsContribuicoesDoParticipante() throws Exception {
        criar(mockMvc, url(), corpo(ana, suco, 6));
        criar(mockMvc, url(), corpo(bruno, suco, 2));

        mockMvc.perform(delete("/api/eventos/" + evento + "/inscricoes/" + ana)).andExpect(status().isNoContent());

        mockMvc.perform(get(url()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].participanteNome").value("Bruno"));
    }

    /**
     * Duas requisições simultâneas que, somadas, estourariam o limite: o lock pessimista
     * no item garante que apenas uma seja aceita.
     */
    @Test
    void contribuicoesConcorrentesNaoDevemEstourarOLimite() throws Exception {
        int participantes = 4;
        List<Long> ids = new ArrayList<>();
        String[] cpfs = {"39053344705", "86288366757", "71428793860", "15350946056"};
        for (int i = 0; i < participantes; i++) {
            long id = criarParticipante(mockMvc, "Pessoa " + i, cpfs[i]);
            inscrever(mockMvc, evento, id);
            ids.add(id);
        }

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(participantes);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (long participante : ids) {
            Callable<Integer> tarefa = () -> {
                largada.await();
                return postJson(mockMvc, url(), corpo(participante, suco, 4)).andReturn().getResponse().getStatus();
            };
            resultados.add(executor.submit(tarefa));
        }
        largada.countDown();

        int aceitas = 0;
        for (Future<Integer> resultado : resultados) {
            if (resultado.get() == 201) {
                aceitas++;
            }
        }
        executor.shutdown();

        assertThat(aceitas).isEqualTo(2);
        mockMvc.perform(get("/api/eventos/" + evento + "/itens"))
                .andExpect(jsonPath("$[0].quantidadeContribuida").value(8));
    }
}
