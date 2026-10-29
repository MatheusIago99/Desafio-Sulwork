package br.com.sulwork.participante;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    boolean existsByCpf(String cpf);

    List<Participante> findAllByOrderByNomeAsc();

    Optional<Participante> findByCpf(String cpf);

    @Query("""
            select p from Participante p
            where lower(p.nome) like lower(concat('%', :termo, '%'))
            order by p.nome
            """)
    List<Participante> buscarPorNome(@Param("termo") String termo);
}
