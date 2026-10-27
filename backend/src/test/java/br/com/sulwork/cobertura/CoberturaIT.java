package br.com.sulwork.cobertura;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criar;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.criarParticipante;
import static br.com.sulwork.support.Api.inscrever;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

/** Valida a consulta SQL nativa de cobertura contra o PostgreSQL real. */
class CoberturaIT extends IntegrationTest {

    @Test
    void deveCalcularCoberturaAPartirDaConsultaNativa() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long bebidas = criarCategoria(mockMvc, evento, "Bebidas", 6);
        long salgados = criarCategoria(mockMvc, evento, "Salgados", null);
        criarCategoria(mockMvc, evento, "Vazia", null);
        long suco = criarItem(mockMvc, evento, bebidas, "Suco", 4, true);
        long cafe = criarItem(mockMvc, evento, bebidas, "Café", 4, false);
        long pao = criarItem(mockMvc, evento, salgados, "Pão", 10, true);
        alterarStatus(mockMvc, evento, "ABERTO");

        long ana = criarParticipante(mockMvc, "Ana", "52998224725");
        long bruno = criarParticipante(mockMvc, "Bruno", "11144477735");
        inscrever(mockMvc, evento, ana);
        inscrever(mockMvc, evento, bruno);
        String url = "/api/eventos/" + evento + "/contribuicoes";
        criar(mockMvc, url, "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 4}".formatted(ana, suco));
        criar(mockMvc, url, "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 1}".formatted(bruno, cafe));
        criar(mockMvc, url, "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 5}".formatted(ana, pao));
        criar(mockMvc, url, "{\"participanteId\": %d, \"itemId\": %d, \"quantidade\": 5}".formatted(bruno, pao));

        // Bebidas: meta 6, contribuído 5 => 83%. Salgados: Pão 10/10 => 100%. Geral: (5/6 + 1) / 2 = 91%
        mockMvc.perform(get("/api/eventos/" + evento + "/cobertura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentualGeral").value(91))
                .andExpect(jsonPath("$.obrigatoriosTotal").value(2))
                .andExpect(jsonPath("$.obrigatoriosCompletos").value(2))
                .andExpect(jsonPath("$.metasTotal").value(1))
                .andExpect(jsonPath("$.metasAtingidas").value(0))
                .andExpect(jsonPath("$.pronto").value(false))
                .andExpect(jsonPath("$.categorias", hasSize(3)))
                .andExpect(jsonPath("$.categorias[0].nome").value("Bebidas"))
                .andExpect(jsonPath("$.categorias[0].criterio").value("META"))
                .andExpect(jsonPath("$.categorias[0].totalContribuido").value(5))
                .andExpect(jsonPath("$.categorias[0].percentual").value(83))
                .andExpect(jsonPath("$.categorias[0].itens", hasSize(2)))
                .andExpect(jsonPath("$.categorias[1].percentual").value(100))
                .andExpect(jsonPath("$.categorias[2].criterio").value("IGNORADA"));
    }
}
