package br.com.sulwork.inscricao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    boolean existsByEventoIdAndParticipanteId(Long eventoId, Long participanteId);

    boolean existsByParticipanteId(Long participanteId);

    Optional<Inscricao> findByEventoIdAndParticipanteId(Long eventoId, Long participanteId);

    @Query("""
            select i from Inscricao i
            join fetch i.participante p
            where i.evento.id = :eventoId
            order by p.nome
            """)
    List<Inscricao> listarPorEvento(@Param("eventoId") Long eventoId);
}
