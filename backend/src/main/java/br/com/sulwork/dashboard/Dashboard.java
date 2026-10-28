package br.com.sulwork.dashboard;

import java.time.LocalDateTime;
import java.util.List;

import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.evento.dto.EventoResponse;

/**
 * Visão geral da aplicação. Todos os números são calculados a partir dos dados
 * cadastrados; não há métricas estimadas.
 *
 * @param coberturaMedia média da cobertura geral dos eventos abertos (null quando não há eventos abertos)
 * @param obrigatoriosPendentes itens obrigatórios ainda incompletos, somados nos eventos abertos
 * @param proximosEventosTotal eventos por vir que ainda não foram realizados nem cancelados
 * @param proximosEventosNaSemana desses, quantos acontecem nos próximos 7 dias
 * @param contribuicoesTotal contribuições registradas nos eventos por vir
 * @param ultimaContribuicaoEm momento da contribuição mais recente (criação ou alteração), se houver
 */
public record Dashboard(
        long eventosAbertos,
        long eventosEmOrganizacao,
        long participantesCadastrados,
        long inscricoesEmEventosAbertos,
        Integer coberturaMedia,
        int obrigatoriosPendentes,
        long proximosEventosTotal,
        long proximosEventosNaSemana,
        long contribuicoesTotal,
        LocalDateTime ultimaContribuicaoEm,
        List<EventosPorTipo> eventosPorTipo,
        List<ProximoEvento> proximosEventos,
        List<PendenciaPrioritaria> pendenciasPrioritarias,
        List<ContribuicaoRecente> contribuicoesRecentes) {

    public record EventosPorTipo(TipoEvento tipo, long quantidade) {
    }

    public record ProximoEvento(
            EventoResponse evento,
            long inscritos,
            int percentualCobertura,
            int obrigatoriosPendentes,
            boolean pronto) {
    }

    /** Item obrigatório ainda incompleto em um evento aberto que aceita contribuições. */
    public record PendenciaPrioritaria(
            Long eventoId,
            String eventoNome,
            Long itemId,
            String itemNome,
            String categoria,
            int quantidadeFaltante,
            String unidade) {
    }

    /** Uma contribuição recente: quem vai levar o quê, e quando registrou ou alterou. */
    public record ContribuicaoRecente(
            Long eventoId,
            String eventoNome,
            String participanteNome,
            String itemNome,
            int quantidade,
            String unidade,
            LocalDateTime atualizadoEm) {
    }
}
