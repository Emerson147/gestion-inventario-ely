package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

  /**
   * Encuentra pagos por venta
   */
  List<Pago> findByVentaId(Long ventaId);

    /**
   * Encuentra pagos por método de pago
   */
  List<Pago> findByMetodoPago(Pago.MetodoPago metodoPago);

  /**
   * Encuentra pagos por estado
   */
  List<Pago> findByEstado(Pago.EstadoPago estado);

  /**
   * Suma los montos de pagos agrupados por método de pago en un rango de fechas
   */
  @Query("SELECT p.metodoPago as metodo, SUM(p.monto) as total FROM Pago p " +
          "WHERE p.estado = 'CONFIRMADO' AND p.fechaCreacion BETWEEN :inicio AND :fin " +
          "GROUP BY p.metodoPago")
  List<Map<String, Object>> totalPagosPorMetodo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

  /**
   * Suma total de pagos confirmados en un rango de fechas
   */
  @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estado = 'CONFIRMADO' AND p.fechaCreacion BETWEEN :inicio AND :fin")
  BigDecimal totalPagosConfirmados(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

  Optional<Pago> findByNumeroPago(String numeroPago);

  List<Pago> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

  List<Pago> findByUsuarioId(Long usuarioId);

  @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.venta.id = :ventaId AND p.estado = 'CONFIRMADO'")
  BigDecimal calcularTotalPagadoPorVenta(@Param("ventaId") Long ventaId);

  @Query("SELECT p.metodoPago as metodo, COUNT(p.id) as cantidad, SUM(p.monto) as total " +
          "FROM Pago p " +
          "WHERE p.estado = 'CONFIRMADO' AND p.fechaCreacion BETWEEN :inicio AND :fin " +
          "GROUP BY p.metodoPago")
  List<Map<String, Object>> obtenerEstadisticasPorMetodoPago(
          @Param("inicio") LocalDateTime inicio,
          @Param("fin") LocalDateTime fin);

  @Query("SELECT FUNCTION('DATE', p.fechaCreacion) as fecha, COUNT(p.id) as cantidad, SUM(p.monto) as total " +
          "FROM Pago p " +
          "WHERE p.estado = 'CONFIRMADO' AND p.fechaCreacion BETWEEN :inicio AND :fin " +
          "GROUP BY FUNCTION('DATE', p.fechaCreacion) " +
          "ORDER BY FUNCTION('DATE', p.fechaCreacion) ASC")
  List<Map<String, Object>> obtenerEstadisticasPorFecha(
          @Param("inicio") LocalDateTime inicio,
          @Param("fin") LocalDateTime fin);
}
