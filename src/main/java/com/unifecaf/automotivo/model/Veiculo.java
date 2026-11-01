package com.unifecaf.automotivo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Year;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "veiculo")
public class Veiculo extends EntidadeBase {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modelo_id", nullable = false)
    private Modelo modelo;

    @Column(name = "ano_fabricacao", nullable = false)
    private Integer anoFabricacao;
    @Column(nullable = false, length = 50)
    private String cor;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Integer quilometragem;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoVeiculo tipo;

    protected Veiculo() { }

    public Veiculo(Modelo modelo, Integer anoFabricacao, String cor, BigDecimal preco,
                   Integer quilometragem, StatusVeiculo status, TipoVeiculo tipo) {
        atualizarDados(modelo, anoFabricacao, cor, preco, quilometragem, status, tipo);
    }

    public void atualizarDados(Modelo modelo, Integer anoFabricacao, String cor, BigDecimal preco,
                              Integer quilometragem, StatusVeiculo status, TipoVeiculo tipo) {
        if (modelo == null || tipo == null) throw new IllegalArgumentException("Modelo e tipo são obrigatórios.");
        if (anoFabricacao == null || anoFabricacao < 1886 || anoFabricacao > Year.now().getValue())
            throw new IllegalArgumentException("Ano de fabricação deve estar entre 1886 e o ano atual.");
        if (cor == null || cor.isBlank() || cor.strip().length() > 50)
            throw new IllegalArgumentException("Informe uma cor de até 50 caracteres.");
        atualizarDadosComerciais(preco, quilometragem, status);
        this.modelo = modelo;
        this.anoFabricacao = anoFabricacao;
        this.cor = cor.strip();
        this.tipo = tipo;
    }

    public void atualizarDadosComerciais(BigDecimal preco, Integer quilometragem, StatusVeiculo status) {
        if (preco == null || preco.signum() <= 0 || preco.compareTo(new BigDecimal("9999999999.99")) > 0
                || preco.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Preço deve ser positivo, com até 10 inteiros e 2 decimais.");
        if (quilometragem == null || quilometragem < 0)
            throw new IllegalArgumentException("Quilometragem não pode ser negativa.");
        if (status == null) throw new IllegalArgumentException("Status é obrigatório.");
        this.preco = preco;
        this.quilometragem = quilometragem;
        this.status = status;
    }

    public boolean podeSerRemovido() { return status == StatusVeiculo.VENDIDO || status == StatusVeiculo.DESCONTINUADO; }
    public Modelo getModelo() { return modelo; }
    public Integer getAnoFabricacao() { return anoFabricacao; }
    public String getCor() { return cor; }
    public BigDecimal getPreco() { return preco; }
    public Integer getQuilometragem() { return quilometragem; }
    public StatusVeiculo getStatus() { return status; }
    public TipoVeiculo getTipo() { return tipo; }
}
