package br.com.sulwork.alergeno;

import java.util.List;

import br.com.sulwork.inscricao.Inscricao;
import br.com.sulwork.inscricao.InscricaoService;
import br.com.sulwork.item.ItemService;
import br.com.sulwork.participante.Participante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestricoesService {

    private final InscricaoService inscricaoService;
    private final ItemService itemService;
    private final AnalisadorRestricoes analisador;

    public RestricoesService(InscricaoService inscricaoService, ItemService itemService,
                             AnalisadorRestricoes analisador) {
        this.inscricaoService = inscricaoService;
        this.itemService = itemService;
        this.analisador = analisador;
    }

    @Transactional(readOnly = true)
    public ResumoRestricoes resumir(Long eventoId) {
        List<Participante> inscritos = inscricaoService.listar(eventoId).stream()
                .map(Inscricao::getParticipante)
                .toList();
        return analisador.analisar(inscritos, itemService.listar(eventoId));
    }
}
