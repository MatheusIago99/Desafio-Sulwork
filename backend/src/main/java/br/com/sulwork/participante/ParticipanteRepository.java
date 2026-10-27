package br.com.sulwork.participante;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    boolean existsByCpf(String cpf);

    List<Participante> findAllByOrderByNomeAsc();

    @Query("""
            select p from Participante p
            where lower(p.nome) like lower(concat('%', :termo, '%')) or p.cpf like concat('%', :termo, '%')
            order by p.nome
            """)
    List<Participante> buscar(@Param("termo") String termo);
}
