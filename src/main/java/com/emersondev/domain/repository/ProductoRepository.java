package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
  /** Aquí puedes agregar métodos personalizados si es necesario
  // Por ejemplo, para buscar productos por nombre o categoría
  // List<Producto> findByNombreContaining(String nombre);
  // List<Producto> findByCategoriaId(Long categoriaId);
  **/

  Optional<Producto> findByCodigo(String codigo);

  Page<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrMarcaContainingIgnoreCase(
          String nombre, String descripcion, String marca, Pageable pageable);

  List<Producto> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
          String nombre, String descripcion);

  @Query("SELECT p FROM Producto p JOIN p.colores c JOIN c.tallas t JOIN Inventario i ON t.id = i.talla.id " +
          "WHERE i.serie = :serie")
  List<Producto> findByInventariosSerie(@Param("serie") String serie);

  @Query("SELECT DISTINCT p FROM Producto p JOIN p.colores c JOIN c.tallas t JOIN Inventario i " +
          "ON p.id = i.producto.id AND c.id = i.color.id AND t.id = i.talla.id " +
          "WHERE i.cantidad <= :umbral")
  List<Producto> findProductosWithLowStock(@Param("umbral") Integer umbral);
}
