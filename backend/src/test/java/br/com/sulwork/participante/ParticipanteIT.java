package br.com.sulwork.participante;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarEventoAberto;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static br.com.sulwork.support.Api.postJson;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ParticipanteIT extends IntegrationTest {

    @Test
    void deveCadastrarParticipanteNormalizandoCpf() throws Exception {
        postJson(mockMvc, "/api/participantes", """
                {"nome": "Ana Souza", "cpf": "529.982.247-25", "restricoes": ["LACTOSE", "GLUTEN"],
                 "observacoesAlimentares": "Vegetariana"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpfMascarado").value("***.982.247-**"))
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.restricoes", hasSize(2)));
    }

    @Test
    void naoDeveExporOCpfCompletoEmNenhumaListagem() throws Exception {
        long evento = criarEventoAberto(mockMvc, "Café");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        inscrever(mockMvc, evento, ana);

        mockMvc.perform(get("/api/participantes"))
                .andExpect(jsonPath("$[0].cpfMascarado").value("***.982.247-**"))
                .andExpect(content().string(not(containsString("52998224725"))));
        mockMvc.perform(get("/api/eventos/" + evento + "/inscricoes"))
                .andExpect(content().string(not(containsString("52998224725"))));
    }

    @Test
    void buscaPorCpfCompletoContinuaFuncionando() throws Exception {
        criarParticipante(mockMvc, "Ana", "52998224725");
        criarParticipante(mockMvc, "Bruno", "11144477735");

        mockMvc.perform(get("/api/participantes").param("busca", "529.982.247-25"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Ana"));
    }

    @Test
    void buscaNaoAceitaPedacosDoCpf() throws Exception {
        criarParticipante(mockMvc, "Ana", "52998224725");

        // Os dígitos do meio aparecem na máscara; aceitá-los na busca permitiria descobrir o resto.
        for (String pedaco : new String[] {"982247", "529982247", "5299822472"}) {
            mockMvc.perform(get("/api/participantes").param("busca", pedaco))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
        mockMvc.perform(get("/api/participantes").param("busca", "ana"))
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deveAtualizarSemInformarOCpf() throws Exception {
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        mockMvc.perform(put("/api/participantes/" + ana).contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome": "Ana Souza", "restricoes": ["LACTOSE"], "observacoesAlimentares": "Vegetariana"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Souza"))
                .andExpect(jsonPath("$.restricoes[0]").value("LACTOSE"))
                .andExpect(jsonPath("$.cpfMascarado").value("***.982.247-**"));
    }

    @Test
    void deveRejeitarCpfInvalido() throws Exception {
        postJson(mockMvc, "/api/participantes", """
                {"nome": "Ana", "cpf": "123.456.789-00"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("cpf"));
    }

    @Test
    void deveRejeitarCpfDuplicado() throws Exception {
        criarParticipante(mockMvc, "Ana", "52998224725");
        postJson(mockMvc, "/api/participantes", """
                {"nome": "Outra Ana", "cpf": "529.982.247-25"}
                """)
                .andExpect(status().isConflict());
    }

    @Test
    void naoDeveInscreverDuasVezesNoMesmoEvento() throws Exception {
        long evento = criarEventoAberto(mockMvc, "Café");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        inscrever(mockMvc, evento, ana);
        postJson(mockMvc, "/api/eventos/" + evento + "/inscricoes", "{\"participanteId\": " + ana + "}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("já está inscrito")));
    }

    @Test
    void mesmoParticipantePodeParticiparDeVariosEventos() throws Exception {
        long cafe = criarEventoAberto(mockMvc, "Café");
        long almoco = criarEventoAberto(mockMvc, "Almoço");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        inscrever(mockMvc, cafe, ana);
        inscrever(mockMvc, almoco, ana);

        mockMvc.perform(get("/api/eventos/" + almoco + "/inscricoes"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].participante.nome").value("Ana"));
    }

    @Test
    void naoDeveInscreverEmEventoEmRascunhoOuAposOPrazo() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Geral", null);
        criarItem(mockMvc, evento, categoria, "Pão", 10, true);
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        postJson(mockMvc, "/api/eventos/" + evento + "/inscricoes", "{\"participanteId\": " + ana + "}")
                .andExpect(status().isUnprocessableEntity());

        alterarStatus(mockMvc, evento, "ABERTO");
        relogio.avancar(Duration.ofDays(9));
        postJson(mockMvc, "/api/eventos/" + evento + "/inscricoes", "{\"participanteId\": " + ana + "}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem", containsString("prazo")));
    }

    @Test
    void naoDeveExcluirParticipanteInscrito() throws Exception {
        long evento = criarEventoAberto(mockMvc, "Café");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        inscrever(mockMvc, evento, ana);

        mockMvc.perform(delete("/api/participantes/" + ana)).andExpect(status().isConflict());
    }
}
