package br.com.sulwork.cobertura;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import br.com.sulwork.cobertura.Cobertura.Criterio;
import org.junit.jupiter.api.Test;

class CalculadoraCoberturaTest {

    private final CalculadoraCobertura calculadora = new CalculadoraCobertura();

    record Linha(Long categoriaId, String categoriaNome, Integer categoriaMeta, Long itemId, String itemNome,
                 Boolean obrigatorio, Integer quantidadeNecessaria, Integer quantidadeContribuida)
            implements LinhaCobertura {

        @Override public Long getCategoriaId() { return categoriaId; }
        @Override public String getCategoriaNome() { return categoriaNome; }
        @Override public Integer getCategoriaMeta() { return categoriaMeta; }
        @Override public Long getItemId() { return itemId; }
        @Override public String getItemNome() { return itemNome; }
        @Override public Boolean getObrigatorio() { return obrigatorio; }
        @Override public Integer getQuantidadeNecessaria() { return quantidadeNecessaria; }
        @Override public Integer getQuantidadeContribuida() { return quantidadeContribuida; }
    }

    private static Linha item(long categoria, Integer meta, long item, boolean obrigatorio, int necessario, int contribuido) {
        return new Linha(categoria, "Cat " + categoria, meta, item, "Item " + item, obrigatorio, necessario, contribuido);
    }

    private static Linha categoriaVazia(long categoria, Integer meta) {
        return new Linha(categoria, "Cat " + categoria, meta, null, null, null, null, 0);
    }

    @Test
    void eventoSemCategoriasTemCoberturaZeroENaoEstaPronto() {
        Cobertura cobertura = calculadora.calcular(List.of());

        assertThat(cobertura.percentualGeral()).isZero();
        assertThat(cobertura.pronto()).isFalse();
    }

    @Test
    void itemObrigatorioPesaODobroDoOpcional() {
        // obrigatório 100% (peso 2) e opcional 0% (peso 1) => 2/3 = 66%
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 4, 4),
                item(1, null, 2, false, 4, 0)));

        assertThat(cobertura.categorias().get(0).criterio()).isEqualTo(Criterio.ITENS);
        assertThat(cobertura.percentualGeral()).isEqualTo(66);
    }

    @Test
    void usaRazaoPorItemENaoUnidadesAbsolutas() {
        // 50 guardanapos completos e 0 de 2 bolos: por unidades seria 96%, por razão é 50%
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 50, 50),
                item(1, null, 2, true, 2, 0)));

        assertThat(cobertura.percentualGeral()).isEqualTo(50);
    }

    @Test
    void categoriaComMetaUsaTotalContribuidoSobreAMeta() {
        // meta de 10 unidades, 6 contribuídas no total da categoria
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, 10, 1, false, 8, 4),
                item(1, 10, 2, false, 8, 2)));

        Cobertura.CategoriaCobertura categoria = cobertura.categorias().get(0);
        assertThat(categoria.criterio()).isEqualTo(Criterio.META);
        assertThat(categoria.percentual()).isEqualTo(60);
        assertThat(cobertura.metasTotal()).isEqualTo(1);
        assertThat(cobertura.metasAtingidas()).isZero();
    }

    @Test
    void metaAcimaDoTotalNecessarioEhSinalizadaComoInatingivel() {
        Cobertura cobertura = calculadora.calcular(List.of(item(1, 20, 1, false, 5, 5)));

        assertThat(cobertura.categorias().get(0).metaAtingivel()).isFalse();
        assertThat(cobertura.categorias().get(0).percentual()).isEqualTo(25);
    }

    @Test
    void geralEhMediaSimplesDasCategoriasIgnorandoCategoriasVaziasSemMeta() {
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 10, 10),   // 100%
                item(2, null, 2, true, 10, 0),    // 0%
                categoriaVazia(3, null)));        // ignorada

        assertThat(cobertura.categorias()).hasSize(3);
        assertThat(cobertura.categorias().get(2).criterio()).isEqualTo(Criterio.IGNORADA);
        assertThat(cobertura.percentualGeral()).isEqualTo(50);
    }

    @Test
    void categoriaComMetaESemItensContaComoZero() {
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 10, 10),
                categoriaVazia(2, 5)));

        assertThat(cobertura.percentualGeral()).isEqualTo(50);
        assertThat(cobertura.categorias().get(1).metaAtingivel()).isFalse();
    }

    @Test
    void naoArredondaParaCimaAte100() {
        Cobertura cobertura = calculadora.calcular(List.of(item(1, null, 1, true, 1000, 999)));

        assertThat(cobertura.percentualGeral()).isEqualTo(99);
    }

    @Test
    void prontoExigeObrigatoriosCompletosEMetasAtingidasMesmoSemOpcionais() {
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 3, 3),
                item(1, null, 2, false, 5, 0),
                item(2, 4, 3, false, 4, 4)));

        assertThat(cobertura.obrigatoriosTotal()).isEqualTo(1);
        assertThat(cobertura.obrigatoriosCompletos()).isEqualTo(1);
        assertThat(cobertura.metasAtingidas()).isEqualTo(1);
        assertThat(cobertura.pronto()).isTrue();
        assertThat(cobertura.percentualGeral()).isLessThan(100);
    }

    @Test
    void naoEstaProntoComObrigatorioIncompletoMesmoComPercentualAlto() {
        Cobertura cobertura = calculadora.calcular(List.of(
                item(1, null, 1, true, 10, 9),
                item(2, null, 2, false, 10, 10),
                item(3, null, 3, false, 10, 10)));

        assertThat(cobertura.percentualGeral()).isGreaterThanOrEqualTo(90);
        assertThat(cobertura.pronto()).isFalse();
    }
}
