package br.com.sulwork.alergeno;

import static br.com.sulwork.support.Api.alterarStatus;
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

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class RestricoesIT extends IntegrationTest {

    @Test
    void deveCruzarRestricoesDosInscritosComAlergenosDosItens() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Comidas", null);
        criarItem(mockMvc, evento, categoria, "Bolo de cenoura", 2, true, "GLUTEN", "OVO", "LACTOSE");
        criarItem(mockMvc, evento, categoria, "Frutas", 2, true);
        alterarStatus(mockMvc, evento, "ABERTO");
        long ana = criarParticipante(mockMvc, "Ana", "52998224725", "LACTOSE");
        long bruno = criarParticipante(mockMvc, "Bruno", "11144477735");
        criarParticipante(mockMvc, "Carla (não inscrita)", "39053344705", "OVO");
        inscrever(mockMvc, evento, ana);
        inscrever(mockMvc, evento, bruno);

        mockMvc.perform(get("/api/eventos/" + evento + "/restricoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInscritos").value(2))
                .andExpect(jsonPath("$.restricoes", hasSize(1)))
                .andExpect(jsonPath("$.restricoes[0].descricao").value("Lactose"))
                .andExpect(jsonPath("$.itens[0].itemNome").value("Bolo de cenoura"))
                .andExpect(jsonPath("$.itens[0].seguroParaTodos").value(false))
                .andExpect(jsonPath("$.itens[0].conflitos[0].participantes", contains("Ana")))
                .andExpect(jsonPath("$.itens[1].seguroParaTodos").value(true));
    }

    @Test
    void deveListarAlergenosDisponiveis() throws Exception {
        mockMvc.perform(get("/api/alergenos"))
                .andExpect(jsonPath("$[0].codigo").value("GLUTEN"))
                .andExpect(jsonPath("$[0].descricao").value("Glúten"));
    }
}
