package com.emersondev.api.controller;

import com.emersondev.api.request.VentaRequest;
import com.emersondev.api.response.ReporteVentasResponse;
import com.emersondev.api.response.VentaResponse;
import com.emersondev.domain.entity.Venta;
import com.emersondev.service.interfaces.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

  private final VentaService ventaService;

  /**
   * Registra una nueva venta
   */
  @PostMapping("/registrar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<VentaResponse> registrarVenta(@Valid @RequestBody VentaRequest ventaRequest) {
    log.info("Recibida solicitud para registrar nueva venta");
    VentaResponse ventaResponse = ventaService.registrarVenta(ventaRequest);
    return new ResponseEntity<>(ventaResponse, HttpStatus.CREATED);
  }

  /**
   * Obtiene todas las ventas
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerTodasLasVentas() {
    log.info("Recibida solicitud para obtener todas las ventas");
    List<VentaResponse> ventas = ventaService.obtenerTodasLasVentas();
    return ResponseEntity.ok(ventas);
  }

  /**
   * Obtiene una venta por su ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<VentaResponse> obtenerVentaPorId(@PathVariable Long id) {
    log.info("Recibida solicitud para obtener venta con ID: {}", id);
    VentaResponse venta = ventaService.obtenerVentaPorId(id);
    return ResponseEntity.ok(venta);
  }

  /**
   * Obtiene una venta por su número
   */
  @GetMapping("/numero/{numeroVenta}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<VentaResponse> obtenerVentaPorNumero(@PathVariable String numeroVenta) {
    log.info("Recibida solicitud para obtener venta con número: {}", numeroVenta);
    VentaResponse venta = ventaService.obtenerVentaPorNumero(numeroVenta);
    return ResponseEntity.ok(venta);
  }

  /**
   * Obtiene ventas por estado
   */
  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasPorEstado(@PathVariable Venta.EstadoVenta estado) {
    log.info("Recibida solicitud para obtener ventas con estado: {}", estado);
    List<VentaResponse> ventas = ventaService.obtenerVentasPorEstado(estado);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Obtiene ventas por cliente
   */
  @GetMapping("/cliente/{clienteId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasPorCliente(@PathVariable Long clienteId) {
    log.info("Recibida solicitud para obtener ventas del cliente ID: {}", clienteId);
    List<VentaResponse> ventas = ventaService.obtenerVentasPorCliente(clienteId);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Obtiene ventas por usuario
   */
  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasPorUsuario(@PathVariable Long usuarioId) {
    log.info("Recibida solicitud para obtener ventas del usuario ID: {}", usuarioId);
    List<VentaResponse> ventas = ventaService.obtenerVentasPorUsuario(usuarioId);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Obtiene ventas por fecha
   */
  @GetMapping("/fecha")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasPorFecha(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    log.info("Recibida solicitud para obtener ventas de la fecha: {}", fecha);
    List<VentaResponse> ventas = ventaService.obtenerVentasPorFecha(fecha);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Obtiene ventas entre fechas
   */
  @GetMapping("/fechas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasEntreFechas(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para obtener ventas entre {} y {}", fechaInicio, fechaFin);
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
    List<VentaResponse> ventas = ventaService.obtenerVentasEntreFechas(inicio, fin);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Anula una venta
   */
  @PutMapping("/{id}/anular")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VentaResponse> anularVenta(
          @PathVariable Long id,
          @RequestParam String motivo) {
    log.info("Recibida solicitud para anular venta ID: {} con motivo: {}", id, motivo);
    VentaResponse venta = ventaService.anularVenta(id, motivo);
    return ResponseEntity.ok(venta);
  }

  /**
   * Elimina una venta (solo para ventas en estado PENDIENTE)
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
    log.info("Recibida solicitud para eliminar venta ID: {}", id);
    ventaService.eliminarVenta(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Actualiza el estado de una venta
   */
  @PutMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<VentaResponse> actualizarEstadoVenta(
          @PathVariable Long id,
          @RequestParam Venta.EstadoVenta nuevoEstado) {
    log.info("Recibida solicitud para actualizar estado de venta ID: {} a {}", id, nuevoEstado);
    VentaResponse venta = ventaService.actualizarEstadoVenta(id, nuevoEstado);
    return ResponseEntity.ok(venta);
  }

  /**
   * Actualiza el comprobante de una venta
   */
  @PutMapping("/{id}/comprobante")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<VentaResponse> actualizarComprobante(
          @PathVariable Long id,
          @RequestParam String serieComprobante,
          @RequestParam String numeroComprobante) {
    log.info("Recibida solicitud para actualizar comprobante de venta ID: {} con serie: {} y número: {}",
            id, serieComprobante, numeroComprobante);
    VentaResponse venta = ventaService.actualizarComprobante(id, serieComprobante, numeroComprobante);
    return ResponseEntity.ok(venta);
  }

  /**
   * Genera un reporte de ventas
   */
  @GetMapping("/reportes/ventas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('GERENCIA')")
  public ResponseEntity<ReporteVentasResponse> generarReporteVentas(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para generar reporte de ventas entre {} y {}", fechaInicio, fechaFin);
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
    ReporteVentasResponse reporte = ventaService.generarReporteVentas(inicio, fin);
    return ResponseEntity.ok(reporte);
  }

  /**
   * Obtiene un resumen diario de ventas
   */
  @GetMapping("/resumen-diario")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('GERENCIA')")
  public ResponseEntity<Map<String, Object>> obtenerResumenDiario(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    LocalDate fechaReporte = fecha != null ? fecha : LocalDate.now();
    log.info("Recibida solicitud para obtener resumen diario de fecha: {}", fechaReporte);
    Map<String, Object> resumen = ventaService.obtenerResumenDiario(fechaReporte);
    return ResponseEntity.ok(resumen);
  }

  /**
   * Obtiene las ventas más recientes
   */
  @GetMapping("/recientes")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> obtenerVentasRecientes(
          @RequestParam(defaultValue = "10") int cantidad) {
    log.info("Recibida solicitud para obtener {} ventas más recientes", cantidad);
    List<VentaResponse> ventas = ventaService.obtenerVentasRecientes(cantidad);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Busca ventas por término
   */
  @GetMapping("/buscar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<List<VentaResponse>> buscarVentas(
          @RequestParam String termino) {
    log.info("Recibida solicitud para buscar ventas con término: {}", termino);
    List<VentaResponse> ventas = ventaService.buscarVentas(termino);
    return ResponseEntity.ok(ventas);
  }

  /**
   * Revierte una venta completada a pendiente
   */
  @PutMapping("/{id}/revertir")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<VentaResponse> revertirVentaCompletada(
          @PathVariable Long id,
          @RequestParam String motivo) {
    log.info("Recibida solicitud para revertir venta ID: {} a estado PENDIENTE con motivo: {}", id, motivo);
    VentaResponse venta = ventaService.revertirVentaCompletada(id, motivo);
    return ResponseEntity.ok(venta);
  }

  /**
   * Obtiene estadísticas de ventas por modelo de calzado
   */
  @GetMapping("/estadisticas/modelos")
  @PreAuthorize("hasRole('ADMIN') or hasRole('GERENCIA')")
  public ResponseEntity<Map<String, Object>> obtenerEstadisticasPorModelo(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para obtener estadísticas por modelo entre {} y {}", fechaInicio, fechaFin);
    Map<String, Object> estadisticas = ventaService.obtenerEstadisticasPorModelo(fechaInicio, fechaFin);
    return ResponseEntity.ok(estadisticas);
  }

  /**
   * Obtiene un resumen de compras del cliente
   */
  @GetMapping("/clientes/{clienteId}/resumen")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('GERENCIA')")
  public ResponseEntity<Map<String, Object>> obtenerResumenComprasPorCliente(
          @PathVariable Long clienteId) {
    log.info("Recibida solicitud para obtener resumen de compras del cliente ID: {}", clienteId);
    Map<String, Object> resumen = ventaService.obtenerResumenComprasPorCliente(clienteId);
    return ResponseEntity.ok(resumen);
  }
}