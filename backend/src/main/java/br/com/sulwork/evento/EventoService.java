package br.com.sulwork.evento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.common.exception.RegraNegocioException;
import br.com.sulwork.evento.dto.EventoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final Clock clock;

    public EventoService(EventoRepository eventoRepository, Clock clock) {
        this.eventoRepository = eventoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Evento> listar(StatusEvento status) {
        return status == null
                ? eventoRepository.findAllByOrderByDataHoraAsc()
                : eventoRepository.findByStatusOrderByDataHoraAsc(status);
    }

    @Transactional(readOnly = true)
    public Evento buscar(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", id));
    }

    @Transactional
    public Evento criar(EventoRequest req) {
        Evento evento = new Evento(req.nome().trim(), req.descricao(), req.tipo(),
                req.dataHora(), req.prazoAlteracoes(), agora());
        return eventoRepository.save(evento);
    }

    @Transactional
    public Evento atualizar(Long id, EventoRequest req) {
        Evento evento = buscar(id);
        evento.atualizar(req.nome().trim(), req.descricao(), req.tipo(),
                req.dataHora(), req.prazoAlteracoes(), agora());
        return evento;
    }

    @Transactional
    public Evento alterarStatus(Long id, StatusEvento destino) {
        Evento evento = buscar(id);
        evento.transicionarPara(destino, agora());
        return evento;
    }

    /** Apenas rascunhos podem ser excluídos; eventos já publicados devem ser cancelados. */
    @Transactional
    public void excluir(Long id) {
        Evento evento = buscar(id);
        if (evento.getStatus() != StatusEvento.RASCUNHO) {
            throw new RegraNegocioException("Apenas eventos em RASCUNHO podem ser excluídos; use o cancelamento.");
        }
        eventoRepository.delete(evento);
    }

    public LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
