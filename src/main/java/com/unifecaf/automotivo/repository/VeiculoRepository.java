package com.unifecaf.automotivo.repository;

import com.unifecaf.automotivo.model.Veiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {
    @Override
    @EntityGraph(attributePaths = {"modelo", "modelo.marca"})
    List<Veiculo> findAll(Specification<Veiculo> specification);

    @Override
    @EntityGraph(attributePaths = {"modelo", "modelo.marca"})
    Optional<Veiculo> findById(Long id);
}
