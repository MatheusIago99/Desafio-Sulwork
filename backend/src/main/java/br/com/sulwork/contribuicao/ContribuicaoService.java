package br.com.sulwork.contribuicao;

import java.time.LocalDateTime;
import java.util.List;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.common.exception.RegraNegocioException;
import br.com.sulwork.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.inscricao.Inscricao;
import br.com.sulwork.inscricao.InscricaoRepository;
import br.com.sulwork.item.Item;
import br.com.sulwork.item.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de contribuição:
 * <ol>
 *   <li>só dentro da janela de alterações (evento ABERTO e prazo vigente);</li>
 *   <li>o participante precisa estar inscrito no evento;</li>
 *   <li>uma contribuição por participante e item;</li>
 *   <li>a soma das contribuições do item não pode ultrapassar a quantidade necessária.</li>
 * </ol>
 * A regra 4 depende de dados de outras transações; por isso o item é travado
 * (SELECT ... FOR UPDATE) antes de somar, serializando contribuições concorrentes no mesmo item.
 */
@Service
public class ContribuicaoService {

    private final ContribuicaoRepository contribuicaoRepository;
    private final ItemRepository itemRepository;
    private final InscricaoRepository inscricaoRepository;
    private final EventoService eventoService;

    public ContribuicaoService(ContribuicaoRepository contribuicaoRepository, ItemRepository itemRepository,
                               InscricaoRepository inscricaoRepository, EventoService eventoService) {
        this.contribuicaoRepository = contribuicaoRepository;
        this.itemRepository = itemRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public List<Contribuicao> listar(Long eventoId) {
        eventoService.buscar(eventoId);
        return contribuicaoRepository.listarPorEvento(eventoId);
    }

    @Transactional
    public Contribuicao criar(Long eventoId, ContribuicaoRequest req) {
        LocalDateTime agora = eventoService.agora();
        Evento evento = eventoService.buscar(eventoId);
        evento.garantirJanelaDeAlteracoes(agora);

        Inscricao inscricao = inscricaoRepository.findByEventoIdAndParticipanteId(eventoId, req.participanteId())
                .orElseThrow(() -> new RegraNegocioException(
                        "O participante precisa estar inscrito no evento para contribuir."));
        Item item = travarItem(eventoId, req.itemId());

        if (contribuicaoRepository.existsByItemIdAndParticipanteId(item.getId(), req.participanteId())) {
            throw new ConflitoException(
                    "O participante já contribui com este item; altere a quantidade da contribuição existente.");
        }
        garantirLimite(item, contribuicaoRepository.somarPorItem(item.getId()), req.quantidade());

        return contribuicaoRepository.save(
                new Contribuicao(item, inscricao.getParticipante(), req.quantidade(), agora));
    }

    @Transactional
    public Contribuicao alterarQuantidade(Long eventoId, Long contribuicaoId, int novaQuantidade) {
        LocalDateTime agora = eventoService.agora();
        eventoService.buscar(eventoId).garantirJanelaDeAlteracoes(agora);
        Contribuicao contribuicao = buscar(eventoId, contribuicaoId);
        Item item = travarItem(eventoId, contribuicao.getItem().getId());

        int somaDosOutros = contribuicaoRepository.somarPorItem(item.getId()) - contribuicao.getQuantidade();
        garantirLimite(item, somaDosOutros, novaQuantidade);

        contribuicao.alterarQuantidade(novaQuantidade, agora);
        return contribuicao;
    }

    @Transactional
    public void remover(Long eventoId, Long contribuicaoId) {
        eventoService.buscar(eventoId).garantirJanelaDeAlteracoes(eventoService.agora());
        contribuicaoRepository.delete(buscar(eventoId, contribuicaoId));
    }

    private Contribuicao buscar(Long eventoId, Long contribuicaoId) {
        return contribuicaoRepository.buscarNoEvento(contribuicaoId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contribuição", contribuicaoId));
    }

    private Item travarItem(Long eventoId, Long itemId) {
        return itemRepository.findByIdAndEventoIdForUpdate(itemId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item", itemId));
    }

    private static void garantirLimite(Item item, int jaContribuido, int quantidade) {
        int restante = item.getQuantidadeNecessaria() - jaContribuido;
        if (quantidade > restante) {
            throw new RegraNegocioException(
                    "Quantidade excede o necessário para \"" + item.getNome() + "\": restam " + restante
                            + " de " + item.getQuantidadeNecessaria() + ".");
        }
    }
}
