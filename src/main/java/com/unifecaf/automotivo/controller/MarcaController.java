package com.unifecaf.automotivo.controller;

import static com.unifecaf.automotivo.dto.Dados.*;
import com.unifecaf.automotivo.service.CatalogoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {
    private final CatalogoService service;
    public MarcaController(CatalogoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<MarcaSaida> cadastrar(@Valid @RequestBody MarcaEntrada dados) {
        MarcaSaida marca = service.cadastrarMarca(dados);
        return ResponseEntity.created(URI.create("/api/marcas")).body(marca);
    }

    @GetMapping
    public List<MarcaSaida> listar() { return service.listarMarcas(); }
}
