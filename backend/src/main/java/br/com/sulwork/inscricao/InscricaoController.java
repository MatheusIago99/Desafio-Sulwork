package br.com.sulwork.inscricao;

import java.util.List;

import br.com.sulwork.inscricao.dto.InscricaoRequest;
import br.com.sulwork.inscricao.dto.InscricaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos/{eventoId}/inscricoes")
@Tag(name = "Inscrições")
public class InscricaoController {

    private final InscricaoService inscricaoService;

    public InscricaoController(InscricaoService inscricaoService) {
        this.inscricaoService = inscricaoService;
    }

    @GetMapping
    @Operation(summary = "Lista os participantes inscritos no evento, com suas restrições")
    public List<InscricaoResponse> listar(@PathVariable Long eventoId) {
        return inscricaoService.listar(eventoId).stream().map(InscricaoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscreve um participante no evento (uma vez por evento)")
    public InscricaoResponse inscrever(@PathVariable Long eventoId, @Valid @RequestBody InscricaoRequest request) {
        return InscricaoResponse.de(inscricaoService.inscrever(eventoId, request.participanteId()));
    }

    @DeleteMapping("/{participanteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancela a inscrição do participante no evento")
    public void cancelar(@PathVariable Long eventoId, @PathVariable Long participanteId) {
        inscricaoService.cancelar(eventoId, participanteId);
    }
}
