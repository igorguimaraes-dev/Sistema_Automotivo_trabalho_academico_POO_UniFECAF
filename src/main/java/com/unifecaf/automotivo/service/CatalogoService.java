package com.unifecaf.automotivo.service;

import static com.unifecaf.automotivo.dto.Dados.*;
import static org.springframework.http.HttpStatus.*;
import com.unifecaf.automotivo.model.*;
import com.unifecaf.automotivo.repository.*;
import com.unifecaf.automotivo.exception.RegraException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogoService {
    private final MarcaRepository marcas;
    private final ModeloRepository modelos;

    public CatalogoService(MarcaRepository marcas, ModeloRepository modelos) {
        this.marcas = marcas;
        this.modelos = modelos;
    }

    @Transactional
    public MarcaSaida cadastrarMarca(MarcaEntrada dados) {
        if (marcas.existsByNomeIgnoreCase(dados.nome().strip()))
            throw new RegraException(CONFLICT, "Marca já cadastrada.");
        return marcaSaida(marcas.save(new Marca(dados.nome())));
    }

    public List<MarcaSaida> listarMarcas() { return marcas.findAll().stream().map(CatalogoService::marcaSaida).toList(); }

    @Transactional
    public ModeloSaida cadastrarModelo(ModeloEntrada dados) {
        Marca marca = marcas.findById(dados.marcaId())
                .orElseThrow(() -> new RegraException(NOT_FOUND, "Marca não encontrada."));
        if (modelos.existsByNomeIgnoreCaseAndMarcaId(dados.nome().strip(), marca.getId()))
            throw new RegraException(CONFLICT, "Modelo já cadastrado para essa marca.");
        return modeloSaida(modelos.save(new Modelo(dados.nome(), marca)));
    }

    public List<ModeloSaida> listarModelos() { return modelos.findAll().stream().map(CatalogoService::modeloSaida).toList(); }
    static MarcaSaida marcaSaida(Marca m) { return new MarcaSaida(m.getId(), m.getNome()); }
    static ModeloSaida modeloSaida(Modelo m) { return new ModeloSaida(m.getId(), m.getNome(), marcaSaida(m.getMarca())); }
}
