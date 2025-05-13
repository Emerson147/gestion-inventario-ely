package com.emersondev.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"inventario", "inventarioDestino", "venta"})
@EqualsAndHashCode(exclude = {"inventario", "inventarioDestino", "venta"})
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Inventario de origen
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventario_id", nullable = false)
  @JsonBackReference
  private Inventario inventario;

  // Inventario de destino (solo para TRASLADO, puede ser null)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventario_destino_id")
  private Inventario inventarioDestino;

  @Column(nullable = false)
  private Integer cantidad;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoMovimiento tipo;

  @Column(nullable = false)
  private  String descripcion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venta_id")
  private Venta venta;

  @Column(nullable = false)
  private String referencia;

  @Column(name = "fecha_movimiento",nullable = false, updatable = false)
  private LocalDateTime fechaMovimiento;

  @Column(name = "usuario", length = 100)
  private String usuario;

  public enum TipoMovimiento {
    ENTRADA,
    SALIDA,
    AJUSTE,
    TRASLADO
  }

  @PrePersist
  private void onCreate() {
    this.fechaMovimiento = LocalDateTime.now();
  }
}