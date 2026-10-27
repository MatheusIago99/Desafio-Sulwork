package br.com.sulwork.cobertura;

import java.util.List;

import br.com.sulwork.item.Categoria;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CoberturaRepository extends Repository<Categoria, Long> {

    /**
     * Consulta SQL nativa que agrega, em uma única ida ao banco, a quantidade contribuída
     * por item, agrupada por categoria. O LEFT JOIN a partir de categoria mantém
     * categorias sem itens (relevantes quando têm meta) e itens sem contribuições.
     */
    @Query(value = """
            SELECT c.id                              AS "categoriaId",
                   c.nome                            AS "categoriaNome",
                   c.meta                            AS "categoriaMeta",
                   i.id                              AS "itemId",
                   i.nome                            AS "itemNome",
                   i.obrigatorio                     AS "obrigatorio",
                   i.quantidade_necessaria           AS "quantidadeNecessaria",
                   CAST(COALESCE(SUM(ct.quantidade), 0) AS INTEGER) AS "quantidadeContribuida"
            FROM categoria c
            LEFT JOIN item i          ON i.categoria_id = c.id
            LEFT JOIN contribuicao ct ON ct.item_id = i.id
            WHERE c.evento_id = :eventoId
            GROUP BY c.id, c.nome, c.meta, i.id, i.nome, i.obrigatorio, i.quantidade_necessaria
            ORDER BY c.nome, i.nome
            """, nativeQuery = true)
    List<LinhaCobertura> consultarPorEvento(@Param("eventoId") Long eventoId);
}
