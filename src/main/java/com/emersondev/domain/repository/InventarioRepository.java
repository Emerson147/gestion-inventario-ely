package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {


  /**
   * Busca un inventario por ID de producto y almacén
   */
  @Query("SELECT i FROM Inventario i WHERE i.producto.id = :productoId AND i.almacen.id = :almacenId")
  Optional<Inventario> findByProductoAndAlmacen(
          @Param("productoId") Long productoId,
          @Param("almacenId") Long almacenId);

  /**
   * Busca un inventario por ID de producto y cantidad mayor a un valor
   */
  List<Inventario> findByProductoIdAndCantidadGreaterThan(Long productoId, Integer cantidad);

  /**
   * Busca un inventario por ID de producto, color, talla y almacén
   */
  @Query("SELECT i FROM Inventario i WHERE i.producto.id = :productoId AND i.color.id = :colorId " +
          "AND i.talla.id = :tallaId AND i.almacen.id = :almacenId")
  Optional<Inventario> findByProductoColorTallaAlmacen(
          @Param("productoId") Long productoId,
          @Param("colorId") Long colorId,
          @Param("tallaId") Long tallaId,
          @Param("almacenId") Long almacenId);

  /**
   * Busca inventarios por ID de producto y cantidad menor o igual a un valor
   */
  Long countByCantidadLessThanEqual(int i);

  /**
   * Verifica si existe un inventario para un color específico
   */
  boolean existsByColorId(Long colorId);

  /**
   * Verifica si existe un inventario para una talla específica
   */
  boolean existsByTallaId(Long tallaId);

  /**
   * Busca un inventario por su número de serie
   */
  Optional<Inventario> findBySerie(String serie);

  /**
   * Busca inventarios por ID de producto
   */
  List<Inventario> findByProductoId(Long productoId);

  /**
   * Busca inventarios por ID de almacén
   */
  List<Inventario> findByAlmacenId(Long almacenId);

  /**
   * Verifica si existe un inventario para la combinación producto/color/talla/almacén
   */
  boolean existsByProductoIdAndColorIdAndTallaIdAndAlmacenId(
          Long productoId, Long colorId, Long tallaId, Long almacenId);

  /**
   * Verifica si existe un inventario para la combinación excluyendo un ID específico
   */
  boolean existsByProductoIdAndColorIdAndTallaIdAndAlmacenIdAndIdNot(
          Long productoId, Long colorId, Long tallaId, Long almacenId, Long id);

  /**
   * Verifica si existe inventario para un almacén específico
   */
  boolean existsByAlmacenId(Long almacenId);

  /**
   * Verifica si existe inventario para un producto específico
   */
  boolean existsByProductoId(Long productoId);

  /**
   * Busca un inventario específico por producto/color/talla/almacén
   */
  Optional<Inventario> findByProductoIdAndColorIdAndTallaIdAndAlmacenId(
          Long productoId, Long colorId, Long tallaId, Long almacenId);

  /**
   * Busca inventarios con cantidad menor o igual a un umbral
   */
  List<Inventario> findByCantidadLessThanEqual(Integer umbral);

  /**
   * Busca inventarios para una variante específica ordenados por fecha de creación
   */
  List<Inventario> findByProductoIdAndColorIdAndTallaIdOrderByFechaCreacionAsc(
          Long productoId, Long colorId, Long tallaId);

  /**
   * Calcula el stock total para un producto
   */
  @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i WHERE i.producto.id = :productoId")
  Integer calcularStockTotalProducto(@Param("productoId") Long productoId);

  /**
   * Calcula el stock para una variante específica
   */
  @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i " +
          "WHERE i.producto.id = :productoId " +
          "AND i.color.id = :colorId " +
          "AND i.talla.id = :tallaId")
  Integer calcularStockPorVariante(
          @Param("productoId") Long productoId,
          @Param("colorId") Long colorId,
          @Param("tallaId") Long tallaId);

  /**
   * Encuentra productos con stock bajo
   */
  @Query("SELECT i.producto.id, i.producto.nombre, SUM(i.cantidad) as stock " +
          "FROM Inventario i " +
          "GROUP BY i.producto.id, i.producto.nombre " +
          "HAVING SUM(i.cantidad) <= :umbral")
  List<Long> findProductosConStockBajo(@Param("umbral") Integer umbral);
}
