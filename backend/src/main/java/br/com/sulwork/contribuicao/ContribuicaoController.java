package br.com.sulwork.contribuicao;

import java.util.List;

import br.com.sulwork.contribuicao.dto.AlterarQuantidadeRequest;
import br.com.sulwork.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.contribuicao.dto.ContribuicaoResponse;
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
@RequestMapping("/api/eventos/{eventoId}/contribuicoes")
@Tag(name = "Contribuições")
public class ContribuicaoController {

    private final ContribuicaoService contribuicaoService;

    public ContribuicaoController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @GetMapping
    @Operation(summary = "Lista quem leva o quê no evento")
    public List<ContribuicaoResponse> listar(@PathVariable Long eventoId) {
        return contribuicaoService.listar(eventoId).stream().map(ContribuicaoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma contribuição (evento aberto, dentro do prazo, sem exceder o necessário)")
    public ContribuicaoResponse criar(@PathVariable Long eventoId, @Valid @RequestBody ContribuicaoRequest request) {
        return ContribuicaoResponse.de(contribuicaoService.criar(eventoId, request));
    }

    @PutMapping("/{contribuicaoId}")
    @Operation(summary = "Altera a quantidade de uma contribuição")
    public ContribuicaoResponse alterar(@PathVariable Long eventoId, @PathVariable Long contribuicaoId,
                                        @Valid @RequestBody AlterarQuantidadeRequest request) {
        return ContribuicaoResponse.de(contribuicaoService.alterarQuantidade(eventoId, contribuicaoId,
                request.quantidade()));
    }

    @DeleteMapping("/{contribuicaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma contribuição")
    public void remover(@PathVariable Long eventoId, @PathVariable Long contribuicaoId) {
        contribuicaoService.remover(eventoId, contribuicaoId);
    }
}
