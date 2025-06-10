package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "detalles_venta")
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venta_id", nullable = false)
  private Venta venta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_id", nullable = false)
  private Color color;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "talla_id", nullable = false)
  private Talla talla;

  @Column(nullable = false)
  private Integer cantidad;

  @Column(nullable = false)
  private BigDecimal precioUnitario;

  @Column(nullable = false)
  private BigDecimal subtotal;

  @Column(length = 255)
  private String descripcionProducto;

  /**
   * Calcula el subtotal
   */
  @PrePersist
  @PreUpdate
  public void calcularSubtotal() {
    if (this.cantidad != null && this.precioUnitario != null) {
      this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
    }
  }

  /**
   * Establece la descripción del producto
   */
  public void setProductDescription() {
    if (producto != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(producto.getNombre());

      if (color != null) {
        sb.append(" - ").append(color.getNombre());
      }

      if (talla != null) {
        sb.append(" - Talla ").append(talla.getNumero());
      }

      this.descripcionProducto = sb.toString();
    }
  }

  public void setPrecioUnitario(BigDecimal precioUnitario) {
    if (precioUnitario.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
    }
    this.precioUnitario = precioUnitario;
  }

  public void setCantidad(Integer cantidad) {
    if (cantidad <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }
    this.cantidad = cantidad;
  }
}