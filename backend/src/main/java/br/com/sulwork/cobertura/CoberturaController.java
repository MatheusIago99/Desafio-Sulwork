package br.com.sulwork.cobertura;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Cobertura")
public class CoberturaController {

    private final CoberturaService coberturaService;

    public CoberturaController(CoberturaService coberturaService) {
        this.coberturaService = coberturaService;
    }

    @GetMapping("/api/eventos/{eventoId}/cobertura")
    @Operation(summary = "Quanto das necessidades do evento já foi atendido, por item, categoria e no geral")
    public Cobertura cobertura(@PathVariable Long eventoId) {
        return coberturaService.calcular(eventoId);
    }
}
