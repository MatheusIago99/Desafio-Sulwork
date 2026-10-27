package br.com.sulwork.participante.dto;

import java.util.Set;
import java.util.TreeSet;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.participante.Participante;

public record ParticipanteResponse(
        Long id,
        String nome,
        String cpf,
        Set<Alergeno> restricoes,
        String observacoesAlimentares) {

    public static ParticipanteResponse de(Participante p) {
        return new ParticipanteResponse(p.getId(), p.getNome(), p.getCpf(),
                new TreeSet<>(p.getRestricoes()), p.getObservacoesAlimentares());
    }
}
