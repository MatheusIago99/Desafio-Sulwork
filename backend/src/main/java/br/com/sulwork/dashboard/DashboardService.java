package br.com.sulwork.dashboard;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.sulwork.cobertura.Cobertura;
import br.com.sulwork.cobertura.CoberturaService;
import br.com.sulwork.dashboard.Dashboard.EventosPorTipo;
import br.com.sulwork.dashboard.Dashboard.ProximoEvento;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoRepository;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.evento.StatusEvento;
import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.evento.dto.EventoResponse;
import br.com.sulwork.inscricao.InscricaoRepository;
import br.com.sulwork.participante.ParticipanteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Agrega os indicadores da tela inicial. A cobertura de cada evento vem do mesmo
 * {@link CoberturaService} usado na página do evento, para os números baterem.
 */
@Service
public class DashboardService {

    static final int LIMITE_PROXIMOS = 5;
    private static final EnumSet<StatusEvento> STATUS_PROXIMOS =
            EnumSet.of(StatusEvento.RASCUNHO, StatusEvento.ABERTO, StatusEvento.ENCERRADO);

    private final EventoRepository eventoRepository;
    private final ParticipanteRepository participanteRepository;
    private final InscricaoRepository inscricaoRepository;
    private final CoberturaService coberturaService;
    private final EventoService eventoService;

    public DashboardService(EventoRepository eventoRepository, ParticipanteRepository participanteRepository,
                            InscricaoRepository inscricaoRepository, CoberturaService coberturaService,
                            EventoService eventoService) {
        this.eventoRepository = eventoRepository;
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.coberturaService = coberturaService;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public Dashboard montar() {
        LocalDateTime agora = eventoService.agora();

        List<Cobertura> coberturasAbertos = eventoRepository.findByStatusOrderByDataHoraAsc(StatusEvento.ABERTO)
                .stream()
                .map(evento -> coberturaService.calcular(evento.getId()))
                .toList();
        Integer coberturaMedia = coberturasAbertos.isEmpty() ? null
                : (int) Math.floor(coberturasAbertos.stream().mapToInt(Cobertura::percentualGeral).average().orElse(0));
        int obrigatoriosPendentes = coberturasAbertos.stream().mapToInt(DashboardService::pendentes).sum();

        List<ProximoEvento> proximos = eventoRepository
                .proximos(agora, STATUS_PROXIMOS, PageRequest.of(0, LIMITE_PROXIMOS))
                .stream()
                .map(evento -> proximo(evento, agora))
                .toList();

        return new Dashboard(
                coberturasAbertos.size(),
                eventoRepository.countByStatus(StatusEvento.RASCUNHO),
                participanteRepository.count(),
                inscricaoRepository.countByEventoStatus(StatusEvento.ABERTO),
                coberturaMedia,
                obrigatoriosPendentes,
                eventosPorTipo(),
                proximos);
    }

    /** Todos os tipos aparecem, inclusive com zero, sempre na mesma ordem. */
    private List<EventosPorTipo> eventosPorTipo() {
        Map<TipoEvento, Long> contagem = eventoRepository.contarPorTipo().stream()
                .collect(Collectors.toMap(l -> (TipoEvento) l[0], l -> ((Number) l[1]).longValue()));
        return Arrays.stream(TipoEvento.values())
                .map(tipo -> new EventosPorTipo(tipo, contagem.getOrDefault(tipo, 0L)))
                .toList();
    }

    private ProximoEvento proximo(Evento evento, LocalDateTime agora) {
        Cobertura cobertura = coberturaService.calcular(evento.getId());
        return new ProximoEvento(EventoResponse.de(evento, agora), inscricaoRepository.countByEventoId(evento.getId()),
                cobertura.percentualGeral(), pendentes(cobertura), cobertura.pronto());
    }

    private static int pendentes(Cobertura cobertura) {
        return cobertura.obrigatoriosTotal() - cobertura.obrigatoriosCompletos();
    }
}
