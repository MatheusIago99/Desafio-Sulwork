package br.com.sulwork.inscricao;

import java.time.LocalDateTime;

import br.com.sulwork.evento.Evento;
import br.com.sulwork.participante.Participante;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Participação de um colaborador em um evento. Única por (evento, participante). */
@Entity
@Table(name = "inscricao",
        uniqueConstraints = @UniqueConstraint(name = "uk_inscricao_evento_participante",
                columnNames = {"evento_id", "participante_id"}))
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participante_id")
    private Participante participante;

    @Column(name = "inscrito_em", nullable = false)
    private LocalDateTime inscritoEm;

    protected Inscricao() {
    }

    public Inscricao(Evento evento, Participante participante, LocalDateTime inscritoEm) {
        this.evento = evento;
        this.participante = participante;
        this.inscritoEm = inscritoEm;
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public Participante getParticipante() {
        return participante;
    }

    public LocalDateTime getInscritoEm() {
        return inscritoEm;
    }
}
