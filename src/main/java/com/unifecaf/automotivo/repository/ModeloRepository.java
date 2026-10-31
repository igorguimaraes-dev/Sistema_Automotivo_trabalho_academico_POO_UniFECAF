package com.unifecaf.automotivo.repository;

import com.unifecaf.automotivo.model.Modelo;
import java.util.List;
import org.springframework.data.jpa.repository.*;

public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    boolean existsByNomeIgnoreCaseAndMarcaId(String nome, Long marcaId);
    @Override
    @EntityGraph(attributePaths = "marca")
    List<Modelo> findAll();
}
