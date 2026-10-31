package com.unifecaf.automotivo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import com.unifecaf.automotivo.model.*;

/** Contratos de entrada e saída da API. */
public final class Dados {
    private Dados() { }

    public record MarcaEntrada(
            @NotBlank(message = "Nome é obrigatório.")
            @Size(max = 100, message = "Nome deve ter até 100 caracteres.") String nome) { }

    public record MarcaSaida(Long id, String nome) { }

    public record ModeloEntrada(
            @NotBlank(message = "Nome é obrigatório.")
            @Size(max = 100, message = "Nome deve ter até 100 caracteres.") String nome,
            @NotNull(message = "Marca é obrigatória.") @Positive(message = "ID da marca deve ser positivo.") Long marcaId) { }

    public record ModeloSaida(Long id, String nome, MarcaSaida marca) { }

    public record VeiculoEntrada(
            @NotNull(message = "Modelo é obrigatório.") @Positive(message = "ID do modelo deve ser positivo.") Long modeloId,
            @NotNull(message = "Ano é obrigatório.") @Min(value = 1886, message = "Ano deve ser igual ou maior que 1886.") Integer anoFabricacao,
            @NotBlank(message = "Cor é obrigatória.") @Size(max = 50, message = "Cor deve ter até 50 caracteres.") String cor,
            @NotNull(message = "Preço é obrigatório.")
            @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Preço aceita até 10 inteiros e 2 decimais.") BigDecimal preco,
            @NotNull(message = "Quilometragem é obrigatória.")
            @PositiveOrZero(message = "Quilometragem não pode ser negativa.") Integer quilometragem,
            @NotNull(message = "Status é obrigatório.") StatusVeiculo status,
            @NotNull(message = "Tipo é obrigatório.") TipoVeiculo tipo) { }

    public record AtualizacaoVeiculo(
            @NotNull(message = "Preço é obrigatório.")
            @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero.")
            @Digits(integer = 10, fraction = 2, message = "Preço aceita até 10 inteiros e 2 decimais.") BigDecimal preco,
            @NotNull(message = "Quilometragem é obrigatória.")
            @PositiveOrZero(message = "Quilometragem não pode ser negativa.") Integer quilometragem,
            @NotNull(message = "Status é obrigatório.") StatusVeiculo status) { }

    public record VeiculoSaida(Long id, MarcaSaida marca, ModeloSaida modelo,
            Integer anoFabricacao, String cor, BigDecimal preco, Integer quilometragem,
            StatusVeiculo status, TipoVeiculo tipo) { }
}
