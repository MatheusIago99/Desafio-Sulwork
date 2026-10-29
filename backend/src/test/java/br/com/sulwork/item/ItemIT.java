package br.com.sulwork.item;

import static br.com.sulwork.support.Api.alterarStatus;
import static br.com.sulwork.support.Api.criarCategoria;
import static br.com.sulwork.support.Api.criarEvento;
import static br.com.sulwork.support.Api.criarItem;
import static br.com.sulwork.support.Api.postJson;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.sulwork.support.IntegrationTest;
import org.junit.jupiter.api.Test;

class ItemIT extends IntegrationTest {

    @Test
    void deveCriarItensOrganizadosPorCategoria() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long bebidas = criarCategoria(mockMvc, evento, "Bebidas", 10);
        long salgados = criarCategoria(mockMvc, evento, "Salgados", null);
        criarItem(mockMvc, evento, salgados, "Pão de queijo", 30, true, "LACTOSE", "OVO");
        criarItem(mockMvc, evento, bebidas, "Café", 2, true);

        mockMvc.perform(get("/api/eventos/" + evento + "/itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].categoria.nome").value("Bebidas"))
                .andExpect(jsonPath("$[0].categoria.meta").value(10))
                .andExpect(jsonPath("$[1].alergenos", contains("LACTOSE", "OVO")));
    }

    @Test
    void deveRejeitarCategoriaDuplicadaIgnorandoMaiusculas() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        criarCategoria(mockMvc, evento, "Bebidas", null);

        postJson(mockMvc, "/api/eventos/" + evento + "/categorias", "{\"nome\": \"bebidas\"}")
                .andExpect(status().isConflict());
    }

    @Test
    void naoDeveUsarCategoriaDeOutroEvento() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long outro = criarEvento(mockMvc, "Almoço");
        long categoriaDoOutro = criarCategoria(mockMvc, outro, "Pratos", null);

        postJson(mockMvc, "/api/eventos/" + evento + "/itens", """
                {"nome": "Arroz", "categoriaId": %d, "quantidadeNecessaria": 1, "obrigatorio": true}
                """.formatted(categoriaDoOutro))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveValidarQuantidadePositiva() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Bebidas", null);

        postJson(mockMvc, "/api/eventos/" + evento + "/itens", """
                {"nome": "Suco", "categoriaId": %d, "quantidadeNecessaria": 0, "obrigatorio": true}
                """.formatted(categoria))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("quantidadeNecessaria"));
    }

    @Test
    void deveRecusarQuantidadesAcimaDoTeto() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Bebidas", null);

        postJson(mockMvc, "/api/eventos/" + evento + "/itens", """
                {"nome": "Suco", "categoriaId": %d, "quantidadeNecessaria": 100001, "obrigatorio": true}
                """.formatted(categoria))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("quantidadeNecessaria"));
        postJson(mockMvc, "/api/eventos/" + evento + "/categorias", """
                {"nome": "Comidas", "meta": 100001}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("meta"));
    }

    @Test
    void naoDeveExcluirCategoriaComItens() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Bebidas", null);
        criarItem(mockMvc, evento, categoria, "Suco", 3, true);

        mockMvc.perform(delete("/api/eventos/" + evento + "/categorias/" + categoria))
                .andExpect(status().isConflict());
    }

    @Test
    void naoDeveAlterarItensDeEventoEncerrado() throws Exception {
        long evento = criarEvento(mockMvc, "Café");
        long categoria = criarCategoria(mockMvc, evento, "Bebidas", null);
        criarItem(mockMvc, evento, categoria, "Suco", 3, true);
        alterarStatus(mockMvc, evento, "ABERTO");
        alterarStatus(mockMvc, evento, "ENCERRADO");

        postJson(mockMvc, "/api/eventos/" + evento + "/itens", """
                {"nome": "Água", "categoriaId": %d, "quantidadeNecessaria": 1, "obrigatorio": false}
                """.formatted(categoria))
                .andExpect(status().isUnprocessableEntity());
    }
}
