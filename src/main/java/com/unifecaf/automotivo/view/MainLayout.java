package com.unifecaf.automotivo.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {
    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        Span monograma = new Span("SA");
        monograma.addClassName("brand-mark");
        Span titulo = new Span("Sistema Automotivo");
        titulo.addClassName("brand-title");
        Div nome = new Div(titulo, new Span("Gestão de estoque"));
        nome.addClassName("brand-name");
        Div marca = new Div(monograma, nome);
        marca.addClassName("brand");

        Nav navegacao = new Nav();
        navegacao.setAriaLabel("Navegação principal");
        navegacao.add(new RouterLink("Veículos", VeiculosView.class),
                new RouterLink("Marcas", MarcasView.class),
                new RouterLink("Modelos", ModelosView.class));
        navegacao.addClassName("main-nav");
        Div rodape = new Div(new Span("UniFECAF"), new Span("Object Oriented Programming"));
        rodape.addClassName("sidebar-footer");
        Div sidebar = new Div(marca, navegacao, rodape);
        sidebar.addClassName("sidebar");
        addToDrawer(sidebar);

        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Abrir ou fechar menu");
        Div topo = new Div(toggle, new Span("CONTROLE DE ESTOQUE"), new Span("Sistema Automotivo"));
        topo.addClassName("topbar");
        addToNavbar(topo);
    }
}
