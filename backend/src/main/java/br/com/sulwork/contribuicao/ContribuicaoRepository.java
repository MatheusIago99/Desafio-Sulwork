package br.com.sulwork.contribuicao;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Long> {

    @Query("""
            select c from Contribuicao c
            join fetch c.item i
            join fetch i.categoria
            join fetch c.participante p
            where i.evento.id = :eventoId
            order by i.nome, p.nome
            """)
    List<Contribuicao> listarPorEvento(@Param("eventoId") Long eventoId);

    @Query("""
            select c from Contribuicao c
            join fetch c.item i
            join fetch c.participante
            where c.id = :id and i.evento.id = :eventoId
            """)
    Optional<Contribuicao> buscarNoEvento(@Param("id") Long id, @Param("eventoId") Long eventoId);

    @EntityGraph(attributePaths = {"item", "participante"})
    Optional<Contribuicao> findByItemIdAndParticipanteId(Long itemId, Long participanteId);

    boolean existsByItemIdAndParticipanteId(Long itemId, Long participanteId);

    boolean existsByItemId(Long itemId);

    /** Contribuições de um participante em todos os eventos, com item e evento já carregados. */
    @Query("""
            select c from Contribuicao c
            join fetch c.item i
            join fetch i.evento e
            where c.participante.id = :participanteId
            order by e.dataHora, i.nome
            """)
    List<Contribuicao> listarDoParticipante(@Param("participanteId") Long participanteId);

    /** Total contribuído (por todos) em cada item informado: cada linha é [itemId, soma]. */
    @Query("""
            select c.item.id, sum(c.quantidade) from Contribuicao c
            where c.item.id in :itemIds
            group by c.item.id
            """)
    List<Object[]> somarPorItens(@Param("itemIds") Collection<Long> itemIds);

    @Query("select coalesce(sum(c.quantidade), 0) from Contribuicao c where c.item.id = :itemId")
    int somarPorItem(@Param("itemId") Long itemId);

    /** Total contribuído por item do evento: cada linha é [itemId, soma]. */
    @Query("""
            select c.item.id, sum(c.quantidade) from Contribuicao c
            where c.item.evento.id = :eventoId
            group by c.item.id
            """)
    List<Object[]> somarPorItemDoEvento(@Param("eventoId") Long eventoId);

    @Modifying
    @Query("""
            delete from Contribuicao c
            where c.participante.id = :participanteId
              and c.item.id in (select i.id from Item i where i.evento.id = :eventoId)
            """)
    int removerDoParticipanteNoEvento(@Param("eventoId") Long eventoId, @Param("participanteId") Long participanteId);
}
