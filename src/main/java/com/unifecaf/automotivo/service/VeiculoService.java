package com.unifecaf.automotivo.service;

import static com.unifecaf.automotivo.dto.Dados.*;
import static org.springframework.http.HttpStatus.*;
import com.unifecaf.automotivo.model.*;
import com.unifecaf.automotivo.repository.*;
import com.unifecaf.automotivo.exception.RegraException;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeiculoService {
    private final VeiculoRepository veiculos;
    private final ModeloRepository modelos;

    public VeiculoService(VeiculoRepository veiculos, ModeloRepository modelos) {
        this.veiculos = veiculos;
        this.modelos = modelos;
    }

    @Transactional
    public VeiculoSaida cadastrar(VeiculoEntrada dados) {
        Modelo modelo = modelos.findById(dados.modeloId())
                .orElseThrow(() -> new RegraException(NOT_FOUND, "Modelo não encontrado."));
        Veiculo veiculo = new Veiculo(modelo, dados.anoFabricacao(), dados.cor(), dados.preco(),
                dados.quilometragem(), dados.status(), dados.tipo());
        return saida(veiculos.save(veiculo));
    }

    public VeiculoSaida consultar(Long id) { return saida(buscar(id)); }

    public List<VeiculoSaida> listar(Long marcaId, Long modeloId, BigDecimal precoMin,
            BigDecimal precoMax, Integer ano, StatusVeiculo status, TipoVeiculo tipo) {
        if ((precoMin != null && precoMin.signum() < 0) || (precoMax != null && precoMax.signum() < 0))
            throw new IllegalArgumentException("Filtros de preço não podem ser negativos.");
        if (precoMin != null && precoMax != null && precoMin.compareTo(precoMax) > 0)
            throw new IllegalArgumentException("Preço mínimo não pode ser maior que o máximo.");
        Specification<Veiculo> filtro = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (marcaId != null) p.add(cb.equal(root.get("modelo").get("marca").get("id"), marcaId));
            if (modeloId != null) p.add(cb.equal(root.get("modelo").get("id"), modeloId));
            if (precoMin != null) p.add(cb.greaterThanOrEqualTo(root.get("preco"), precoMin));
            if (precoMax != null) p.add(cb.lessThanOrEqualTo(root.get("preco"), precoMax));
            if (ano != null) p.add(cb.equal(root.get("anoFabricacao"), ano));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (tipo != null) p.add(cb.equal(root.get("tipo"), tipo));
            query.orderBy(cb.asc(root.get("id")));
            return cb.and(p.toArray(Predicate[]::new));
        };
        return veiculos.findAll(filtro).stream().map(VeiculoService::saida).toList();
    }

    @Transactional
    public VeiculoSaida atualizar(Long id, AtualizacaoVeiculo dados) {
        Veiculo veiculo = buscar(id);
        veiculo.atualizarDadosComerciais(dados.preco(), dados.quilometragem(), dados.status());
        return saida(veiculos.save(veiculo));
    }

    @Transactional
    public void remover(Long id) {
        Veiculo veiculo = buscar(id);
        if (!veiculo.podeSerRemovido())
            throw new RegraException(CONFLICT, "Marque o veículo como vendido ou descontinuado antes de removê-lo.");
        veiculos.delete(veiculo);
    }

    private Veiculo buscar(Long id) {
        return veiculos.findById(id).orElseThrow(() -> new RegraException(NOT_FOUND, "Veículo não encontrado."));
    }

    private static VeiculoSaida saida(Veiculo v) {
        return new VeiculoSaida(v.getId(), CatalogoService.marcaSaida(v.getModelo().getMarca()),
                CatalogoService.modeloSaida(v.getModelo()), v.getAnoFabricacao(), v.getCor(),
                v.getPreco(), v.getQuilometragem(), v.getStatus(), v.getTipo());
    }
}
