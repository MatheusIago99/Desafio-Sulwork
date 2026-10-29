package br.com.sulwork.participante;

import java.util.List;

import br.com.sulwork.common.exception.ConflitoException;
import br.com.sulwork.common.exception.RecursoNaoEncontradoException;
import br.com.sulwork.common.validation.Cpf;
import br.com.sulwork.inscricao.InscricaoRepository;
import br.com.sulwork.participante.dto.AtualizarParticipanteRequest;
import br.com.sulwork.participante.dto.ParticipanteRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final InscricaoRepository inscricaoRepository;

    public ParticipanteService(ParticipanteRepository participanteRepository, InscricaoRepository inscricaoRepository) {
        this.participanteRepository = participanteRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Transactional(readOnly = true)
    public List<Participante> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return participanteRepository.findAllByOrderByNomeAsc();
        }
        String termo = busca.trim();
        String digitos = Cpf.normalizar(termo);
        // CPF só com os 11 dígitos e correspondência exata. Aceitar pedaços permitiria
        // descobrir o CPF completo a partir dos dígitos que a máscara mostra.
        if (digitos.matches("\\d{11}")) {
            return participanteRepository.findByCpf(digitos).map(List::of).orElse(List.of());
        }
        return participanteRepository.buscarPorNome(termo);
    }

    @Transactional(readOnly = true)
    public Participante buscar(Long id) {
        return participanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participante", id));
    }

    @Transactional
    public Participante criar(ParticipanteRequest req) {
        String cpf = Cpf.normalizar(req.cpf());
        if (participanteRepository.existsByCpf(cpf)) {
            throw new ConflitoException("Já existe um participante cadastrado com este CPF.");
        }
        return participanteRepository.save(
                new Participante(req.nome().trim(), cpf, req.restricoes(), req.observacoesAlimentares()));
    }

    @Transactional
    public Participante atualizar(Long id, AtualizarParticipanteRequest req) {
        Participante participante = buscar(id);
        participante.atualizar(req.nome().trim(), req.restricoes(), req.observacoesAlimentares());
        return participante;
    }

    @Transactional
    public void excluir(Long id) {
        Participante participante = buscar(id);
        if (inscricaoRepository.existsByParticipanteId(id)) {
            throw new ConflitoException("O participante está inscrito em eventos e não pode ser excluído.");
        }
        participanteRepository.delete(participante);
    }
}
