package br.com.sulwork.item.dto;

import java.util.Set;
import java.util.TreeSet;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.item.Item;

public record ItemResponse(
        Long id,
        String nome,
        CategoriaResponse categoria,
        int quantidadeNecessaria,
        String unidade,
        boolean obrigatorio,
        Set<Alergeno> alergenos,
        int quantidadeContribuida,
        int quantidadeFaltante) {

    public static ItemResponse de(Item item, int contribuido) {
        return new ItemResponse(item.getId(), item.getNome(), CategoriaResponse.de(item.getCategoria()),
                item.getQuantidadeNecessaria(), item.getUnidade(), item.isObrigatorio(),
                new TreeSet<>(item.getAlergenos()), contribuido, item.getQuantidadeNecessaria() - contribuido);
    }
}
