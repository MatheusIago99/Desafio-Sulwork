package br.com.sulwork.item.dto;

import br.com.sulwork.item.Categoria;

public record CategoriaResponse(Long id, String nome, Integer meta) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getMeta());
    }
}
