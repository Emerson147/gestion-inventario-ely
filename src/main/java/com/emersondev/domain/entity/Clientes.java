package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clientes")
public class Clientes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String nombres;

  @Column(nullable = false, length = 100)
  private String apellidos;

  @Column(length = 15)
  private String dni;

  @Column(length = 15)
  private String ruc;

  @Column(length = 15)
  private String telefono;

  @Column(length = 255)
  private String direccion;

  @Column(length = 100)
  private String email;

  @Column(columnDefinition = "BOOLEAN DEFAULT true")
  private Boolean estado = true;

  @OneToMany(mappedBy = "cliente")
  private List<Venta> ventas = new ArrayList<>();

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime fechaCreacion;

  @UpdateTimestamp
  private LocalDateTime fechaActualizacion;
}