package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

  Optional<Venta> findByNumeroVenta(String numeroVenta);

  @Query("SELECT v FROM Venta v WHERE DATE(v.fechaCreacion) = :fecha")
  List<Venta> findByFechaCreacion(@Param("fecha") LocalDate fecha);

  @Query("SELECT v FROM Venta v WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
  List<Venta> findByFechaCreacionBetween(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

  Page<Venta> findByClienteId(Long clienteId, Pageable pageable);

  List<Venta> findByClienteId(Long clienteId);

  List<Venta> findByUsuarioId(Long usuarioId);

  List<Venta> findByEstado(Venta.EstadoVenta estado);

  @Query("SELECT COUNT(v), SUM(v.total) FROM Venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND v.estado = 'COMPLETADA'")
  Object[] getVentasStats(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT FUNCTION('DATE_FORMAT', v.fechaCreacion, '%Y-%m-%d') as fecha, " +
          "COUNT(v) as cantidad, SUM(v.total) as total FROM Venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin AND v.estado = 'COMPLETADA' " +
          "GROUP BY FUNCTION('DATE_FORMAT', v.fechaCreacion, '%Y-%m-%d') " +
          "ORDER BY FUNCTION('DATE_FORMAT', v.fechaCreacion, '%Y-%m-%d')")
  List<Object[]> getVentasPorDia(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT COUNT(v), v.estado FROM Venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "GROUP BY v.estado")
  List<Object[]> countVentasByEstado(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);
}
