package com.emersondev.domain.repository;

import com.emersondev.domain.entity.DetalleVenta;
import com.emersondev.domain.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

  /**
   * Encuentra detalles por venta
   */
  List<DetalleVenta> findByVentaId(Long ventaId);

  /**
   * Encuentra detalles por producto
   */
  List<DetalleVenta> findByProductoId(Long productoId);

  /**
   * Encuentra detalles por color
   */
  List<DetalleVenta> findByColorId(Long colorId);

  /**
   * Encuentra detalles por talla
   */
  List<DetalleVenta> findByTallaId(Long tallaId);

  /**
   * Encuentra los productos más vendidos en un período
   */
  @Query("SELECT d.producto.id as productoId, d.producto.nombre as productoNombre, " +
          "SUM(d.cantidad) as cantidadVendida, SUM(d.subtotal) as totalVendido " +
          "FROM DetalleVenta d " +
          "JOIN d.venta v " +
          "WHERE v.estado = 'COMPLETADA' AND v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "GROUP BY d.producto.id, d.producto.nombre " +
          "ORDER BY cantidadVendida DESC")
  List<Map<String, Object>> findProductosMasVendidosEnPeriodo(
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

  /**
   * Encuentra las tallas más vendidas por producto
   */
  @Query("SELECT d.talla.numero as talla, SUM(d.cantidad) as cantidad " +
          "FROM DetalleVenta d " +
          "JOIN d.venta v " +
          "WHERE v.estado = 'COMPLETADA' AND d.producto.id = :productoId " +
          "AND v.fechaCreacion BETWEEN :fechaInicio AND :fechaFin " +
          "GROUP BY d.talla.numero " +
          "ORDER BY cantidad DESC")
  List<Map<String, Object>> findTallasMasVendidasPorProducto(
          @Param("productoId") Long productoId,
          @Param("fechaInicio") LocalDateTime fechaInicio,
          @Param("fechaFin") LocalDateTime fechaFin);

}
