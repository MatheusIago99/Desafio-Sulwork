package br.com.sulwork.participante.dto;

import java.util.Set;
import java.util.TreeSet;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.common.validation.Cpf;
import br.com.sulwork.participante.Participante;

/**
 * O CPF só é usado para identificar o colaborador e evitar cadastro duplicado; por isso
 * a API nunca devolve o número completo, apenas a versão mascarada (minimização, LGPD).
 */
public record ParticipanteResponse(
        Long id,
        String nome,
        String cpfMascarado,
        Set<Alergeno> restricoes,
        String observacoesAlimentares) {

    public static ParticipanteResponse de(Participante p) {
        return new ParticipanteResponse(p.getId(), p.getNome(), Cpf.mascarar(p.getCpf()),
                new TreeSet<>(p.getRestricoes()), p.getObservacoesAlimentares());
    }
}
