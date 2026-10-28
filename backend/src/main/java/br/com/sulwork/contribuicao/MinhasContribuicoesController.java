package br.com.sulwork.contribuicao;

import java.util.List;

import br.com.sulwork.contribuicao.dto.MinhaContribuicaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Contribuições")
public class MinhasContribuicoesController {

    private final ContribuicaoService contribuicaoService;

    public MinhasContribuicoesController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @GetMapping("/api/participantes/{participanteId}/contribuicoes")
    @Operation(summary = "Tudo o que o participante vai levar, em todos os eventos, e se ainda pode alterar")
    public List<MinhaContribuicaoResponse> listar(@PathVariable Long participanteId) {
        return contribuicaoService.listarDoParticipante(participanteId);
    }
}
