package com.unifecaf.automotivo.view;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.service.CatalogoService;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import jakarta.validation.Validator;
import java.util.Map;

@Route(value = "marcas", layout = MainLayout.class)
@PageTitle("Marcas | Sistema Automotivo")
public class MarcasView extends Div {
    private final CatalogoService catalogo;
    private final Grid<MarcaSaida> tabela = new Grid<>(MarcaSaida.class, false);
    private final Span quantidade = new Span();
    private final Span erro = UiSupport.erro();

    public MarcasView(CatalogoService catalogo, Validator validator) {
        this.catalogo = catalogo;
        addClassName("page");
        add(UiSupport.cabecalho("CADASTROS", "Marcas", "Cadastre as fabricantes dos veículos do estoque.", null));

        TextField nome = new TextField("Nome da marca");
        nome.setMaxLength(100);
        nome.setPlaceholder("Ex.: Toyota");
        nome.setRequiredIndicatorVisible(true);
        Button salvar = UiSupport.principal("Cadastrar marca", () -> {
            MarcaEntrada dados = new MarcaEntrada(nome.getValue());
            if (!UiSupport.validar(validator, dados, Map.of("nome", nome))) return;
            UiSupport.executar(erro, () -> {
                catalogo.cadastrarMarca(dados);
                nome.clear();
                atualizar();
                UiSupport.sucesso("Marca cadastrada.");
            });
        });
        Div form = new Div(nome, salvar);
        form.addClassName("catalog-form");
        Div cadastro = new Div(new H2("Nova marca"), new Paragraph("A marca será utilizada no cadastro dos modelos."), form, erro);
        cadastro.addClassName("panel");

        tabela.addColumn(MarcaSaida::id).setHeader("Código").setWidth("110px").setFlexGrow(0);
        tabela.addColumn(MarcaSaida::nome).setHeader("Marca").setSortable(true);
        tabela.setAllRowsVisible(true);
        tabela.setEmptyStateText("Nenhuma marca cadastrada. Utilize o formulário acima para começar.");
        Div lista = new Div(quantidade, tabela);
        lista.addClassName("panel");
        quantidade.addClassName("list-count");
        add(cadastro, lista);
        atualizar();
    }

    private void atualizar() {
        UiSupport.executar(erro, () -> {
            var marcas = catalogo.listarMarcas();
            tabela.setItems(marcas);
            quantidade.setText(marcas.size() + (marcas.size() == 1 ? " marca cadastrada" : " marcas cadastradas"));
        });
    }
}
