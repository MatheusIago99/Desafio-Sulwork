package br.com.sulwork.contribuicao.dto;

import java.time.LocalDateTime;

import br.com.sulwork.contribuicao.Contribuicao;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.StatusEvento;
import br.com.sulwork.item.Item;

/**
 * Uma contribuição vista pelo participante: o que ele leva, em qual evento, e se ainda
 * pode alterar ({@code aceitaAlteracoes} segue a mesma regra de prazo e status do evento).
 */
public record MinhaContribuicaoResponse(
        Long contribuicaoId,
        Long eventoId,
        String eventoNome,
        LocalDateTime eventoDataHora,
        LocalDateTime prazoAlteracoes,
        StatusEvento eventoStatus,
        boolean prazoVencido,
        boolean aceitaAlteracoes,
        Long itemId,
        String itemNome,
        String unidade,
        boolean obrigatorio,
        int quantidade,
        int quantidadeNecessaria,
        int quantidadeContribuidaTotal) {

    public static MinhaContribuicaoResponse de(Contribuicao contribuicao, int totalDoItem, LocalDateTime agora) {
        Item item = contribuicao.getItem();
        Evento evento = item.getEvento();
        return new MinhaContribuicaoResponse(
                contribuicao.getId(),
                evento.getId(),
                evento.getNome(),
                evento.getDataHora(),
                evento.getPrazoAlteracoes(),
                evento.getStatus(),
                evento.prazoVencido(agora),
                evento.aceitaAlteracoesDeParticipantes(agora),
                item.getId(),
                item.getNome(),
                item.getUnidade(),
                item.isObrigatorio(),
                contribuicao.getQuantidade(),
                item.getQuantidadeNecessaria(),
                totalDoItem);
    }
}
