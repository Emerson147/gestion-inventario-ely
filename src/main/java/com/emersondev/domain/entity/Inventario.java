package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventarios", indexes = {
    @Index(name = "idx_inventario_serie", columnList = "serie"),
    @Index(name = "idx_inventario_producto", columnList = "producto_id"),
    @Index(name = "idx_inventario_color", columnList = "color_id"),
    @Index(name = "idx_inventario_talla", columnList = "talla_id"),
    @Index(name = "idx_inventario_almacen", columnList = "almacen_id"),
    @Index(name = "idx_inventario_estado", columnList = "estado"),
    @Index(name = "idx_inventario_cantidad", columnList = "cantidad"),
    @Index(name = "idx_inventario_producto_color_talla", columnList = "producto_id,color_id,talla_id")
})
public class Inventario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String serie;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_id", nullable = false)
  private Color color;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "talla_id", nullable = false)
  private Talla talla;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "almacen_id", nullable = false)
  private Almacen almacen;

  @Column(nullable = false)
  private Integer cantidad = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoInventario estado;

  @OneToMany(mappedBy = "inventario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private Set<MovimientoInventario> movimientos = new HashSet<>();

  public enum EstadoInventario {
    DISPONIBLE,
    AGOTADO,
    BAJO_STOCK,
    RESERVADO;

  }

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;

  private static final int UMBRAL_BAJO_STOCK = 4;

  // Metodo para actualizar estado basado en la cantidad
  public void actualizarEstado() {
    if (cantidad == 0) {
      estado = EstadoInventario.AGOTADO;
    } else if (cantidad <= UMBRAL_BAJO_STOCK) {
      estado = EstadoInventario.BAJO_STOCK;
    } else {
      estado = EstadoInventario.DISPONIBLE;
    }
  }

  // Método para agregar un movimiento
  public void agregarMovimiento(MovimientoInventario movimiento) {
    movimientos.add(movimiento);
    movimiento.setInventario(this);
  }

}