package com.unifecaf.automotivo.view;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.model.*;
import com.unifecaf.automotivo.service.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.*;
import jakarta.validation.Validator;

@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "veiculos", layout = MainLayout.class)
@PageTitle("Veículos | Sistema Automotivo")
public class VeiculosView extends Div {
    private final VeiculoService veiculos;
    private final CatalogoService catalogo;
    private final Validator validator;
    private final Grid<VeiculoSaida> tabela = new Grid<>(VeiculoSaida.class, false);
    private final ComboBox<MarcaSaida> marca = new ComboBox<>("Marca");
    private final ComboBox<ModeloSaida> modelo = new ComboBox<>("Modelo");
    private final BigDecimalField precoMin = new BigDecimalField("Preço mínimo");
    private final BigDecimalField precoMax = new BigDecimalField("Preço máximo");
    private final IntegerField ano = new IntegerField("Ano de fabricação");
    private final ComboBox<StatusVeiculo> status = new ComboBox<>("Disponibilidade");
    private final ComboBox<TipoVeiculo> tipo = new ComboBox<>("Tipo");
    private final Span erro = UiSupport.erro();
    private final Span quantidade = new Span();
    private final Div vazio = new Div(new H2("Nenhum veículo encontrado"),
            new Paragraph("Ajuste os filtros ou cadastre um veículo. Para o primeiro cadastro, adicione uma marca e um modelo."));

    public VeiculosView(VeiculoService veiculos, CatalogoService catalogo, Validator validator) {
        this.veiculos = veiculos;
        this.catalogo = catalogo;
        this.validator = validator;
        addClassName("page");
        add(UiSupport.cabecalho("GESTÃO DE ESTOQUE", "Estoque de veículos",
                "Consulte o estoque e mantenha as informações dos veículos atualizadas.",
                UiSupport.principal("Novo veículo", () -> abrirFormulario(null))));
        configurarFiltros();
        configurarTabela();
        quantidade.addClassName("list-count");
        Span instrucao = new Span("Edite para atualizar preço, quilometragem e disponibilidade.");
        instrucao.addClassName("list-hint");
        Div listHeader = new Div(quantidade, instrucao);
        listHeader.addClassName("list-header");
        vazio.addClassName("empty-state");
        Div lista = new Div(listHeader, tabela, vazio);
        lista.addClassName("panel");
        add(lista);
        atualizar();
    }

    private void configurarFiltros() {
        marca.setItemLabelGenerator(MarcaSaida::nome);
        modelo.setItemLabelGenerator(ModeloSaida::nome);
        status.setItems(StatusVeiculo.values());
        status.setItemLabelGenerator(UiSupport::status);
        tipo.setItems(TipoVeiculo.values());
        tipo.setItemLabelGenerator(UiSupport::tipo);
        marca.setPlaceholder("Todas as marcas");
        modelo.setPlaceholder("Todos os modelos");
        status.setPlaceholder("Todos os status");
        tipo.setPlaceholder("Todos os tipos");
        for (var campo : new ComboBox<?>[]{marca, modelo, status, tipo}) campo.setClearButtonVisible(true);
        ano.setClearButtonVisible(true);
        ano.setPlaceholder("Todos os anos");
        precoMin.setPrefixComponent(new Span("R$"));
        precoMax.setPrefixComponent(new Span("R$"));
        precoMin.setLocale(java.util.Locale.forLanguageTag("pt-BR"));
        precoMax.setLocale(java.util.Locale.forLanguageTag("pt-BR"));
        precoMin.setPlaceholder("Sem mínimo");
        precoMax.setPlaceholder("Sem máximo");
        precoMin.setClearButtonVisible(true);
        precoMax.setClearButtonVisible(true);
        UiSupport.executar(erro, () -> {
            var modelos = catalogo.listarModelos();
            marca.setItems(catalogo.listarMarcas());
            modelo.setItems(modelos);
            marca.addValueChangeListener(e -> {
                modelo.clear();
                modelo.setItems(modelos.stream().filter(m -> e.getValue() == null
                        || m.marca().id().equals(e.getValue().id())).toList());
            });
        });
        Div campos = new Div(marca, modelo, precoMin, precoMax, ano, status, tipo);
        campos.addClassName("filter-fields");
        Button filtrar = UiSupport.principal("Filtrar", this::atualizar);
        Button limpar = new Button("Limpar filtros", e -> {
            marca.clear(); modelo.clear(); precoMin.clear(); precoMax.clear(); ano.clear(); status.clear(); tipo.clear();
            atualizar();
        });
        Div acoes = new Div(filtrar, limpar);
        acoes.addClassName("filter-actions");
        Div filtros = new Div(new H2("Filtrar veículos"), campos, acoes, erro);
        filtros.addClassNames("panel", "filter-panel");
        add(filtros);
    }

