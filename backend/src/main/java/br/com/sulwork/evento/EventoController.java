package br.com.sulwork.evento;

import java.util.List;

import br.com.sulwork.evento.dto.AlterarStatusRequest;
import br.com.sulwork.evento.dto.EventoRequest;
import br.com.sulwork.evento.dto.EventoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    @Operation(summary = "Lista eventos, opcionalmente filtrando por status")
    public List<EventoResponse> listar(@RequestParam(required = false) StatusEvento status) {
        return eventoService.listar(status).stream()
                .map(e -> EventoResponse.de(e, eventoService.agora()))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um evento")
    public EventoResponse buscar(@PathVariable Long id) {
        return EventoResponse.de(eventoService.buscar(id), eventoService.agora());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um evento (sempre em RASCUNHO)")
    public EventoResponse criar(@Valid @RequestBody EventoRequest request) {
        return EventoResponse.de(eventoService.criar(request), eventoService.agora());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um evento em RASCUNHO ou ABERTO")
    public EventoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EventoRequest request) {
        return EventoResponse.de(eventoService.atualizar(id, request), eventoService.agora());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status do evento respeitando a máquina de estados")
    public EventoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest request) {
        return EventoResponse.de(eventoService.alterarStatus(id, request.status()), eventoService.agora());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um evento em RASCUNHO")
    public void excluir(@PathVariable Long id) {
        eventoService.excluir(id);
    }
}
