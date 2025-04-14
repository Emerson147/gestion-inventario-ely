package com.emersondev.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.Size;

 @Data
 @Builder
 @NoArgsConstructor
 @AllArgsConstructor
 public class ProductoRequest {

   /**
    * Código único del producto
    */
   @NotBlank(message = "El código es obligatorio")
   private String codigo;

   /**
    * Nombre del producto
    */
   @NotBlank(message = "El nombre es obligatorio")
   @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
   private String nombre;

   /**
    * Descripción del producto
    */
   @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
   private String descripcion;

   /**
    * Marca del producto
    */
   @NotBlank(message = "La marca es obligatoria")
   private String marca;

   /**
    * Modelo del producto
    */
   @NotBlank(message = "El modelo es obligatorio")
   private String modelo;

   /**
    * Precio de compra del producto
    */
   @NotNull(message = "El precio de compra es obligatorio")
   @Positive(message = "El precio de compra debe ser positivo")
   private BigDecimal precioCompra;

   /**
    * Precio de venta del producto
    */
   @NotNull(message = "El precio de venta es obligatorio")
   @Positive(message = "El precio de venta debe ser positivo")
   private BigDecimal precioVenta;

   /**
    * URL de la imagen del producto
    */
   private String imagen;

   /**
    * Colores disponibles para el producto
    */
   @Valid
   private List<ColorRequest> colores;
 }

