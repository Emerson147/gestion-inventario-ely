package com.emersondev.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tallas")
public class Talla {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String numero; // Puede ser "34", "35", etc.

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_id", nullable = false)
  private Color color;
}