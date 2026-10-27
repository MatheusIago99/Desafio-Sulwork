package br.com.sulwork.pendencia;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.sulwork.alergeno.ResumoRestricoes;
import br.com.sulwork.alergeno.ResumoRestricoes.ItemComAlergenos;
import br.com.sulwork.alergeno.RestricoesService;
import br.com.sulwork.cobertura.Cobertura;
import br.com.sulwork.cobertura.Cobertura.CategoriaCobertura;
import br.com.sulwork.cobertura.Cobertura.Criterio;
import br.com.sulwork.cobertura.Cobertura.ItemCobertura;
import br.com.sulwork.cobertura.CoberturaService;
import br.com.sulwork.contribuicao.Contribuicao;
import br.com.sulwork.contribuicao.ContribuicaoService;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.item.Item;
import br.com.sulwork.item.ItemService;
import br.com.sulwork.pendencia.Pendencias.ItemPendente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta a lista "O que falta?" reaproveitando os cálculos de cobertura e de restrições,
 * para que a priorização use exatamente os mesmos números exibidos no restante da aplicação.
 *
 * <p>Ordenação: prioridade (ALTA, MEDIA, BAIXA), depois o item menos coberto primeiro,
 * depois a maior quantidade faltante.</p>
 */
@Service
public class PendenciaService {

    private static final Comparator<ItemPendente> ORDEM = Comparator
            .comparing(ItemPendente::prioridade)
            .thenComparingInt(ItemPendente::percentual)
            .thenComparing(Comparator.comparingInt(ItemPendente::quantidadeFaltante).reversed())
            .thenComparing(ItemPendente::nome, String.CASE_INSENSITIVE_ORDER);

    private final EventoService eventoService;
    private final ItemService itemService;
    private final CoberturaService coberturaService;
    private final RestricoesService restricoesService;
    private final ContribuicaoService contribuicaoService;

    public PendenciaService(EventoService eventoService, ItemService itemService, CoberturaService coberturaService,
                            RestricoesService restricoesService, ContribuicaoService contribuicaoService) {
        this.eventoService = eventoService;
        this.itemService = itemService;
        this.coberturaService = coberturaService;
        this.restricoesService = restricoesService;
        this.contribuicaoService = contribuicaoService;
    }

    /**
     * @param participanteId opcional; quando informado, cada item indica quanto este participante já leva.
     */
    @Transactional(readOnly = true)
    public Pendencias listar(Long eventoId, Long participanteId) {
        Evento evento = eventoService.buscar(eventoId);
        Cobertura cobertura = coberturaService.calcular(eventoId);
        Map<Long, Item> itens = itemService.listar(eventoId).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        ResumoRestricoes restricoes = restricoesService.resumir(eventoId);
        Map<Long, ItemComAlergenos> alergenos = restricoes.itens().stream()
                .collect(Collectors.toMap(ItemComAlergenos::itemId, Function.identity()));
        Map<Long, Integer> minhas = participanteId == null ? Map.of()
                : contribuicaoService.listar(eventoId).stream()
                        .filter(c -> c.getParticipante().getId().equals(participanteId))
                        .collect(Collectors.toMap(c -> c.getItem().getId(), Contribuicao::getQuantidade));

        List<ItemPendente> pendentes = cobertura.categorias().stream()
                .flatMap(categoria -> categoria.itens().stream()
                        .filter(item -> !item.completo())
                        .map(item -> pendente(categoria, item, itens.get(item.id()), alergenos.get(item.id()),
                                minhas.get(item.id()))))
                .sorted(ORDEM)
                .toList();

        int obrigatoriosPendentes = (int) pendentes.stream().filter(ItemPendente::obrigatorio).count();
        return new Pendencias(evento.aceitaAlteracoesDeParticipantes(eventoService.agora()),
                pendentes.size(), obrigatoriosPendentes, pendentes);
    }

    static Prioridade prioridade(CategoriaCobertura categoria, ItemCobertura item) {
        if (item.obrigatorio()) {
            return Prioridade.ALTA;
        }
        boolean metaPendente = categoria.criterio() == Criterio.META
                && categoria.totalContribuido() < categoria.meta();
        return metaPendente ? Prioridade.MEDIA : Prioridade.BAIXA;
    }

    private static ItemPendente pendente(CategoriaCobertura categoria, ItemCobertura cobertura, Item item,
                                         ItemComAlergenos alergenos, Integer minhaContribuicao) {
        return new ItemPendente(
                cobertura.id(),
                cobertura.nome(),
                categoria.nome(),
                item.getUnidade(),
                cobertura.obrigatorio(),
                prioridade(categoria, cobertura),
                cobertura.quantidadeNecessaria(),
                cobertura.quantidadeContribuida(),
                cobertura.quantidadeNecessaria() - cobertura.quantidadeContribuida(),
                cobertura.percentual(),
                alergenos.alergenos(),
                alergenos.conflitos(),
                alergenos.seguroParaTodos(),
                minhaContribuicao);
    }
}
