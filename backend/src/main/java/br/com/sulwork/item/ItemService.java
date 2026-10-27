package br.com.sulwork.item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.common.exception.RegraNegocioException;
import br.com.sulwork.contribuicao.ContribuicaoRepository;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.item.dto.ItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ContribuicaoRepository contribuicaoRepository;
    private final CategoriaService categoriaService;
    private final EventoService eventoService;

    public ItemService(ItemRepository itemRepository, ContribuicaoRepository contribuicaoRepository,
                       CategoriaService categoriaService, EventoService eventoService) {
        this.itemRepository = itemRepository;
        this.contribuicaoRepository = contribuicaoRepository;
        this.categoriaService = categoriaService;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public List<Item> listar(Long eventoId) {
        eventoService.buscar(eventoId);
        return itemRepository.listarPorEvento(eventoId);
    }

    /** Quantidade já contribuída por item do evento (itens sem contribuição ficam de fora). */
    @Transactional(readOnly = true)
    public Map<Long, Integer> contribuidoPorItem(Long eventoId) {
        return contribuicaoRepository.somarPorItemDoEvento(eventoId).stream()
                .collect(Collectors.toMap(linha -> (Long) linha[0], linha -> ((Number) linha[1]).intValue()));
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
        Item item = itemRepository.findByIdAndEventoIdForUpdate(itemId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item", itemId));
        Categoria categoria = categoriaService.buscar(eventoId, req.categoriaId());
        int contribuido = contribuicaoRepository.somarPorItem(itemId);
        if (req.quantidadeNecessaria() < contribuido) {
            throw new RegraNegocioException("A quantidade necessária não pode ficar abaixo do já contribuído ("
                    + contribuido + ").");
        }
        item.atualizar(categoria, req.nome().trim(), req.quantidadeNecessaria(), req.unidade(),
                req.obrigatorio(), req.alergenos());
        return item;
    }

    @Transactional
    public void excluir(Long eventoId, Long itemId) {
        eventoService.buscar(eventoId).garantirEmOrganizacao();
        Item item = buscar(eventoId, itemId);
        if (contribuicaoRepository.existsByItemId(itemId)) {
            throw new ConflitoException("O item já possui contribuições; remova-as antes de excluir o item.");
        }
        itemRepository.delete(item);
    }
}
