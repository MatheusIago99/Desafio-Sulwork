package br.com.sulwork.demo;

import static br.com.sulwork.alergeno.Alergeno.AMENDOIM;
import static br.com.sulwork.alergeno.Alergeno.GLUTEN;
import static br.com.sulwork.alergeno.Alergeno.LACTOSE;
import static br.com.sulwork.alergeno.Alergeno.OVO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.contribuicao.ContribuicaoService;
import br.com.sulwork.contribuicao.dto.ContribuicaoRequest;
import br.com.sulwork.evento.Evento;
import br.com.sulwork.evento.EventoRepository;
import br.com.sulwork.evento.EventoService;
import br.com.sulwork.evento.StatusEvento;
import br.com.sulwork.evento.TipoEvento;
import br.com.sulwork.evento.dto.EventoRequest;
import br.com.sulwork.inscricao.InscricaoService;
import br.com.sulwork.item.CategoriaService;
import br.com.sulwork.item.ItemService;
import br.com.sulwork.item.dto.CategoriaRequest;
import br.com.sulwork.item.dto.ItemRequest;
import br.com.sulwork.participante.ParticipanteService;
import br.com.sulwork.participante.dto.ParticipanteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Popula o banco com um cenário de exemplo quando a aplicação sobe com o perfil "demo"
 * e não há eventos cadastrados. Usa os próprios services, então os dados passam pelas
 * mesmas regras de negócio da API.
 */
@Component
@Profile("demo")
public class DadosDemonstracao implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);

    private final EventoRepository eventoRepository;
    private final EventoService eventoService;
    private final ParticipanteService participanteService;
    private final InscricaoService inscricaoService;
    private final CategoriaService categoriaService;
    private final ItemService itemService;
    private final ContribuicaoService contribuicaoService;

    public DadosDemonstracao(EventoRepository eventoRepository, EventoService eventoService,
                             ParticipanteService participanteService, InscricaoService inscricaoService,
                             CategoriaService categoriaService, ItemService itemService,
                             ContribuicaoService contribuicaoService) {
        this.eventoRepository = eventoRepository;
        this.eventoService = eventoService;
        this.participanteService = participanteService;
        this.inscricaoService = inscricaoService;
        this.categoriaService = categoriaService;
        this.itemService = itemService;
        this.contribuicaoService = contribuicaoService;
    }

    // CPFs fictícios: números de exemplo amplamente usados em documentação e testes.

    @Override
    public void run(ApplicationArguments args) {
        if (eventoRepository.count() > 0) {
            return;
        }
        log.info("Perfil demo: criando dados de demonstração");

        long ana = participante("Ana Souza", "52998224725", null, LACTOSE);
        long bruno = participante("Bruno Lima", "11144477735", null);
        long carla = participante("Carla Mendes", "12345678909", "Vegetariana", GLUTEN);
        long diego = participante("Diego Rocha", "98765432100", null, AMENDOIM);
        participante("Elisa Martins", "00000000191", null);

        LocalDateTime agora = eventoService.agora().truncatedTo(ChronoUnit.HOURS);

        Evento cafe = eventoService.criar(new EventoRequest("Café da manhã de boas-vindas",
                "Café para receber o pessoal novo do time de produto.", TipoEvento.CAFE_DA_MANHA,
                agora.plusDays(7).withHour(8), agora.plusDays(5).withHour(18)));
        long bebidas = categoria(cafe, "Bebidas", 8);
        long comidas = categoria(cafe, "Comidas", null);
        long descartaveis = categoria(cafe, "Descartáveis", null);
        long cafePassado = item(cafe, bebidas, "Café passado", 3, "garrafas", true);
        long suco = item(cafe, bebidas, "Suco natural", 4, "litros", false);
        item(cafe, bebidas, "Leite", 2, "litros", false, LACTOSE);
        long pao = item(cafe, comidas, "Pão francês", 30, "unidades", true, GLUTEN);
        long bolo = item(cafe, comidas, "Bolo de cenoura", 2, null, true, GLUTEN, OVO, LACTOSE);
        long frutas = item(cafe, comidas, "Salada de frutas", 1, "travessa", false);
        item(cafe, comidas, "Pão de queijo", 40, "unidades", false, LACTOSE, OVO);
        item(cafe, descartaveis, "Copos", 50, "unidades", true);
        eventoService.alterarStatus(cafe.getId(), StatusEvento.ABERTO);
        for (long participante : new long[] {ana, bruno, carla, diego}) {
            inscricaoService.inscrever(cafe.getId(), participante);
        }
        contribuir(cafe, ana, cafePassado, 2);
        contribuir(cafe, bruno, pao, 20);
        contribuir(cafe, carla, frutas, 1);
        contribuir(cafe, diego, suco, 2);
        contribuir(cafe, bruno, bolo, 1);

        Evento churrasco = eventoService.criar(new EventoRequest("Churrasco de fim de ano",
                "Confraternização de encerramento do ano.", TipoEvento.CHURRASCO,
                agora.plusDays(30).withHour(12), agora.plusDays(27).withHour(18)));
        long carnes = categoria(churrasco, "Carnes", null);
        item(churrasco, carnes, "Picanha", 4, "kg", true);
        item(churrasco, carnes, "Linguiça", 3, "kg", true);
    }

    private long participante(String nome, String cpf, String observacoes, Alergeno... restricoes) {
        return participanteService.criar(new ParticipanteRequest(nome, cpf, Set.of(restricoes), observacoes)).getId();
    }

    private long categoria(Evento evento, String nome, Integer meta) {
        return categoriaService.criar(evento.getId(), new CategoriaRequest(nome, meta)).getId();
    }

    private long item(Evento evento, long categoria, String nome, int quantidade, String unidade, boolean obrigatorio,
                      Alergeno... alergenos) {
        return itemService.criar(evento.getId(),
                new ItemRequest(nome, categoria, quantidade, unidade, obrigatorio, Set.of(alergenos))).getId();
    }

    private void contribuir(Evento evento, long participante, long item, int quantidade) {
        contribuicaoService.criar(evento.getId(), new ContribuicaoRequest(participante, item, quantidade));
    }
}
