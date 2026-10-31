package com.unifecaf.automotivo.view;

import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.unifecaf.automotivo.exception.RegraException;
import com.unifecaf.automotivo.model.*;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

final class UiSupport {
    private UiSupport() { }

    static Div cabecalho(String secao, String titulo, String descricao, Button acao) {
        Span legenda = new Span(secao);
        legenda.addClassName("eyebrow");
        Div texto = new Div(legenda, new H1(titulo), new Paragraph(descricao));
        Div header = new Div(texto);
        if (acao != null) header.add(acao);
        header.addClassName("page-header");
        return header;
    }

    static Button principal(String texto, Runnable acao) {
        Button button = new Button(texto, e -> acao.run());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    static Span erro() {
        Span erro = new Span();
        erro.addClassName("form-error");
        erro.getElement().setAttribute("role", "alert");
        erro.setVisible(false);
        return erro;
    }

    static boolean validar(Validator validator, Object dados, Map<String, HasValidation> campos) {
        campos.values().forEach(c -> c.setInvalid(false));
        var violacoes = validator.validate(dados);
        violacoes.forEach(v -> {
            HasValidation campo = campos.get(v.getPropertyPath().toString());
            if (campo != null) {
                campo.setErrorMessage(v.getMessage());
                campo.setInvalid(true);
            }
        });
        return violacoes.isEmpty();
    }

    static boolean executar(Span erro, Runnable acao) {
        erro.setVisible(false);
        try {
            acao.run();
            return true;
        } catch (RegraException | IllegalArgumentException e) {
            mostrarErro(erro, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            mostrarErro(erro, "Registro já cadastrado ou associação inválida. Confira os dados.");
        } catch (DataAccessException | CannotCreateTransactionException e) {
            mostrarErro(erro, "Não foi possível acessar os dados. Confira se o banco está disponível e tente novamente.");
        }
        return false;
    }

    static void mostrarErro(Span erro, String mensagem) {
        erro.setText(mensagem);
        erro.setVisible(true);
    }

    static void sucesso(String texto) {
        Notification notification = Notification.show(texto, 3500, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static String numero(Integer valor) {
        return NumberFormat.getIntegerInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }

    static String status(StatusVeiculo status) {
        return switch (status) {
            case DISPONIVEL -> "Disponível";
            case VENDIDO -> "Vendido";
            case DESCONTINUADO -> "Descontinuado";
        };
    }

    static String tipo(TipoVeiculo tipo) {
        return switch (tipo) {
            case CARRO -> "Carro";
            case MOTO -> "Moto";
            case UTILITARIO -> "Utilitário";
        };
    }

    static Span badge(StatusVeiculo status) {
        Span badge = new Span(status(status));
        badge.addClassNames("status-badge", "status-" + status.name().toLowerCase(Locale.ROOT));
        return badge;
    }
}
