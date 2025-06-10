package com.emersondev.util;

import com.emersondev.domain.entity.*;
import com.emersondev.domain.repository.ComprobanteRepository;
import com.emersondev.domain.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utilidad para generar códigos y números de serie para distintas entidades del sistema
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class SerieGenerator {

  private final VentaRepository ventaRepository;
  private final ComprobanteRepository comprobanteRepository;

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
  public String generarCodigoProducto(Producto producto) {
    StringBuilder sb = new StringBuilder();

   // Usar 3 primeras letras del nombre o codigo del producto
    if (producto != null && producto.getNombre() != null) {
      sb.append(producto.getNombre().substring(0, 3).toUpperCase()).append("-");
    } else {
      sb.append("PRD-");
    }

    // Usar 3 primeras letras de las marca
    if (producto != null && producto.getMarca() != null) {
      sb.append(producto.getMarca().substring(0, 3).toUpperCase()).append("-");
    } else {
      sb.append("MAR-");
    }

    // Agregar 2 caracteres aleatorios
    for (int i = 0; i < 3; i++) {
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
      sb.append(producto.getCodigo().substring(0, 3).toUpperCase()).append("-");
    } else {
      sb.append("PRD-");
    }

    // Usar las 3 letra de la marca
    if (producto != null && producto.getMarca() != null ) {
      sb.append(producto.getMarca().substring(0, 3).toUpperCase()).append("-");
    } else {
      sb.append("MAR-");
    }

    // Agregar primera letra del color
    if (color != null && color.getNombre() != null && !color.getNombre().isEmpty()) {
      sb.append(color.getNombre().substring(0, 2).toUpperCase());
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
   * Genera un código único para pago
   * @return código único
   */
  public String generarNumeroPago(Long venta) {
    String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    StringBuilder sb = new StringBuilder();
    sb.append("PAG-").append(fecha).append("-").append(venta);

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
    String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String prefijo = "V-" + fecha + "-";

    // Usando el método sugerido arriba
    Optional<Venta> ultimaVenta = ventaRepository.findTopByNumeroVentaStartingWithOrderByNumeroVentaDesc(prefijo);

    int nextCorrelativo = 1;
    if (ultimaVenta.isPresent()) {
      String ultimoNumero = ultimaVenta.get().getNumeroVenta();
      String correlativoStr = ultimoNumero.substring(ultimoNumero.lastIndexOf("-") + 1);
      nextCorrelativo = Integer.parseInt(correlativoStr) + 1;
    }

    return String.format("%s%04d", prefijo, nextCorrelativo);
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

  /**
   * Obtiene la siguiente serie disponible para un tipo de comprobante.
   * Si la serie actual llegó al máximo correlativo (ejemplo: 99999999), pasa a la siguiente serie.
   */
  public String obtenerSiguienteSerie(Comprobante.TipoDocumento tipo, String serieActual) {
    // Opcional: define el máximo correlativo permitido por serie
    String maxCorrelativo = "99999999";
    String ultimoNumero = comprobanteRepository.findMaxNumeroByTipoAndSerie(tipo, serieActual);

    // Si la serie está llena, incrementa la serie
    if (ultimoNumero != null && ultimoNumero.equals(maxCorrelativo)) {
      // Asume formato F001, B001, etc.
      String prefijo = serieActual.substring(0, 1);
      int numeroSerie = Integer.parseInt(serieActual.substring(1));
      int nuevaSerie = numeroSerie + 1;
      return String.format("%s%03d", prefijo, nuevaSerie);
    }
    // Si no está llena, sigue usando la misma serie
    return serieActual;
  }

}
