package br.com.sulwork.evento;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findAllByOrderByDataHoraAsc();

    List<Evento> findByStatusOrderByDataHoraAsc(StatusEvento status);

    /** Próximos eventos com um dos status informados, do mais próximo ao mais distante. */
    @Query("""
            select e from Evento e
            where e.dataHora >= :agora and e.status in :status
            order by e.dataHora, e.id
            """)
    List<Evento> proximos(@Param("agora") LocalDateTime agora, @Param("status") Collection<StatusEvento> status,
                          Pageable pagina);

    @Query("select count(e) from Evento e where e.dataHora >= :inicio and e.dataHora < :fim and e.status in :status")
    long contarEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
                     @Param("status") Collection<StatusEvento> status);
}
