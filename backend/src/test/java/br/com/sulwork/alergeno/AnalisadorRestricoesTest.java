package br.com.sulwork.alergeno;

import static br.com.sulwork.alergeno.Alergeno.AMENDOIM;
import static br.com.sulwork.alergeno.Alergeno.GLUTEN;
import static br.com.sulwork.alergeno.Alergeno.LACTOSE;
import static br.com.sulwork.alergeno.Alergeno.OVO;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.item.Categoria;
import br.com.sulwork.item.Item;
import br.com.sulwork.participante.Participante;
import org.junit.jupiter.api.Test;

class AnalisadorRestricoesTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 3, 10, 9, 0);

    private final AnalisadorRestricoes analisador = new AnalisadorRestricoes();
    private final Evento evento =
            new Evento("Café", null, TipoEvento.CAFE_DA_MANHA, AGORA.plusDays(2), AGORA.plusDays(1), AGORA);
    private final Categoria categoria = new Categoria(evento, "Geral", null);

    private Item item(String nome, Alergeno... alergenos) {
        return new Item(evento, categoria, nome, 1, null, true, Set.of(alergenos));
    }

    private static Participante participante(String nome, String observacao, Alergeno... restricoes) {
        return new Participante(nome, "52998224725", Set.of(restricoes), observacao);
    }

    @Test
    void deveAgruparRestricoesDosInscritosOrdenandoPelasMaisFrequentes() {
        ResumoRestricoes resumo = analisador.analisar(List.of(
                participante("Carla", null, GLUTEN),
                participante("Ana", null, LACTOSE, GLUTEN),
                participante("Bruno", null)), List.of());

        assertThat(resumo.totalInscritos()).isEqualTo(3);
        assertThat(resumo.restricoes()).hasSize(2);
        assertThat(resumo.restricoes().get(0).alergeno()).isEqualTo(GLUTEN);
        assertThat(resumo.restricoes().get(0).participantes()).containsExactly("Ana", "Carla");
        assertThat(resumo.restricoes().get(1).alergeno()).isEqualTo(LACTOSE);
    }

    @Test
    void deveApontarQuaisInscritosSaoAfetadosPorCadaItem() {
        ResumoRestricoes resumo = analisador.analisar(
                List.of(participante("Ana", null, LACTOSE), participante("Bruno", null, OVO, LACTOSE)),
                List.of(item("Bolo", LACTOSE, OVO, GLUTEN), item("Frutas")));

        ResumoRestricoes.ItemComAlergenos bolo = resumo.itens().get(0);
        assertThat(bolo.seguroParaTodos()).isFalse();
        assertThat(bolo.participantesAfetados()).isEqualTo(2);
        assertThat(bolo.conflitos()).extracting(ResumoRestricoes.Conflito::alergeno).containsExactly(LACTOSE, OVO);
        assertThat(bolo.conflitos().get(0).participantes()).containsExactly("Ana", "Bruno");

        assertThat(resumo.itens().get(1).seguroParaTodos()).isTrue();
    }

    @Test
    void itemComAlergenoSemInscritoRestritoEhSeguro() {
        ResumoRestricoes resumo = analisador.analisar(
                List.of(participante("Ana", null, LACTOSE)), List.of(item("Paçoca", AMENDOIM)));

        assertThat(resumo.itens().get(0).alergenos()).containsExactly(AMENDOIM);
        assertThat(resumo.itens().get(0).seguroParaTodos()).isTrue();
    }

    @Test
    void deveListarObservacoesEmTextoLivre() {
        ResumoRestricoes resumo = analisador.analisar(
                List.of(participante("Ana", "  Vegetariana "), participante("Bruno", " ")), List.of());

        assertThat(resumo.observacoes()).containsExactly(new ResumoRestricoes.Observacao("Ana", "Vegetariana"));
    }
}
