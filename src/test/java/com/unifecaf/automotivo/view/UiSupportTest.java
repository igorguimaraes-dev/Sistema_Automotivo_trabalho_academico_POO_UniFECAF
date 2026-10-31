package com.unifecaf.automotivo.view;

import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.CannotCreateTransactionException;
import static org.assertj.core.api.Assertions.assertThat;

class UiSupportTest {
    @Test
    void bancoIndisponivelMostraMensagemNoFormularioSemPropagarErroInterno() {
        Span erro = UiSupport.erro();
        boolean sucesso = UiSupport.executar(erro, () -> {
            throw new CannotCreateTransactionException("Conexão indisponível");
        });
        assertThat(sucesso).isFalse();
        assertThat(erro.isVisible()).isTrue();
        assertThat(erro.getText()).contains("banco", "tente novamente");
    }
}
