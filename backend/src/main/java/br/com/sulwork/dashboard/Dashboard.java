package br.com.sulwork.dashboard;

import java.util.List;

import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.evento.dto.EventoResponse;

/**
 * Visão geral da aplicação. Todos os números são calculados a partir dos dados
 * cadastrados; não há métricas estimadas.
 *
 * @param coberturaMedia média da cobertura geral dos eventos abertos (null quando não há eventos abertos)
 * @param obrigatoriosPendentes itens obrigatórios ainda incompletos, somados nos eventos abertos
 */
public record Dashboard(
        long eventosAbertos,
        long eventosEmOrganizacao,
        long participantesCadastrados,
        long inscricoesEmEventosAbertos,
        Integer coberturaMedia,
        int obrigatoriosPendentes,
        List<EventosPorTipo> eventosPorTipo,
        List<ProximoEvento> proximosEventos) {

    public record EventosPorTipo(TipoEvento tipo, long quantidade) {
    }

    public record ProximoEvento(
            EventoResponse evento,
            long inscritos,
            int percentualCobertura,
            int obrigatoriosPendentes,
            boolean pronto) {
    }
}
