package com.emersondev.domain.repository;

import com.emersondev.domain.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

  Page<MovimientoInventario> findByInventarioId(Long inventarioId, Pageable pageable);

  List<MovimientoInventario> findByInventarioId(Long inventarioId);

  @Query("SELECT m FROM MovimientoInventario m WHERE " +
          "(:inventarioId IS NULL OR m.inventario.id = :inventarioId) AND " +
          "(:productoId IS NULL OR m.inventario.producto.id = :productoId) AND " +
          "(:colorId IS NULL OR m.inventario.color.id = :colorId) AND " +
          "(:tallaId IS NULL OR m.inventario.talla.id = :tallaId) AND " +
          "(:tipo IS NULL OR m.tipo = :tipo) AND " +
          "(COALESCE(:fechaInicio, NULL) IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
          "(COALESCE(:fechaFin, NULL) IS NULL OR m.fechaMovimiento <= :fechaFin)")
  Page<MovimientoInventario> buscarMovimientos(
          @Param("inventarioId") Long inventarioId,
          @Param("productoId") Long productoId,
          @Param("colorId") Long colorId,
          @Param("tallaId") Long tallaId,
          @Param("tipo") MovimientoInventario.TipoMovimiento tipo,
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin,
          Pageable pageable);

  @Query("SELECT COUNT(m) FROM MovimientoInventario m WHERE m.inventario.id = :inventarioId")
  Long countByInventarioId(@Param("inventarioId") Long inventarioId);
}
