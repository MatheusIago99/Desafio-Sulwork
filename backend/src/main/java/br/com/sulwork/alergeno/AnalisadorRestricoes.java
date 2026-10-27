package br.com.sulwork.alergeno;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.sulwork.alergeno.ResumoRestricoes.Conflito;
import br.com.sulwork.alergeno.ResumoRestricoes.ItemComAlergenos;
import br.com.sulwork.alergeno.ResumoRestricoes.Observacao;
import br.com.sulwork.alergeno.ResumoRestricoes.RestricaoNoEvento;
import br.com.sulwork.item.Item;
import br.com.sulwork.participante.Participante;
import org.springframework.stereotype.Component;

/**
 * Cruza as restrições dos inscritos com os alérgenos dos itens. O resultado é
 * informativo: um conflito não impede a contribuição (o item pode ser útil para os
 * demais), mas orienta quem organiza e quem contribui, por exemplo a levar também
 * uma opção sem lactose.
 */
@Component
public class AnalisadorRestricoes {

    public ResumoRestricoes analisar(Collection<Participante> inscritos, Collection<Item> itens) {
        Map<Alergeno, List<String>> participantesPorAlergeno = new EnumMap<>(Alergeno.class);
        List<Observacao> observacoes = new ArrayList<>();
        for (Participante participante : inscritos) {
            for (Alergeno alergeno : participante.getRestricoes()) {
                participantesPorAlergeno.computeIfAbsent(alergeno, a -> new ArrayList<>()).add(participante.getNome());
            }
            String texto = participante.getObservacoesAlimentares();
            if (texto != null && !texto.isBlank()) {
                observacoes.add(new Observacao(participante.getNome(), texto.trim()));
            }
        }

        List<RestricaoNoEvento> restricoes = participantesPorAlergeno.entrySet().stream()
                .map(e -> new RestricaoNoEvento(e.getKey(), e.getKey().getDescricao(), ordenados(e.getValue())))
                .sorted(Comparator.comparingInt((RestricaoNoEvento r) -> r.participantes().size()).reversed()
                        .thenComparing(RestricaoNoEvento::alergeno))
                .toList();

        List<ItemComAlergenos> itensAnalisados = itens.stream()
                .map(item -> analisarItem(item, participantesPorAlergeno))
                .toList();

        return new ResumoRestricoes(inscritos.size(), restricoes, observacoes, itensAnalisados);
    }

    public ItemComAlergenos analisarItem(Item item, Map<Alergeno, List<String>> participantesPorAlergeno) {
        List<Alergeno> alergenos = item.getAlergenos().stream().sorted().toList();
        List<Conflito> conflitos = new ArrayList<>();
        Set<String> afetados = new LinkedHashSet<>();
        for (Alergeno alergeno : alergenos) {
            List<String> participantes = participantesPorAlergeno.getOrDefault(alergeno, List.of());
            if (!participantes.isEmpty()) {
                conflitos.add(new Conflito(alergeno, ordenados(participantes)));
                afetados.addAll(participantes);
            }
        }
        return new ItemComAlergenos(item.getId(), item.getNome(), alergenos, conflitos, afetados.size(),
                conflitos.isEmpty());
    }

    private static List<String> ordenados(List<String> nomes) {
        return nomes.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
