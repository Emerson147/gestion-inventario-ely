package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

  /**
   * Busca un comprobante por serie y número
   */
  Optional<Comprobante> findBySerieAndNumero(String serie, String numero);

  /**
   * Busca comprobantes por tipo, serie y número
   */
  Optional<Comprobante> findByTipoDocumentoAndSerieAndNumero(
          Comprobante.TipoDocumento tipoDocumento, String serie, String numero);

  /**
   * Lista comprobantes por tipo
   */
  List<Comprobante> findByTipoDocumento(Comprobante.TipoDocumento tipoDocumento);

  /**
   * Lista comprobantes por estado
   */
  List<Comprobante> findByEstado(Comprobante.EstadoComprobante estado);

  /**
   * Lista comprobantes por cliente
   */
  List<Comprobante> findByClienteId(Long clienteId);

  /**
   * Lista comprobantes por venta
   */
  Optional<Comprobante> findByVentaId(Long ventaId);

  /**
   * Lista comprobantes por rango de fechas
   */
  List<Comprobante> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);

  /**
   * Lista comprobantes por tipo y rango de fechas
   */
  List<Comprobante> findByTipoDocumentoAndFechaEmisionBetween(
          Comprobante.TipoDocumento tipoDocumento, LocalDateTime inicio, LocalDateTime fin);

  /**
   * Obtiene el último número para una serie y tipo
   */
  @Query("SELECT MAX(c.numero) FROM Comprobante c WHERE c.tipoDocumento = :tipo AND c.serie = :serie")
  String findMaxNumeroByTipoAndSerie(
          @Param("tipo") Comprobante.TipoDocumento tipo,
          @Param("serie") String serie);

  /**
   * Cuenta comprobantes por tipo, estado y rango de fechas
   */
  @Query("SELECT COUNT(c) FROM Comprobante c WHERE " +
          "c.tipoDocumento = :tipo AND c.estado = :estado AND " +
          "c.fechaEmision BETWEEN :inicio AND :fin")
  Long countByTipoAndEstadoAndFechaEmisionBetween(
          @Param("tipo") Comprobante.TipoDocumento tipo,
          @Param("estado") Comprobante.EstadoComprobante estado,
          @Param("inicio") LocalDateTime inicio,
          @Param("fin") LocalDateTime fin);

  /**
   * Suma montos por tipo, estado y rango de fechas
   */
  @Query("SELECT SUM(c.total) FROM Comprobante c WHERE " +
          "c.tipoDocumento = :tipo AND c.estado = :estado AND " +
          "c.fechaEmision BETWEEN :inicio AND :fin")
  BigDecimal sumTotalByTipoAndEstadoAndFechaEmisionBetween(
          @Param("tipo") Comprobante.TipoDocumento tipo,
          @Param("estado") Comprobante.EstadoComprobante estado,
          @Param("inicio") LocalDateTime inicio,
          @Param("fin") LocalDateTime fin);

  // Ya lo tienes para el número correlativo, pero si necesitas cambiar la serie, añade métodos así:
  @Query("SELECT MAX(c.serie) FROM Comprobante c WHERE c.tipoDocumento = :tipo")
  String findMaxSerieByTipo(@Param("tipo") Comprobante.TipoDocumento tipo);

}
