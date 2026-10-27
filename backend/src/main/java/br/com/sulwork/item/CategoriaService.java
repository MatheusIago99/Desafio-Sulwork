package br.com.sulwork.item;

import java.util.List;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.item.dto.CategoriaRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ItemRepository itemRepository;
    private final EventoService eventoService;

    public CategoriaService(CategoriaRepository categoriaRepository, ItemRepository itemRepository,
                            EventoService eventoService) {
        this.categoriaRepository = categoriaRepository;
        this.itemRepository = itemRepository;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar(Long eventoId) {
        eventoService.buscar(eventoId);
        return categoriaRepository.findByEventoIdOrderByNomeAsc(eventoId);
    }

    @Transactional(readOnly = true)
    public Categoria buscar(Long eventoId, Long categoriaId) {
        return categoriaRepository.findByIdAndEventoId(categoriaId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", categoriaId));
    }

    @Transactional
    public Categoria criar(Long eventoId, CategoriaRequest req) {
        Evento evento = eventoService.buscar(eventoId);
        evento.garantirEmOrganizacao();
        String nome = req.nome().trim();
        if (categoriaRepository.existsByEventoIdAndNomeIgnoreCase(eventoId, nome)) {
            throw new ConflitoException("Já existe uma categoria com este nome no evento.");
        }
        return categoriaRepository.save(new Categoria(evento, nome, req.meta()));
    }

    @Transactional
    public Categoria atualizar(Long eventoId, Long categoriaId, CategoriaRequest req) {
        eventoService.buscar(eventoId).garantirEmOrganizacao();
        Categoria categoria = buscar(eventoId, categoriaId);
        String nome = req.nome().trim();
        if (categoriaRepository.existsByEventoIdAndNomeIgnoreCaseAndIdNot(eventoId, nome, categoriaId)) {
            throw new ConflitoException("Já existe uma categoria com este nome no evento.");
        }
        categoria.atualizar(nome, req.meta());
        return categoria;
    }

    @Transactional
    public void excluir(Long eventoId, Long categoriaId) {
        eventoService.buscar(eventoId).garantirEmOrganizacao();
        Categoria categoria = buscar(eventoId, categoriaId);
        if (itemRepository.existsByCategoriaId(categoriaId)) {
            throw new ConflitoException("A categoria possui itens; remova ou mova os itens antes de excluí-la.");
        }
        categoriaRepository.delete(categoria);
    }
}
