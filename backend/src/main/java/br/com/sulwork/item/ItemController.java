package br.com.sulwork.item;

import java.util.List;

import br.com.sulwork.item.dto.ItemRequest;
import br.com.sulwork.item.dto.ItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/{eventoId}/itens")
@Tag(name = "Itens")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    @Operation(summary = "Lista os itens do evento, ordenados por categoria")
    public List<ItemResponse> listar(@PathVariable Long eventoId) {
        return itemService.listar(eventoId).stream().map(ItemResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um item com quantidade necessária, obrigatoriedade e alérgenos")
    public ItemResponse criar(@PathVariable Long eventoId, @Valid @RequestBody ItemRequest request) {
        return ItemResponse.de(itemService.criar(eventoId, request));
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Atualiza um item")
    public ItemResponse atualizar(@PathVariable Long eventoId, @PathVariable Long itemId,
                                  @Valid @RequestBody ItemRequest request) {
        return ItemResponse.de(itemService.atualizar(eventoId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um item")
    public void excluir(@PathVariable Long eventoId, @PathVariable Long itemId) {
        itemService.excluir(eventoId, itemId);
    }
}
