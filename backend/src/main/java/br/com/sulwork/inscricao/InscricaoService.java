package br.com.sulwork.inscricao;

import java.util.List;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.contribuicao.ContribuicaoRepository;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.participante.Participante;
import br.com.sulwork.participante.ParticipanteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscricaoService {

    private final InscricaoRepository inscricaoRepository;
    private final ContribuicaoRepository contribuicaoRepository;
    private final EventoService eventoService;
    private final ParticipanteService participanteService;

    public InscricaoService(InscricaoRepository inscricaoRepository, ContribuicaoRepository contribuicaoRepository,
                            EventoService eventoService, ParticipanteService participanteService) {
        this.inscricaoRepository = inscricaoRepository;
        this.contribuicaoRepository = contribuicaoRepository;
        this.eventoService = eventoService;
        this.participanteService = participanteService;
    }

    @Transactional(readOnly = true)
    public List<Inscricao> listar(Long eventoId) {
        eventoService.buscar(eventoId);
        return inscricaoRepository.listarPorEvento(eventoId);
    }

    /** Inscrever-se segue a mesma janela das contribuições: evento ABERTO e prazo vigente. */
    @Transactional
    public Inscricao inscrever(Long eventoId, Long participanteId) {
        Evento evento = eventoService.buscar(eventoId);
        Participante participante = participanteService.buscar(participanteId);
        evento.garantirJanelaDeAlteracoes(eventoService.agora());
        if (inscricaoRepository.existsByEventoIdAndParticipanteId(eventoId, participanteId)) {
            throw new ConflitoException("O participante já está inscrito neste evento.");
        }
        return inscricaoRepository.save(new Inscricao(evento, participante, eventoService.agora()));
    }

    /** Ao sair do evento, as contribuições do participante nele são removidas junto. */
    @Transactional
    public void cancelar(Long eventoId, Long participanteId) {
        Evento evento = eventoService.buscar(eventoId);
        Inscricao inscricao = buscar(eventoId, participanteId);
        evento.garantirJanelaDeAlteracoes(eventoService.agora());
        contribuicaoRepository.removerDoParticipanteNoEvento(eventoId, participanteId);
        inscricaoRepository.delete(inscricao);
    }

    @Transactional(readOnly = true)
    public Inscricao buscar(Long eventoId, Long participanteId) {
        return inscricaoRepository.findByEventoIdAndParticipanteId(eventoId, participanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Inscrição do participante " + participanteId + " no evento", eventoId));
    }
}
