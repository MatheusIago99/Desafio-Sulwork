package br.com.sulwork.item;

import java.util.EnumSet;
import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
import br.com.sulwork.evento.Evento;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(name = "quantidade_necessaria", nullable = false)
    private int quantidadeNecessaria;

    @Column(length = 20)
    private String unidade;

    @Column(nullable = false)
    private boolean obrigatorio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_alergeno", joinColumns = @JoinColumn(name = "item_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "alergeno", length = 30)
    private Set<Alergeno> alergenos = EnumSet.noneOf(Alergeno.class);

    protected Item() {
    }

    public Item(Evento evento, Categoria categoria, String nome, int quantidadeNecessaria, String unidade,
                boolean obrigatorio, Set<Alergeno> alergenos) {
        this.evento = evento;
        atualizar(categoria, nome, quantidadeNecessaria, unidade, obrigatorio, alergenos);
    }

    public void atualizar(Categoria categoria, String nome, int quantidadeNecessaria, String unidade,
                          boolean obrigatorio, Set<Alergeno> alergenos) {
        this.categoria = categoria;
        this.nome = nome;
        this.quantidadeNecessaria = quantidadeNecessaria;
        this.unidade = unidade;
        this.obrigatorio = obrigatorio;
        this.alergenos.clear();
        if (alergenos != null) {
            this.alergenos.addAll(alergenos);
        }
    }

    public Long getId() {
        return id;
    }

    public Evento getEvento() {
        return evento;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidadeNecessaria() {
        return quantidadeNecessaria;
    }

    public String getUnidade() {
        return unidade;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public Set<Alergeno> getAlergenos() {
        return alergenos;
    }
}
