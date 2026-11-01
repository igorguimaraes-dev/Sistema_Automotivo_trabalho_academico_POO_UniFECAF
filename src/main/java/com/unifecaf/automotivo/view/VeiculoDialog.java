package com.unifecaf.automotivo.view;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.model.*;
import com.unifecaf.automotivo.service.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.textfield.*;
import jakarta.validation.Validator;
import java.util.Map;

class VeiculoDialog extends Dialog {
    VeiculoDialog(VeiculoService service, CatalogoService catalogo, Validator validator,
                  VeiculoSaida existente, Runnable aoSalvar) {
        boolean edicao = existente != null;
        setHeaderTitle(edicao ? "Editar veículo" : "Novo veículo");
        setWidth("720px");
        setMaxWidth("calc(100vw - 32px)");
        setCloseOnOutsideClick(false);
        Span erro = UiSupport.erro();

        BigDecimalField preco = new BigDecimalField("Preço de venda");
        preco.setLocale(java.util.Locale.forLanguageTag("pt-BR"));
        preco.setPrefixComponent(new Span("R$"));
        preco.setPlaceholder("75000,00");
        preco.setHelperText("Use vírgula para os centavos. Ex.: 75000,50");
        preco.setRequiredIndicatorVisible(true);
        IntegerField km = new IntegerField("Quilometragem");
        km.setMin(0);
        km.setSuffixComponent(new Span("km"));
        km.setRequiredIndicatorVisible(true);
        ComboBox<StatusVeiculo> status = new ComboBox<>("Disponibilidade");
        status.setItems(StatusVeiculo.values());
        status.setItemLabelGenerator(UiSupport::status);
        status.setRequiredIndicatorVisible(true);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));
        ComboBox<MarcaSaida> marca = new ComboBox<>("Marca do veículo");
        ComboBox<ModeloSaida> modelo = new ComboBox<>("Modelo do veículo");
        IntegerField ano = new IntegerField("Ano de fabricação");
        TextField cor = new TextField("Cor");
        ComboBox<TipoVeiculo> tipo = new ComboBox<>("Tipo de veículo");

        marca.setItemLabelGenerator(MarcaSaida::nome);
        marca.setPlaceholder("Selecione a marca");
        marca.setRequiredIndicatorVisible(true);
        modelo.setItemLabelGenerator(ModeloSaida::nome);
        modelo.setPlaceholder("Selecione primeiro a marca");
        modelo.setRequiredIndicatorVisible(true);
        modelo.setEnabled(false);
        UiSupport.executar(erro, () -> {
            var modelos = catalogo.listarModelos();
            var marcas = catalogo.listarMarcas();
            marca.setItems(marcas);
            marca.addValueChangeListener(e -> {
                modelo.clear();
                var opcoes = modelos.stream().filter(m -> e.getValue() != null
                        && m.marca().id().equals(e.getValue().id())).toList();
                modelo.setItems(opcoes);
                modelo.setEnabled(!opcoes.isEmpty());
                modelo.setHelperText(e.getValue() != null && opcoes.isEmpty()
                        ? "Cadastre um modelo para esta marca na tela Modelos." : "");
            });
            if (edicao) {
                marca.setValue(marcas.stream().filter(m -> m.id().equals(existente.marca().id())).findFirst().orElse(null));
                modelo.setValue(modelos.stream().filter(m -> m.id().equals(existente.modelo().id())).findFirst().orElse(null));
            }
        });
        ano.setMin(1886);
        ano.setMax(java.time.Year.now().getValue());
        ano.setRequiredIndicatorVisible(true);
        ano.setPlaceholder("Ex.: 2022");
        cor.setMaxLength(50);
        cor.setRequiredIndicatorVisible(true);
        cor.setPlaceholder("Ex.: Prata");
        tipo.setItems(TipoVeiculo.values());
        tipo.setItemLabelGenerator(UiSupport::tipo);
        tipo.setRequiredIndicatorVisible(true);
        if (edicao) {
            add(new Paragraph("Atualize as características e a disponibilidade deste veículo."));
            ano.setValue(existente.anoFabricacao());
            cor.setValue(existente.cor());
            tipo.setValue(existente.tipo());
            preco.setValue(existente.preco());
            km.setValue(existente.quilometragem());
            status.setValue(existente.status());
        } else {
            status.setValue(StatusVeiculo.DISPONIVEL);
            km.setValue(0);
        }
        form.add(marca, modelo, ano, cor, tipo, preco, km, status);
        form.getChildren().forEach(c -> c.getElement().getStyle().set("width", "100%"));
        add(form, erro);
        Button salvar = UiSupport.principal(edicao ? "Salvar alterações" : "Cadastrar veículo", () -> {
            erro.setVisible(false);
            VeiculoEntrada dados = new VeiculoEntrada(modelo.getValue() == null ? null : modelo.getValue().id(),
                    ano.getValue(), cor.getValue(), preco.getValue(), km.getValue(), status.getValue(), tipo.getValue());
            marca.setInvalid(marca.isEmpty());
            marca.setErrorMessage("Selecione a marca.");
            boolean valido = UiSupport.validar(validator, dados, Map.of("modeloId", modelo, "anoFabricacao", ano,
                    "cor", cor, "preco", preco, "quilometragem", km, "status", status, "tipo", tipo));
            if (!valido || marca.isEmpty()) return;
            if (!UiSupport.executar(erro, () -> {
                if (edicao) service.atualizarCompleto(existente.id(), dados);
                else service.cadastrar(dados);
            })) return;
            close();
            aoSalvar.run();
            UiSupport.sucesso(edicao ? "Veículo atualizado." : "Veículo cadastrado.");
        });
        getFooter().add(new Button("Cancelar", e -> close()), salvar);
    }
}
