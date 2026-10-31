package com.unifecaf.automotivo.view;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.service.CatalogoService;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.validation.Validator;
import java.util.Map;

@Route(value = "modelos", layout = MainLayout.class)
@PageTitle("Modelos | Sistema Automotivo")
public class ModelosView extends Div {
    private final CatalogoService catalogo;
    private final Grid<ModeloSaida> tabela = new Grid<>(ModeloSaida.class, false);
    private final Span quantidade = new Span();
    private final Span erro = UiSupport.erro();

    public ModelosView(CatalogoService catalogo, Validator validator) {
        this.catalogo = catalogo;
        addClassName("page");
        add(UiSupport.cabecalho("CADASTROS", "Modelos", "Relacione cada modelo à sua fabricante.", null));
        ComboBox<MarcaSaida> marca = new ComboBox<>("Marca");
        marca.setItemLabelGenerator(MarcaSaida::nome);
        marca.setPlaceholder("Selecione a marca");
        marca.setRequiredIndicatorVisible(true);
        TextField nome = new TextField("Nome do modelo");
        nome.setMaxLength(100);
        nome.setPlaceholder("Ex.: Corolla");
        nome.setRequiredIndicatorVisible(true);
        Button salvar = UiSupport.principal("Cadastrar modelo", () -> {
            ModeloEntrada dados = new ModeloEntrada(nome.getValue(), marca.getValue() == null ? null : marca.getValue().id());
            if (!UiSupport.validar(validator, dados, Map.of("nome", nome, "marcaId", marca))) return;
            UiSupport.executar(erro, () -> {
                catalogo.cadastrarModelo(dados);
                nome.clear();
                atualizar();
                UiSupport.sucesso("Modelo cadastrado.");
            });
        });
        Div form = new Div(marca, nome, salvar);
        form.addClassName("catalog-form");
        Paragraph ajuda = new Paragraph("Cadastre primeiro a marca. Depois, associe a ela o nome do modelo.");
        Div cadastro = new Div(new H2("Novo modelo"), ajuda, form, erro);
        cadastro.addClassName("panel");
        tabela.addColumn(ModeloSaida::id).setHeader("Código").setWidth("110px").setFlexGrow(0);
        tabela.addColumn(m -> m.marca().nome()).setHeader("Marca").setSortable(true);
        tabela.addColumn(ModeloSaida::nome).setHeader("Modelo").setSortable(true);
        tabela.setAllRowsVisible(true);
        tabela.setEmptyStateText("Nenhum modelo cadastrado. Selecione uma marca e informe o nome do modelo.");
        quantidade.addClassName("list-count");
        Div lista = new Div(quantidade, tabela);
        lista.addClassName("panel");
        add(cadastro, lista);
        UiSupport.executar(erro, () -> {
            var marcas = catalogo.listarMarcas();
            marca.setItems(marcas);
            if (marcas.isEmpty()) ajuda.setText("Cadastre uma marca na tela Marcas antes de adicionar um modelo.");
        });
        atualizar();
    }

    private void atualizar() {
        UiSupport.executar(erro, () -> {
            var modelos = catalogo.listarModelos();
            tabela.setItems(modelos);
            quantidade.setText(modelos.size() + (modelos.size() == 1 ? " modelo cadastrado" : " modelos cadastrados"));
        });
    }
}
