package br.com.sulwork.alergeno;

import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Restrições e alérgenos")
public class AlergenoController {

    private final RestricoesService restricoesService;

    public AlergenoController(RestricoesService restricoesService) {
        this.restricoesService = restricoesService;
    }

    public record AlergenoResponse(Alergeno codigo, String descricao) {
    }

    @GetMapping("/api/alergenos")
    @Operation(summary = "Lista os alérgenos aceitos em restrições e itens")
    public List<AlergenoResponse> listar() {
        return Arrays.stream(Alergeno.values()).map(a -> new AlergenoResponse(a, a.getDescricao())).toList();
    }

    @GetMapping("/api/eventos/{eventoId}/restricoes")
    @Operation(summary = "Restrições dos inscritos cruzadas com os alérgenos dos itens do evento")
    public ResumoRestricoes resumo(@PathVariable Long eventoId) {
        return restricoesService.resumir(eventoId);
    }
}
