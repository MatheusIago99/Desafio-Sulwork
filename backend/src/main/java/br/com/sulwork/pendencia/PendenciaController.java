package br.com.sulwork.pendencia;

import br.com.sulwork.contribuicao.ContribuicaoService;
import br.com.sulwork.contribuicao.dto.ContribuicaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/{eventoId}/pendencias")
@Tag(name = "O que falta?")
public class PendenciaController {

    private final PendenciaService pendenciaService;
    private final ContribuicaoService contribuicaoService;

    public PendenciaController(PendenciaService pendenciaService, ContribuicaoService contribuicaoService) {
        this.pendenciaService = pendenciaService;
        this.contribuicaoService = contribuicaoService;
    }

    @GetMapping
    @Operation(summary = "Itens que ainda faltam, priorizados (obrigatórios primeiro, menos cobertos antes)")
    public Pendencias listar(@PathVariable Long eventoId, @RequestParam(required = false) Long participanteId) {
        return pendenciaService.listar(eventoId, participanteId);
    }

    @PostMapping("/{itemId}/assumir")
    @Operation(summary = "\"Eu levo\": assume a quantidade informada ou tudo o que falta do item")
    public ContribuicaoResponse assumir(@PathVariable Long eventoId, @PathVariable Long itemId,
                                        @Valid @RequestBody AssumirItemRequest request) {
        return ContribuicaoResponse.de(contribuicaoService.assumir(eventoId, itemId, request.participanteId(),
                request.quantidade()));
    }
}
