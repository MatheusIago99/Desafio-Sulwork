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

    long countByStatus(StatusEvento status);

    /** Próximos eventos com um dos status informados, do mais próximo ao mais distante. */
    @Query("""
            select e from Evento e
            where e.dataHora >= :agora and e.status in :status
            order by e.dataHora
            """)
    List<Evento> proximos(@Param("agora") LocalDateTime agora, @Param("status") Collection<StatusEvento> status,
                          Pageable pagina);

    /** Quantidade de eventos por tipo: cada linha é [tipo, quantidade]. */
    @Query("select e.tipo, count(e) from Evento e group by e.tipo")
    List<Object[]> contarPorTipo();
}
