package br.com.sulwork.evento.dto;

import java.time.LocalDateTime;
import java.util.Set;

import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.StatusEvento;
import br.com.sulwork.evento.TipoEvento;

/**
 * Além dos dados do evento, expõe campos derivados que a interface usa para habilitar
 * ou bloquear ações sem duplicar regras de negócio no frontend.
 */
public record EventoResponse(
        Long id,
        String nome,
        String descricao,
        TipoEvento tipo,
        LocalDateTime dataHora,
        LocalDateTime prazoAlteracoes,
        StatusEvento status,
        boolean prazoVencido,
        boolean aceitaAlteracoes,
        Set<StatusEvento> transicoesPermitidas,
        LocalDateTime criadoEm) {

    public static EventoResponse de(Evento evento, LocalDateTime agora) {
        return new EventoResponse(
                evento.getId(),
                evento.getNome(),
                evento.getDescricao(),
                evento.getTipo(),
                evento.getDataHora(),
                evento.getPrazoAlteracoes(),
                evento.getStatus(),
                evento.prazoVencido(agora),
                evento.aceitaAlteracoesDeParticipantes(agora),
                evento.getStatus().transicoesPermitidas(),
                evento.getCriadoEm());
    }
}
