package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "facturas")
public class Factura {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String numeroFactura;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venta_id", nullable = false, unique = true)
  private Venta venta;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TipoComprobante tipoComprobante;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaEmision;

  public enum TipoComprobante {
    FACTURA, BOLETA
  }
}