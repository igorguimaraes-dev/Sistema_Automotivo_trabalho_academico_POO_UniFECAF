package com.unifecaf.automotivo.controller;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.service.CatalogoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modelos")
public class ModeloController {
    private final CatalogoService service;
    public ModeloController(CatalogoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<ModeloSaida> cadastrar(@Valid @RequestBody ModeloEntrada dados) {
        ModeloSaida modelo = service.cadastrarModelo(dados);
        return ResponseEntity.created(URI.create("/api/modelos")).body(modelo);
    }

    @GetMapping
    public List<ModeloSaida> listar() { return service.listarModelos(); }
}
