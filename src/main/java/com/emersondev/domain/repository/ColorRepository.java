package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Color;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

  List<Color> findByProductoId(Long productoId);

  Optional<Color> findByNombreAndProductoId(String nombre, Long productoId);

  boolean existsByNombreAndProductoId(String nombre, Long productoId);

}
