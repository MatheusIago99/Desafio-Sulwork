package br.com.sulwork.contribuicao;

import java.time.LocalDateTime;

import br.com.sulwork.item.Item;
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

/**
 * Compromisso de um participante de levar certa quantidade de um item.
 * Cada participante tem no máximo uma contribuição por item; para mudar a
 * quantidade, a contribuição é atualizada.
 */
@Entity
@Table(name = "contribuicao")
public class Contribuicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participante_id")
    private Participante participante;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Contribuicao() {
    }

    public Contribuicao(Item item, Participante participante, int quantidade, LocalDateTime agora) {
        this.item = item;
        this.participante = participante;
        this.quantidade = quantidade;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void alterarQuantidade(int quantidade, LocalDateTime agora) {
        this.quantidade = quantidade;
        this.atualizadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public Participante getParticipante() {
        return participante;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