    private void configurarTabela() {
        tabela.addComponentColumn(v -> {
            Span nome = new Span(v.marca().nome() + " " + v.modelo().nome());
            nome.addClassName("vehicle-name");
            Span detalhe = new Span(UiSupport.tipo(v.tipo()) + " · Código " + v.id());
            Div cell = new Div(nome, detalhe);
            cell.addClassName("vehicle-cell");
            return cell;
        }).setHeader("Veículo").setWidth("220px").setFlexGrow(1);
        tabela.addColumn(VeiculoSaida::anoFabricacao).setHeader("Ano").setWidth("85px").setFlexGrow(0);
        tabela.addColumn(VeiculoSaida::cor).setHeader("Cor").setWidth("95px").setFlexGrow(0);
        tabela.addColumn(v -> UiSupport.moeda(v.preco())).setHeader("Preço").setWidth("150px").setFlexGrow(0);
        tabela.addColumn(v -> UiSupport.numero(v.quilometragem()) + " km").setHeader("Quilometragem").setWidth("145px").setFlexGrow(0);
        tabela.addComponentColumn(v -> UiSupport.badge(v.status())).setHeader("Status").setWidth("150px").setFlexGrow(0);
        tabela.addComponentColumn(v -> {
            Button editar = new Button("Editar", e -> abrirFormulario(v));
            editar.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editar.setAriaLabel("Editar " + v.modelo().nome() + " código " + v.id());
            Button excluir = new Button("Excluir", e -> confirmarExclusao(v));
            excluir.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            excluir.setAriaLabel("Excluir " + v.modelo().nome() + " código " + v.id());
            excluir.setEnabled(v.status() != StatusVeiculo.DISPONIVEL);
            if (!excluir.isEnabled()) excluir.setTooltipText("Para excluir, altere o status para vendido ou descontinuado.");
            Div acoes = new Div(editar, excluir);
            acoes.addClassName("row-actions");
            return acoes;
        }).setHeader("Ações").setWidth("180px").setFlexGrow(0);
        tabela.setAllRowsVisible(true);
        tabela.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private void atualizar() {
        UiSupport.executar(erro, () -> {
            if (precoMin.isInvalid() || precoMax.isInvalid() || ano.isInvalid())
                throw new IllegalArgumentException("Confira os valores numéricos dos filtros.");
            var resultado = veiculos.listar(marca.getValue() == null ? null : marca.getValue().id(),
                    modelo.getValue() == null ? null : modelo.getValue().id(), precoMin.getValue(), precoMax.getValue(),
                    ano.getValue(), status.getValue(), tipo.getValue());
            tabela.setItems(resultado);
            tabela.setVisible(!resultado.isEmpty());
            vazio.setVisible(resultado.isEmpty());
            quantidade.setText(resultado.size() + (resultado.size() == 1 ? " veículo encontrado" : " veículos encontrados"));
        });
    }

    private void abrirFormulario(VeiculoSaida veiculo) {
        new VeiculoDialog(veiculos, catalogo, validator, veiculo, this::atualizar).open();
    }

    private void confirmarExclusao(VeiculoSaida veiculo) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Excluir veículo?");
        dialog.setText("O veículo " + veiculo.marca().nome() + " " + veiculo.modelo().nome()
                + " (código " + veiculo.id() + ") será removido do estoque. Esta ação não pode ser desfeita.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Excluir veículo");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            if (UiSupport.executar(erro, () -> veiculos.remover(veiculo.id()))) {
                atualizar();
                UiSupport.sucesso("Veículo excluído.");
            }
        });
        dialog.open();
    }
}
