package br.com.sulwork.item;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
            select i from Item i
            join fetch i.categoria c
            where i.evento.id = :eventoId
            order by c.nome, i.nome
            """)
    List<Item> listarPorEvento(@Param("eventoId") Long eventoId);

    Optional<Item> findByIdAndEventoId(Long id, Long eventoId);

    /**
     * Trava a linha do item até o fim da transação. Serializa contribuições
     * concorrentes no mesmo item, para a soma nunca ultrapassar a quantidade necessária.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id and i.evento.id = :eventoId")
    Optional<Item> findByIdAndEventoIdForUpdate(@Param("id") Long id, @Param("eventoId") Long eventoId);

    long countByEventoId(Long eventoId);

    boolean existsByCategoriaId(Long categoriaId);
}
