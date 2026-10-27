package br.com.sulwork.cobertura;

import br.com.sulwork.evento.EventoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoberturaService {

    private final CoberturaRepository coberturaRepository;
    private final CalculadoraCobertura calculadora;
    private final EventoService eventoService;

    public CoberturaService(CoberturaRepository coberturaRepository, CalculadoraCobertura calculadora,
                            EventoService eventoService) {
        this.coberturaRepository = coberturaRepository;
        this.calculadora = calculadora;
        this.eventoService = eventoService;
    }

    @Transactional(readOnly = true)
    public Cobertura calcular(Long eventoId) {
        eventoService.buscar(eventoId);
        return calculadora.calcular(coberturaRepository.consultarPorEvento(eventoId));
    }
}
