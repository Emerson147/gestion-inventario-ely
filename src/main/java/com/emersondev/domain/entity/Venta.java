package com.emersondev.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "ventas")
@AllArgsConstructor
@RequiredArgsConstructor
public class Venta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 20)
  private String numeroVenta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Clientes cliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false)
  private BigDecimal subtotal;

  @Column(nullable = false)
  private BigDecimal igv;

  @Column(nullable = false)
  private BigDecimal total;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private EstadoVenta estado = EstadoVenta.PENDIENTE;

  @Column(length = 20)
  @Enumerated(EnumType.STRING)
  private TipoComprobante tipoComprobante;

  @Column(length = 20)
  private String serieComprobante;

  @Column(length = 20)
  private String numeroComprobante;

  @Column(length = 500)
  private String observaciones;

  @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DetalleVenta> detalles = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;

  public enum EstadoVenta {
    PENDIENTE, COMPLETADA, ANULADA
  }

  public enum TipoComprobante {
    BOLETA, FACTURA, NOTA_VENTA, TICKET
  }

  /**
   * Agrega un detalle a la venta
   */
  public void addDetalle(DetalleVenta detalle) {
    detalles.add(detalle);
    detalle.setVenta(this);
  }

  /**
   * Elimina un detalle de la venta
   */
  public void removeDetalle(DetalleVenta detalle) {
    detalles.remove(detalle);
    detalle.setVenta(null);
  }
}