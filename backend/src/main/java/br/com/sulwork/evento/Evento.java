package br.com.sulwork.evento;

import java.time.LocalDateTime;

import br.com.sulwork.common.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvento tipo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "prazo_alteracoes", nullable = false)
    private LocalDateTime prazoAlteracoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEvento status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Evento() {
    }

    public Evento(String nome, String descricao, TipoEvento tipo, LocalDateTime dataHora,
                  LocalDateTime prazoAlteracoes, LocalDateTime agora) {
        validarDatas(dataHora, prazoAlteracoes, agora);
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.prazoAlteracoes = prazoAlteracoes;
        this.status = StatusEvento.RASCUNHO;
        this.criadoEm = agora;
    }

    public void atualizar(String nome, String descricao, TipoEvento tipo, LocalDateTime dataHora,
                          LocalDateTime prazoAlteracoes, LocalDateTime agora) {
        garantirEmOrganizacao();
        validarDatas(dataHora, prazoAlteracoes, agora);
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.prazoAlteracoes = prazoAlteracoes;
    }

    /**
     * Aplica uma transição de status. Além da tabela de transições do enum, há
     * guardas que dependem do tempo:
     * <ul>
     *   <li>abrir/reabrir exige que o prazo para alterações ainda não tenha vencido;</li>
     *   <li>marcar como realizado exige que a data do evento já tenha chegado.</li>
     * </ul>
     */
    public void transicionarPara(StatusEvento destino, LocalDateTime agora) {
        if (!status.podeTransicionarPara(destino)) {
            throw new RegraNegocioException(
                    "Transição de status não permitida: " + status + " -> " + destino);
        }
        if (destino == StatusEvento.ABERTO && prazoVencido(agora)) {
            throw new RegraNegocioException(
                    "Não é possível abrir o evento: o prazo para alterações já venceu. Atualize o prazo antes.");
        }
        if (destino == StatusEvento.REALIZADO && agora.isBefore(dataHora)) {
            throw new RegraNegocioException(
                    "O evento só pode ser marcado como realizado a partir da sua data e horário.");
        }
        this.status = destino;
    }

    public boolean prazoVencido(LocalDateTime agora) {
        return !agora.isBefore(prazoAlteracoes);
    }

    /**
     * Regra central da interação prazo x status: participantes só alteram inscrições
     * e contribuições quando o evento está ABERTO e o prazo ainda não venceu.
     */
    public boolean aceitaAlteracoesDeParticipantes(LocalDateTime agora) {
        return status == StatusEvento.ABERTO && !prazoVencido(agora);
    }

    public void garantirJanelaDeAlteracoes(LocalDateTime agora) {
        if (status != StatusEvento.ABERTO) {
            throw new RegraNegocioException(
                    "O evento não está aberto (status atual: " + status + "); alterações não são permitidas.");
        }
        if (prazoVencido(agora)) {
            throw new RegraNegocioException(
                    "O prazo para alterações deste evento venceu em " + prazoAlteracoes + ".");
        }
    }

    public void garantirEmOrganizacao() {
        if (!status.permiteOrganizacao()) {
            throw new RegraNegocioException(
                    "O evento não pode mais ser alterado (status atual: " + status + ").");
        }
    }

    private static void validarDatas(LocalDateTime dataHora, LocalDateTime prazoAlteracoes, LocalDateTime agora) {
        if (!dataHora.isAfter(agora)) {
            throw new RegraNegocioException("A data e horário do evento devem estar no futuro.");
        }
        if (prazoAlteracoes.isAfter(dataHora)) {
            throw new RegraNegocioException("O prazo para alterações não pode ser posterior à data do evento.");
        }
        // Um prazo já vencido criaria um evento que nunca poderia ser aberto. Para fechar as
        // alterações antes do prazo, o organizador encerra o evento.
        if (!prazoAlteracoes.isAfter(agora)) {
            throw new RegraNegocioException("O prazo para alterações deve estar no futuro.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public LocalDateTime getPrazoAlteracoes() {
        return prazoAlteracoes;
    }

    public StatusEvento getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
