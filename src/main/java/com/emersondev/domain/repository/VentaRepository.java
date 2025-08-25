package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

  Optional<Venta> findByNumeroVenta(String numeroVenta);

  @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario WHERE DATE(v.fechaCreacion) = :fecha")
  List<Venta> findByFechaCreacion(@Param("fecha") LocalDate fecha);

  @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin")
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

@Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaCreacion BETWEEN :inicioHoy AND :finHoy")
  BigDecimal sumTotalByFechaCreacionBetween(
          @Param("inicioHoy") LocalDateTime inicioHoy,
          @Param("finHoy") LocalDateTime finHoy);

  boolean existsByClienteId(Long id);

  List<Venta> findByFechaCreacionBetweenAndEstado(LocalDateTime fechaInicio, LocalDateTime fechaFin, Venta.EstadoVenta estadoVenta);

  List<Venta> findByClienteIdAndEstado(Long clienteId, Venta.EstadoVenta estadoVenta);

  List<Venta> findByFechaCreacionBetweenAndEstadoNot(LocalDateTime inicio, LocalDateTime fin, Venta.EstadoVenta estadoVenta);

  @Query("SELECT COUNT(DISTINCT v.cliente.id) FROM Venta v " +
          "WHERE v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "AND v.estado != 'ANULADA' " +
          "AND NOT EXISTS (SELECT 1 FROM Venta v2 WHERE v2.cliente = v.cliente AND v2.fechaCreacion < :fechaInicio)")
  int countClientesNuevosEnPeriodo(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);

  @Query("SELECT v FROM Venta v WHERE v.fechaCreacion BETWEEN :inicio AND :fin AND v.usuario.id = :usuarioId AND v.estado = :estado")
  List<Venta> findByFechaCreacionBetweenAndUsuarioIdAndEstado(
          @Param("inicio") LocalDateTime inicio,
          @Param("fin") LocalDateTime fin,
          @Param("usuarioId") Long usuarioId,
          @Param("estado") Venta.EstadoVenta estado);

  @Query("SELECT v.numeroVenta FROM Venta v WHERE v.numeroVenta LIKE :prefijo ORDER BY v.numeroVenta DESC")
  String findUltimoNumeroVentaParaFecha(@Param("prefijo") String prefijo);

  Optional<Venta> findTopByNumeroVentaStartingWithOrderByNumeroVentaDesc(String prefijo);
  
  @Query("SELECT DISTINCT v FROM Venta v " +
         "LEFT JOIN FETCH v.cliente " +
         "LEFT JOIN FETCH v.usuario " +
         "LEFT JOIN FETCH v.detalles d " +
         "LEFT JOIN FETCH d.inventario i " +
         "LEFT JOIN FETCH i.producto " +
         "LEFT JOIN FETCH i.color " +
         "LEFT JOIN FETCH i.talla " +
         "WHERE v.id = :ventaId")
  Optional<Venta> findByIdWithFullDetails(@Param("ventaId") Long ventaId);
  
  @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.cliente LEFT JOIN FETCH v.usuario WHERE v.estado = :estado")
  List<Venta> findByEstadoWithDetails(@Param("estado") Venta.EstadoVenta estado);
}
