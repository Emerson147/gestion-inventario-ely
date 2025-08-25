package com.emersondev.domain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"colores"})
@EqualsAndHashCode(exclude = {"colores"})
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_codigo", columnList = "codigo"),
    @Index(name = "idx_producto_marca", columnList = "marca"),
    @Index(name = "idx_producto_modelo", columnList = "modelo"),
    @Index(name = "idx_producto_nombre", columnList = "nombre")
})
public class Producto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 50)
  private String codigo;

  @Column(nullable = false)
  private String nombre;

  private String descripcion;

  @Column(nullable = false)
  private String marca;

  @Column(nullable = false)
  private String modelo;

  @Column(nullable = false)
  private BigDecimal precioCompra;

  @Column(nullable = false)
  private BigDecimal precioVenta;

  private String imagen;

  @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JsonManagedReference
  @BatchSize(size = 10)
  private Set<Color> colores = new HashSet<>();

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;
}