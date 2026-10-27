package br.com.sulwork.cobertura;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.sulwork.cobertura.Cobertura.CategoriaCobertura;
import br.com.sulwork.cobertura.Cobertura.Criterio;
import br.com.sulwork.cobertura.Cobertura.ItemCobertura;
import org.springframework.stereotype.Component;

/**
 * Calcula a cobertura de um evento a partir das linhas da consulta nativa.
 *
 * <ol>
 *   <li><b>Item</b>: contribuído / necessário. Usar a razão (e não unidades absolutas)
 *       evita que um item de 50 guardanapos pese mais que um de 2 bolos.</li>
 *   <li><b>Categoria sem meta</b>: média ponderada dos itens; obrigatório pesa 2 e opcional 1.</li>
 *   <li><b>Categoria com meta</b>: total de unidades contribuídas na categoria / meta. A meta é a
 *       definição explícita do organizador de "suficiente" para aquela categoria.</li>
 *   <li><b>Geral</b>: média simples das categorias consideradas; cada categoria é uma área de
 *       necessidade do evento (bebidas, salgados...).</li>
 *   <li><b>Pronto</b>: todos os itens obrigatórios completos e todas as metas atingidas,
 *       independentemente do percentual geral.</li>
 * </ol>
 * Percentuais são arredondados para baixo: o evento só mostra 100% quando está de fato completo.
 */
@Component
public class CalculadoraCobertura {

    static final int PESO_OBRIGATORIO = 2;
    static final int PESO_OPCIONAL = 1;

    public Cobertura calcular(List<LinhaCobertura> linhas) {
        Map<Long, List<LinhaCobertura>> porCategoria = new LinkedHashMap<>();
        for (LinhaCobertura linha : linhas) {
            porCategoria.computeIfAbsent(linha.getCategoriaId(), id -> new ArrayList<>()).add(linha);
        }

        List<CategoriaCobertura> categorias = porCategoria.values().stream().map(this::calcularCategoria).toList();
        List<CategoriaCobertura> consideradas = categorias.stream()
                .filter(c -> c.criterio() != Criterio.IGNORADA)
                .toList();

        double somaRazoes = consideradas.stream().mapToDouble(this::razaoCategoria).sum();
        int percentualGeral = consideradas.isEmpty() ? 0 : percentual(somaRazoes / consideradas.size());

        List<ItemCobertura> obrigatorios = categorias.stream()
                .flatMap(c -> c.itens().stream())
                .filter(ItemCobertura::obrigatorio)
                .toList();
        int obrigatoriosCompletos = (int) obrigatorios.stream().filter(ItemCobertura::completo).count();

        List<CategoriaCobertura> comMeta = categorias.stream().filter(c -> c.criterio() == Criterio.META).toList();
        int metasAtingidas = (int) comMeta.stream().filter(c -> c.totalContribuido() >= c.meta()).count();

        boolean temItens = categorias.stream().anyMatch(c -> !c.itens().isEmpty());
        boolean pronto = temItens && obrigatoriosCompletos == obrigatorios.size() && metasAtingidas == comMeta.size();

        return new Cobertura(percentualGeral, obrigatorios.size(), obrigatoriosCompletos,
                comMeta.size(), metasAtingidas, pronto, categorias);
    }

    private CategoriaCobertura calcularCategoria(List<LinhaCobertura> linhas) {
        LinhaCobertura primeira = linhas.get(0);
        List<ItemCobertura> itens = linhas.stream()
                .filter(l -> l.getItemId() != null)
                .map(l -> new ItemCobertura(l.getItemId(), l.getItemNome(), l.getObrigatorio(),
                        l.getQuantidadeNecessaria(), l.getQuantidadeContribuida(),
                        percentual(razao(l.getQuantidadeContribuida(), l.getQuantidadeNecessaria()))))
                .toList();

        int totalNecessario = itens.stream().mapToInt(ItemCobertura::quantidadeNecessaria).sum();
        int totalContribuido = itens.stream().mapToInt(ItemCobertura::quantidadeContribuida).sum();
        Integer meta = primeira.getCategoriaMeta();

        Criterio criterio;
        double razao;
        if (meta != null) {
            criterio = Criterio.META;
            razao = razao(totalContribuido, meta);
        } else if (!itens.isEmpty()) {
            criterio = Criterio.ITENS;
            razao = mediaPonderada(itens);
        } else {
            criterio = Criterio.IGNORADA;
            razao = 0;
        }

        // As contribuições nunca passam do necessário de cada item, então uma meta maior que a soma
        // das quantidades necessárias nunca seria atingida. Sinalizamos para o organizador ajustar.
        boolean metaAtingivel = meta == null || meta <= totalNecessario;

        return new CategoriaCobertura(primeira.getCategoriaId(), primeira.getCategoriaNome(), meta,
                totalNecessario, totalContribuido, percentual(razao), criterio, metaAtingivel, itens);
    }

    private double razaoCategoria(CategoriaCobertura categoria) {
        return categoria.criterio() == Criterio.META
                ? razao(categoria.totalContribuido(), categoria.meta())
                : mediaPonderada(categoria.itens());
    }

    private static double mediaPonderada(List<ItemCobertura> itens) {
        double soma = 0;
        int pesos = 0;
        for (ItemCobertura item : itens) {
            int peso = item.obrigatorio() ? PESO_OBRIGATORIO : PESO_OPCIONAL;
            soma += peso * razao(item.quantidadeContribuida(), item.quantidadeNecessaria());
            pesos += peso;
        }
        return pesos == 0 ? 0 : soma / pesos;
    }

    private static double razao(int contribuido, int necessario) {
        return necessario <= 0 ? 0 : Math.min(1.0, (double) contribuido / necessario);
    }

    private static int percentual(double razao) {
        return (int) Math.floor(razao * 100 + 1e-9);
    }
}
