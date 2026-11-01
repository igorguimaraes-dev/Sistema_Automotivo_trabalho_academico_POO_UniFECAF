package com.unifecaf.automotivo.controller;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.model.*;
import com.unifecaf.automotivo.service.VeiculoService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veiculos")
public class VeiculoController {
    private final VeiculoService service;
    public VeiculoController(VeiculoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<VeiculoSaida> cadastrar(@Valid @RequestBody VeiculoEntrada dados) {
        VeiculoSaida veiculo = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/veiculos/" + veiculo.id())).body(veiculo);
    }

    @GetMapping
    public List<VeiculoSaida> listar(@RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Long modeloId, @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax, @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) StatusVeiculo status, @RequestParam(required = false) TipoVeiculo tipo) {
        return service.listar(marcaId, modeloId, precoMin, precoMax, ano, status, tipo);
    }

    @GetMapping("/{id}")
    public VeiculoSaida consultar(@PathVariable Long id) { return service.consultar(id); }

    @PatchMapping("/{id}")
    public VeiculoSaida atualizar(@PathVariable Long id, @Valid @RequestBody AtualizacaoVeiculo dados) {
        return service.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public VeiculoSaida atualizarCompleto(@PathVariable Long id, @Valid @RequestBody VeiculoEntrada dados) {
        return service.atualizarCompleto(id, dados);
    }
}
