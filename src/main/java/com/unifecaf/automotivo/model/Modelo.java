package com.unifecaf.automotivo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modelo", uniqueConstraints = @UniqueConstraint(columnNames = {"marca_id", "nome"}))
public class Modelo extends EntidadeBase {
    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    protected Modelo() { }
    public Modelo(String nome, Marca marca) {
        this.nome = nome.strip();
        this.marca = marca;
    }
    public String getNome() { return nome; }
    public Marca getMarca() { return marca; }
}
