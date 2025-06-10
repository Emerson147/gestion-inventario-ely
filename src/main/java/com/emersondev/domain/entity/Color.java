package com.emersondev.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"producto", "tallas"})
@EqualsAndHashCode(exclude = {"producto", "tallas"})
@Entity
@Table(name = "colores")
public class Color {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String nombre;

  @Column(name = "codigo_hex", length = 7)
  private String codigoHex;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "producto_id", nullable = false)
  @JsonBackReference
  private Producto producto;

  @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private Set<Talla> tallas = new HashSet<>();
}