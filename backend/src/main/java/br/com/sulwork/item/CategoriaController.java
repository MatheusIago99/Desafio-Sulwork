package br.com.sulwork.item;

import java.util.List;

import br.com.sulwork.item.dto.CategoriaRequest;
import br.com.sulwork.item.dto.CategoriaResponse;
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
@RequestMapping("/api/eventos/{eventoId}/categorias")
@Tag(name = "Categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Lista as categorias do evento")
    public List<CategoriaResponse> listar(@PathVariable Long eventoId) {
        return categoriaService.listar(eventoId).stream().map(CategoriaResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma categoria, com meta opcional de unidades")
    public CategoriaResponse criar(@PathVariable Long eventoId, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(categoriaService.criar(eventoId, request));
    }

    @PutMapping("/{categoriaId}")
    @Operation(summary = "Atualiza nome e meta da categoria")
    public CategoriaResponse atualizar(@PathVariable Long eventoId, @PathVariable Long categoriaId,
                                       @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(categoriaService.atualizar(eventoId, categoriaId, request));
    }

    @DeleteMapping("/{categoriaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui uma categoria sem itens")
    public void excluir(@PathVariable Long eventoId, @PathVariable Long categoriaId) {
        categoriaService.excluir(eventoId, categoriaId);
    }
}
