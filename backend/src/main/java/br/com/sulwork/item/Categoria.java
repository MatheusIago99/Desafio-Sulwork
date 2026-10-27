package br.com.sulwork.item;

import br.com.sulwork.evento.Evento;
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
 * Agrupa os itens de um evento (ex.: Bebidas, Salgados). A meta é opcional e
 * representa a quantidade mínima de unidades que a categoria deve reunir,
 * independentemente de quais itens a compõem.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @Column(nullable = false, length = 60)
    private String nome;

    private Integer meta;

    protected Categoria() {
    }

    public Categoria(Evento evento, String nome, Integer meta) {
        this.evento = evento;
        atualizar(nome, meta);
    }

    public void atualizar(String nome, Integer meta) {
        this.nome = nome;
        this.meta = meta;
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public String getNome() {
        return nome;
    }

    public Integer getMeta() {
        return meta;
    }
}
