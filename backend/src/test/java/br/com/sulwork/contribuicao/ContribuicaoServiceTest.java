package br.com.sulwork.contribuicao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RegraNegocioException;
import br.com.sulwork.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.evento.StatusEvento;
import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.inscricao.Inscricao;
import br.com.sulwork.inscricao.InscricaoRepository;
import br.com.sulwork.item.Categoria;
import br.com.sulwork.item.Item;
import br.com.sulwork.item.ItemRepository;
import br.com.sulwork.participante.Participante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContribuicaoServiceTest {

    private static final long EVENTO_ID = 1L;
    private static final long ITEM_ID = 10L;
    private static final long PARTICIPANTE_ID = 20L;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 3, 10, 9, 0);

    @Mock
    private ContribuicaoRepository contribuicaoRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private InscricaoRepository inscricaoRepository;
    @Mock
    private EventoService eventoService;

    @InjectMocks
    private ContribuicaoService service;

    private Evento evento;
    private Item item;
    private Participante participante;

    @BeforeEach
    void setUp() {
        evento = new Evento("Café", null, TipoEvento.CAFE_DA_MANHA, AGORA.plusDays(10), AGORA.plusDays(8), AGORA);
        evento.transicionarPara(StatusEvento.ABERTO, AGORA);
        item = new Item(evento, new Categoria(evento, "Bebidas", null), "Suco", 10, "litros", true, Set.of());
        ReflectionTestUtils.setField(item, "id", ITEM_ID);
        participante = new Participante("Ana", "52998224725", Set.of(), null);

        when(eventoService.agora()).thenReturn(AGORA);
        when(eventoService.buscar(EVENTO_ID)).thenReturn(evento);
    }

    private void participanteInscrito() {
        when(inscricaoRepository.findByEventoIdAndParticipanteId(EVENTO_ID, PARTICIPANTE_ID))
                .thenReturn(Optional.of(new Inscricao(evento, participante, AGORA)));
        when(itemRepository.findByIdAndEventoIdForUpdate(ITEM_ID, EVENTO_ID)).thenReturn(Optional.of(item));
    }

    @Test
    void deveRegistrarContribuicaoDentroDoLimite() {
        participanteInscrito();
        when(contribuicaoRepository.somarPorItem(ITEM_ID)).thenReturn(4);
        when(contribuicaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Contribuicao contribuicao = service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 6));

        assertThat(contribuicao.getQuantidade()).isEqualTo(6);
        assertThat(contribuicao.getParticipante()).isSameAs(participante);
    }

    @Test
    void naoDeveUltrapassarAQuantidadeNecessaria() {
        participanteInscrito();
        when(contribuicaoRepository.somarPorItem(ITEM_ID)).thenReturn(4);

        assertThatThrownBy(() -> service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 7)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("restam 6 de 10");
        verify(contribuicaoRepository, never()).save(any());
    }

    @Test
    void naoDeveAceitarParticipanteNaoInscrito() {
        when(inscricaoRepository.findByEventoIdAndParticipanteId(EVENTO_ID, PARTICIPANTE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inscrito");
    }

    @Test
    void naoDeveDuplicarContribuicaoDoMesmoParticipanteNoItem() {
        participanteInscrito();
        when(contribuicaoRepository.existsByItemIdAndParticipanteId(ITEM_ID, PARTICIPANTE_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 1)))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void naoDeveContribuirAposOPrazo() {
        when(eventoService.agora()).thenReturn(AGORA.plusDays(9));

        assertThatThrownBy(() -> service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("prazo");
        verify(itemRepository, never()).findByIdAndEventoIdForUpdate(any(), any());
    }

    @Test
    void naoDeveContribuirEmEventoEncerrado() {
        evento.transicionarPara(StatusEvento.ENCERRADO, AGORA);

        assertThatThrownBy(() -> service.criar(EVENTO_ID, new ContribuicaoRequest(PARTICIPANTE_ID, ITEM_ID, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ENCERRADO");
    }

    @Test
    void aoAlterarDeveDesconsiderarAQuantidadeAtualDaPropriaContribuicao() {
        Contribuicao existente = new Contribuicao(item, participante, 4, AGORA);
        when(contribuicaoRepository.buscarNoEvento(5L, EVENTO_ID)).thenReturn(Optional.of(existente));
        when(itemRepository.findByIdAndEventoIdForUpdate(any(), any())).thenReturn(Optional.of(item));
        when(contribuicaoRepository.somarPorItem(any())).thenReturn(10); // 4 desta + 6 de outros

        Contribuicao alterada = service.alterarQuantidade(EVENTO_ID, 5L, 4);
        assertThat(alterada.getQuantidade()).isEqualTo(4);

        assertThatThrownBy(() -> service.alterarQuantidade(EVENTO_ID, 5L, 5))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("restam 4");
    }
}
