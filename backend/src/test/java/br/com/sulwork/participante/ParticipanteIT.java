package br.com.sulwork.participante;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static br.com.sulwork.support.Api.postJson;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class ParticipanteIT extends IntegrationTest {

    @Test
    void deveCadastrarParticipanteNormalizandoCpf() throws Exception {
        postJson(mockMvc, "/api/participantes", """
                {"nome": "Ana Souza", "cpf": "529.982.247-25", "restricoes": ["LACTOSE", "GLUTEN"],
                 "observacoesAlimentares": "Vegetariana"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.restricoes", hasSize(2)));
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
        long evento = criarEvento(mockMvc, "Café");
        alterarStatus(mockMvc, evento, "ABERTO");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");

        inscrever(mockMvc, evento, ana);
        postJson(mockMvc, "/api/eventos/" + evento + "/inscricoes", "{\"participanteId\": " + ana + "}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("já está inscrito")));
    }

    @Test
    void mesmoParticipantePodeParticiparDeVariosEventos() throws Exception {
        long cafe = criarEvento(mockMvc, "Café");
        long almoco = criarEvento(mockMvc, "Almoço");
        alterarStatus(mockMvc, cafe, "ABERTO");
        alterarStatus(mockMvc, almoco, "ABERTO");
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
        long evento = criarEvento(mockMvc, "Café");
        alterarStatus(mockMvc, evento, "ABERTO");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        inscrever(mockMvc, evento, ana);

        mockMvc.perform(delete("/api/participantes/" + ana)).andExpect(status().isConflict());
    }
}
