package br.com.sulwork.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEventoIdOrderByNomeAsc(Long eventoId);

    Optional<Categoria> findByIdAndEventoId(Long id, Long eventoId);

    boolean existsByEventoIdAndNomeIgnoreCase(Long eventoId, String nome);

    boolean existsByEventoIdAndNomeIgnoreCaseAndIdNot(Long eventoId, String nome, Long id);
}
