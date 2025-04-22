package com.emersondev.util;

import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Factura;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.entity.Talla;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilidad para generar códigos y números de serie para distintas entidades del sistema
 */

@Component
@Slf4j
public class SerieGenerator {

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int CODIGO_PRODUCTO_LENGTH = 8;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  // Contadores para generación de secuencias
  private final AtomicInteger ventaCounter = new AtomicInteger(1);
  private final AtomicInteger facturaCounter = new AtomicInteger(1);
  private final AtomicInteger boletaCounter = new AtomicInteger(1);

  /**
   * Genera un código único para un producto
   * @return código alfanumérico de producto
   */
  public String generarCodigoProducto() {
    StringBuilder sb = new StringBuilder();
    sb.append("ZPT"); // Prefijo para zapatos

    // Agregar 5 caracteres aleatorios
    for (int i = 0; i < CODIGO_PRODUCTO_LENGTH - 3; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }

    return sb.toString();
  }

  /**
   * Genera una serie para un item de inventario
   * @param producto el producto
   * @param color el color
   * @param talla la talla
   * @return serie única para el inventario
   */
  public String generarSerieInventario(Producto producto, Color color, Talla talla) {
    StringBuilder sb = new StringBuilder();

    // Usar primera letra del nombre o código del producto
    if (producto != null && producto.getCodigo() != null) {
      sb.append(producto.getCodigo().substring(0, 3));
    } else {
      sb.append("PRD");
    }

    // Agregar primera letra del color
    if (color != null && color.getNombre() != null && !color.getNombre().isEmpty()) {
      sb.append(color.getNombre().substring(0, 1).toUpperCase());
    } else {
      sb.append("X");
    }

    // Agregar la talla
    if (talla != null && talla.getNumero() != null) {
      sb.append(talla.getNumero());
    } else {
      sb.append("00");
    }

    // Agregar fecha actual en formato YYYYMMDD
    sb.append(LocalDateTime.now().format(DATE_FORMATTER));

    // Agregar 3 caracteres aleatorios
    for (int i = 0; i < 3; i++) {
      sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
    }

    return sb.toString();
  }

  /**
   * Genera un número único para una venta
   * @return número de venta
   */
  public String generarNumeroVenta() {
    String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    int sequence = ventaCounter.getAndIncrement();

    return String.format("V-%s-%04d", fecha, sequence);
  }

  /**
   * Genera un número único para una factura
   * @param tipoComprobante el tipo de comprobante
   * @return número de factura o boleta
   */
  public String generarNumeroFactura(Factura.TipoComprobante tipoComprobante) {
    String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefijo;
    int sequence;

    if (tipoComprobante == Factura.TipoComprobante.FACTURA) {
      prefijo = "F";
      sequence = facturaCounter.getAndIncrement();
    } else {
      prefijo = "B";
      sequence = boletaCounter.getAndIncrement();
    }

    return String.format("%s-%s-%04d", prefijo, fecha, sequence);
  }

  /**
   * Reinicia los contadores (útil para pruebas o cuando se reinicia el servidor)
   */
  public void resetCounters() {
    ventaCounter.set(1);
    facturaCounter.set(1);
    boletaCounter.set(1);
    log.info("Se han reiniciado los contadores de secuencia");
  }

  /**
   * Establece valores específicos para los contadores (útil para recuperación o migración)
   * @param ventaSeq secuencia de venta
   * @param facturaSeq secuencia de factura
   * @param boletaSeq secuencia de boleta
   */
  public void setCounters(int ventaSeq, int facturaSeq, int boletaSeq) {
    ventaCounter.set(ventaSeq);
    facturaCounter.set(facturaSeq);
    boletaCounter.set(boletaSeq);
    log.info("Se han establecido los contadores de secuencia: venta={}, factura={}, boleta={}",
            ventaSeq, facturaSeq, boletaSeq);
  }

  // En SerieGenerator
  public String generarSerieInventarioSimplificado(Producto producto) {
    return "INV-" + producto.getCodigo();
  }
}
