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

  Optional<Inventario> findBySerie(String serie);

  @Query("SELECT i FROM Inventario i WHERE i.producto.id = :productoId AND i.color.id = :colorId " +
          "AND i.talla.id = :tallaId AND i.almacen.id = :almacenId")
  Optional<Inventario> findByProductoColorTallaAlmacen(
          @Param("productoId") Long productoId,
          @Param("colorId") Long colorId,
          @Param("tallaId") Long tallaId,
          @Param("almacenId") Long almacenId);

  List<Inventario> findByProductoIdAndCantidadGreaterThan(Long productoId, Integer cantidad);

  List<Inventario> findByProductoId(Long productoId);

  List<Inventario> findByAlmacenId(Long almacenId);

  List<Inventario> findByCantidadLessThanEqual(Integer umbral);

  @Query("SELECT SUM(i.cantidad) FROM Inventario i WHERE i.producto.id = :productoId")
  Integer sumCantidadByProductoId(@Param("productoId") Long productoId);

  @Query("SELECT SUM(i.cantidad) FROM Inventario i WHERE i.producto.id = :productoId " +
          "AND i.color.id = :colorId AND i.talla.id = :tallaId")
  Integer sumCantidadByProductoColorTalla(
          @Param("productoId") Long productoId,
          @Param("colorId") Long colorId,
          @Param("tallaId") Long tallaId);

}
