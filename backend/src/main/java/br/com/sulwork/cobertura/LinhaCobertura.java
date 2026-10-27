package br.com.sulwork.cobertura;

/**
 * Projeção de uma linha da consulta nativa de cobertura: uma categoria e um de seus
 * itens (ou só a categoria, quando ela não tem itens) com o total já contribuído.
 */
public interface LinhaCobertura {

    Long getCategoriaId();

    String getCategoriaNome();

    Integer getCategoriaMeta();

    Long getItemId();

    String getItemNome();

    Boolean getObrigatorio();

    Integer getQuantidadeNecessaria();

    Integer getQuantidadeContribuida();
}
