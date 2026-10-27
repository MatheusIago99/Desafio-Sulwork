package br.com.sulwork.participante;

import java.util.EnumSet;
import java.util.Set;

import br.com.sulwork.alergeno.Alergeno;
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
import jakarta.persistence.Table;

/**
 * Colaborador da Sulwork. É cadastrado uma única vez (CPF único) e pode se inscrever
 * em vários eventos.
 */
@Entity
@Table(name = "participante")
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 11, unique = true)
    private String cpf;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "participante_restricao", joinColumns = @JoinColumn(name = "participante_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "alergeno", length = 30)
    private Set<Alergeno> restricoes = EnumSet.noneOf(Alergeno.class);

    @Column(name = "observacoes_alimentares", length = 500)
    private String observacoesAlimentares;

    protected Participante() {
    }

    public Participante(String nome, String cpf, Set<Alergeno> restricoes, String observacoesAlimentares) {
        this.cpf = cpf;
        atualizar(nome, restricoes, observacoesAlimentares);
    }

    /** O CPF identifica o colaborador e não é alterável. */
    public void atualizar(String nome, Set<Alergeno> restricoes, String observacoesAlimentares) {
        this.nome = nome;
        this.restricoes.clear();
        if (restricoes != null) {
            this.restricoes.addAll(restricoes);
        }
        this.observacoesAlimentares = observacoesAlimentares;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public Set<Alergeno> getRestricoes() {
        return restricoes;
    }

    public String getObservacoesAlimentares() {
        return observacoesAlimentares;
    }
}
