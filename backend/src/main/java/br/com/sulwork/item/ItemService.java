package br.com.sulwork.item;

import java.util.List;

import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.item.dto.ItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoriaService categoriaService;
    private final EventoService eventoService;

    public ItemService(ItemRepository itemRepository, CategoriaService categoriaService, EventoService eventoService) {
        this.itemRepository = itemRepository;
        this.categoriaService = categoriaService;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public List<Item> listar(Long eventoId) {
        eventoService.buscar(eventoId);
        return itemRepository.listarPorEvento(eventoId);
    }

    @Transactional(readOnly = true)
    public Item buscar(Long eventoId, Long itemId) {
        return itemRepository.findByIdAndEventoId(itemId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item", itemId));
    }

    @Transactional
    public Item criar(Long eventoId, ItemRequest req) {
        Evento evento = eventoService.buscar(eventoId);
        evento.garantirEmOrganizacao();
        Categoria categoria = categoriaService.buscar(eventoId, req.categoriaId());
        return itemRepository.save(new Item(evento, categoria, req.nome().trim(), req.quantidadeNecessaria(),
                req.unidade(), req.obrigatorio(), req.alergenos()));
    }

    @Transactional
    public Item atualizar(Long eventoId, Long itemId, ItemRequest req) {
        eventoService.buscar(eventoId).garantirEmOrganizacao();
        Item item = buscar(eventoId, itemId);
        Categoria categoria = categoriaService.buscar(eventoId, req.categoriaId());
        item.atualizar(categoria, req.nome().trim(), req.quantidadeNecessaria(), req.unidade(),
                req.obrigatorio(), req.alergenos());
        return item;
    }

    @Transactional
    public void excluir(Long eventoId, Long itemId) {
        eventoService.buscar(eventoId).garantirEmOrganizacao();
        itemRepository.delete(buscar(eventoId, itemId));
    }
}
