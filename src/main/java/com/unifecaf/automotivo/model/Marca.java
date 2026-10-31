package com.unifecaf.automotivo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "marca")
public class Marca extends EntidadeBase {
    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    protected Marca() { }
    public Marca(String nome) { this.nome = nome.strip(); }
    public String getNome() { return nome; }
}
