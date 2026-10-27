package br.com.sulwork.participante;

import java.util.List;

import br.com.sulwork.participante.dto.ParticipanteRequest;
import br.com.sulwork.participante.dto.ParticipanteResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/participantes")
@Tag(name = "Participantes")
public class ParticipanteController {

    private final ParticipanteService participanteService;

    public ParticipanteController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping
    @Operation(summary = "Lista participantes, com busca opcional por nome ou CPF")
    public List<ParticipanteResponse> listar(@RequestParam(required = false) String busca) {
        return participanteService.listar(busca).stream().map(ParticipanteResponse::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um participante")
    public ParticipanteResponse buscar(@PathVariable Long id) {
        return ParticipanteResponse.de(participanteService.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um participante (CPF com ou sem máscara)")
    public ParticipanteResponse criar(@Valid @RequestBody ParticipanteRequest request) {
        return ParticipanteResponse.de(participanteService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e restrições alimentares (o CPF não muda)")
    public ParticipanteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ParticipanteRequest request) {
        return ParticipanteResponse.de(participanteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um participante sem inscrições")
    public void excluir(@PathVariable Long id) {
        participanteService.excluir(id);
    }
}
