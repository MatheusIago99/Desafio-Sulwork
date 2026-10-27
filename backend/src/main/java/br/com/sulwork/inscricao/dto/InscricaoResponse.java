package br.com.sulwork.inscricao.dto;

import java.time.LocalDateTime;

import br.com.sulwork.inscricao.Inscricao;
import br.com.sulwork.participante.dto.ParticipanteResponse;

public record InscricaoResponse(Long id, ParticipanteResponse participante, LocalDateTime inscritoEm) {

    public static InscricaoResponse de(Inscricao inscricao) {
        return new InscricaoResponse(inscricao.getId(),
                ParticipanteResponse.de(inscricao.getParticipante()), inscricao.getInscritoEm());
    }
}
