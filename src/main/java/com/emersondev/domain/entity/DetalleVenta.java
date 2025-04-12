package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalles_venta")
public class DetalleVenta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venta_id", nullable = false)
  private Venta venta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventario_id", nullable = false)
  private Inventario inventario;

  @Column(nullable = false)
  private Integer cantidad;

  @Column(nullable = false)
  private BigDecimal precioUnitario;

  @Column(nullable = false)
  private BigDecimal subtotal;
}