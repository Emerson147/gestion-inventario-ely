package com.emersondev.domain.repository;

import com.emersondev.domain.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
  List<DetalleVenta> findByVentaId(Long ventaId);

  @Query("SELECT dv.inventario.producto.nombre, SUM(dv.cantidad) as cantidad " +
          "FROM DetalleVenta dv JOIN dv.venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "AND v.estado = 'COMPLETADA' " +
          "GROUP BY dv.inventario.producto.nombre " +
          "ORDER BY cantidad DESC")
  List<Object[]> findTopSellingProducts(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT SUM(dv.cantidad) FROM DetalleVenta dv JOIN dv.venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "AND v.estado = 'COMPLETADA'")
  Long countTotalProductsSOld(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);
}
