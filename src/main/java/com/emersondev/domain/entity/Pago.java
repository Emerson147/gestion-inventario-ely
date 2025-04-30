package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class Pago {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String numeroPago;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "venta_id", nullable = false)
  private Venta venta;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal monto;

  @Column(nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private MetodoPago metodoPago;

  @Column(nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private EstadoPago estado = EstadoPago.CONFIRMADO;

  @Column(length = 100)
  private String numeroReferencia;

  @Column(length = 100)
  private String nombreTarjeta;

  @Column(length = 20)
  private String ultimos4Digitos;

  @Column(length = 500)
  private String observaciones;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;

  public enum MetodoPago {
    EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, YAPE, PLIN, OTROS
  }

  public enum EstadoPago {
    PENDIENTE, CONFIRMADO, RECHAZADO, REEMBOLSADO
  }

  @PrePersist
  @PreUpdate
  public void prePersistUpdate() {
    // Validaciones adicionales
    if (this.metodoPago == MetodoPago.TARJETA_CREDITO || this.metodoPago == MetodoPago.TARJETA_DEBITO) {
      if (this.ultimos4Digitos == null || this.ultimos4Digitos.isBlank()) {
        throw new IllegalStateException("Para pagos con tarjeta se requieren los últimos 4 dígitos");
      }
    }
  }
}


