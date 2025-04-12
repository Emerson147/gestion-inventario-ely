package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "almacenes")
public class Almacen {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nombre;

  private String ubicacion;

  private String descripcion;

  @OneToMany(mappedBy = "almacen", cascade = CascadeType.ALL)
  private Set<Inventario> inventarios = new HashSet<>();
}