package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inventarios")
public class Inventario {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
  private Integer cantidad;

  // Serie única para cada zapato en inventario
  @Column(unique = true, nullable = false)
  private String serie;

  private String ubicacionExacta; // Ubicación dentro del almacén
}