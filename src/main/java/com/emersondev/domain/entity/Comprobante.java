package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "comprobantes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tipo_documento", "serie", "numero"})
})
public class Comprobante {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private TipoDocumento tipoDocumento;

  @Column(nullable = false, length = 10)
  private String serie;

  @Column(nullable = false, length = 10)
  private String numero;

  @Column(nullable = false)
  private LocalDateTime fechaEmision;

  @Column(length = 40)
  private String codigoHash;

  @OneToOne
  @JoinColumn(name = "venta_id", nullable = false)
  private Venta venta;

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
  private EstadoComprobante estado = EstadoComprobante.EMITIDO;

  @Column(length = 500)
  private String observaciones;

  @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DetalleComprobante> detalles = new ArrayList<>();

  @Column(length = 100)
  private String rutaArchivoPdf;

  @Column(length = 100)
  private String rutaArchivoXml;

  @Column
  private LocalDateTime fechaAnulacion;

  @Column(length = 255)
  private String motivoAnulacion;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;

  public enum TipoDocumento {
    FACTURA, BOLETA, NOTA_VENTA, TICKET
  }

  public enum EstadoComprobante {
    EMITIDO, ANULADO, ENVIADO_SUNAT, ERROR_SUNAT
  }

  /**
   * Agrega un detalle al comprobante
   */
  public void addDetalle(DetalleComprobante detalle) {
    detalles.add(detalle);
    detalle.setComprobante(this);
  }

  /**
   * Elimina un detalle del comprobante
   */
  public void removeDetalle(DetalleComprobante detalle) {
    detalles.remove(detalle);
    detalle.setComprobante(null);
  }

  /**
   * Genera un código Hash simple para el comprobante
   */
  public void generarCodigoHash() {
    // En un sistema real, esto sería un algoritmo más complejo
    // que podría incluir firma digital según los requisitos fiscales
    String base = this.tipoDocumento + "-" + this.serie + "-" + this.numero + "-" + this.total;
    this.codigoHash = Integer.toHexString(base.hashCode()).toUpperCase();
  }
}
