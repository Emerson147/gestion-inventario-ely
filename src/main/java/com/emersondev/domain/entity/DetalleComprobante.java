package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Entity
public class DetalleComprobante {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "comprobante_id", nullable = false)
  private Comprobante comprobante;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "producto_id", nullable = false)
  private Producto producto;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_id", nullable = false, referencedColumnName = "id")
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

  @Column(nullable = false)
  private BigDecimal igv;

  @Column(nullable = false)
  private BigDecimal total;

  @Column(length = 250)
  private String descripcion;

  @Column(length = 50)
  private String unidadMedida = "UNIDAD";

  @Column(length = 20)
  private String codigoProducto;

  /**
   * Calcula los montos del detalle
   */
  @PrePersist
  @PreUpdate
  public void calcularMontos() {
    if (this.cantidad != null && this.precioUnitario != null) {
      // El precio unitario ya incluye IGV en nuestro caso
      this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
      this.igv = this.subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_EVEN);
      this.total = this.subtotal;
    }
  }

  /**
   * Establece la descripción del detalle
   */
  public void generarDescripcion() {
    if (producto != null) {
      StringBuilder sb = new StringBuilder();
      sb.append(producto.getNombre());

      if (color != null) {
        sb.append(" - ").append(color.getNombre());
      }

      if (talla != null) {
        sb.append(" - Talla ").append(talla.getNumero());
      }

      this.descripcion = sb.toString();
      this.codigoProducto = producto.getCodigo();
    }
  }
}
