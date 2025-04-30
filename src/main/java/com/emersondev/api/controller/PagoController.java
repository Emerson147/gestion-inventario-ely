package com.emersondev.api.controller;

import com.emersondev.api.request.PagoRequest;
import com.emersondev.api.response.PagoResponse;
import com.emersondev.api.response.ReportePagosResponse;
import com.emersondev.domain.entity.Pago;
import com.emersondev.service.interfaces.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Slf4j
public class PagoController {

  private final PagoService pagoService;

  /**
   * Registra un nuevo pago
   */
  @PostMapping("/registrar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody PagoRequest pagoRequest) {
    log.info("Recibida solicitud para registrar nuevo pago");
    PagoResponse pago = pagoService.registrarPago(pagoRequest);
    return new ResponseEntity<>(pago, HttpStatus.CREATED);
  }

  /**
   * Obtiene un pago por ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<PagoResponse> obtenerPagoPorId(@PathVariable Long id) {
    log.info("Obteniendo pago con ID: {}", id);
    PagoResponse pago = pagoService.obtenerPagoPorId(id);
    return ResponseEntity.ok(pago);
  }

  /**
   * Obtiene un pago por número
   */
  @GetMapping("/numero/{numeroPago}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<PagoResponse> obtenerPagoPorNumero(@PathVariable String numeroPago) {
    log.info("Obteniendo pago con número: {}", numeroPago);
    PagoResponse pago = pagoService.obtenerPagoPorNumero(numeroPago);
    return ResponseEntity.ok(pago);
  }

  /**
   * Obtiene pagos de una venta
   */
  @GetMapping("/venta/{ventaId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosPorVenta(@PathVariable Long ventaId) {
    log.info("Obteniendo pagos para venta ID: {}", ventaId);
    List<PagoResponse> pagos = pagoService.obtenerPagosPorVenta(ventaId);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Obtiene el total pagado para una venta
   */
  @GetMapping("/venta/{ventaId}/total-pagado")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<BigDecimal> obtenerTotalPagadoPorVenta(@PathVariable Long ventaId) {
    log.info("Obteniendo total pagado para venta ID: {}", ventaId);
    BigDecimal totalPagado = pagoService.obtenerTotalPagadoPorVenta(ventaId);
    return ResponseEntity.ok(totalPagado);
  }

  /**
   * Obtiene el saldo pendiente de una venta
   */
  @GetMapping("/venta/{ventaId}/saldo-pendiente")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<BigDecimal> calcularSaldoPendiente(@PathVariable Long ventaId) {
    log.info("Calculando saldo pendiente para venta ID: {}", ventaId);
    BigDecimal saldoPendiente = pagoService.calcularSaldoPendiente(ventaId);
    return ResponseEntity.ok(saldoPendiente);
  }

  /**
   * Verifica si una venta está pagada
   */
  @GetMapping("/venta/{ventaId}/pagada")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<Boolean> verificarVentaPagada(@PathVariable Long ventaId) {
    log.info("Verificando si la venta ID: {} está pagada", ventaId);
    boolean pagada = pagoService.verificarVentaPagada(ventaId);
    return ResponseEntity.ok(pagada);
  }

  /**
   * Obtiene pagos por método de pago
   */
  @GetMapping("/metodo/{metodoPago}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosPorMetodo(
          @PathVariable Pago.MetodoPago metodoPago) {
    log.info("Obteniendo pagos por método: {}", metodoPago);
    List<PagoResponse> pagos = pagoService.obtenerPagosPorMetodo(metodoPago);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Obtiene pagos por estado
   */
  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosPorEstado(
          @PathVariable Pago.EstadoPago estado) {
    log.info("Obteniendo pagos por estado: {}", estado);
    List<PagoResponse> pagos = pagoService.obtenerPagosPorEstado(estado);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Obtiene pagos por fecha
   */
  @GetMapping("/fecha")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosPorFecha(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    log.info("Obteniendo pagos para fecha: {}", fecha);
    List<PagoResponse> pagos = pagoService.obtenerPagosPorFecha(fecha);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Obtiene pagos entre fechas
   */
  @GetMapping("/fechas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosEntreFechas(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Obteniendo pagos entre fechas: {} y {}", fechaInicio, fechaFin);
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
    List<PagoResponse> pagos = pagoService.obtenerPagosEntreFechas(inicio, fin);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Obtiene pagos por usuario
   */
  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('CAJA')")
  public ResponseEntity<List<PagoResponse>> obtenerPagosPorUsuario(
          @PathVariable Long usuarioId) {
    log.info("Obteniendo pagos por usuario ID: {}", usuarioId);
    List<PagoResponse> pagos = pagoService.obtenerPagosPorUsuario(usuarioId);
    return ResponseEntity.ok(pagos);
  }

  /**
   * Actualiza el estado de un pago
   */
  @PutMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN') or hasRole('CAJA')")
  public ResponseEntity<PagoResponse> actualizarEstadoPago(
          @PathVariable Long id,
          @RequestParam Pago.EstadoPago nuevoEstado,
          @RequestParam(required = false) String observacion) {
    log.info("Actualizando estado de pago ID: {} a {}", id, nuevoEstado);
    PagoResponse pago = pagoService.actualizarEstadoPago(id, nuevoEstado, observacion);
    return ResponseEntity.ok(pago);
  }

  /**
   * Reembolsa un pago
   */
  @PutMapping("/{id}/reembolsar")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PagoResponse> reembolsarPago(
          @PathVariable Long id,
          @RequestParam String motivo) {
    log.info("Reembolsando pago ID: {} por motivo: {}", id, motivo);
    PagoResponse pago = pagoService.reembolsarPago(id, motivo);
    return ResponseEntity.ok(pago);
  }

  /**
   * Genera un reporte de pagos
   */
  @GetMapping("/reportes/pagos")
  @PreAuthorize("hasRole('ADMIN') or hasRole('GERENCIA')")
  public ResponseEntity<ReportePagosResponse> generarReportePagos(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Generando reporte de pagos entre {} y {}", fechaInicio, fechaFin);
    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
    ReportePagosResponse reporte = pagoService.generarReportePagos(inicio, fin);
    return ResponseEntity.ok(reporte);
  }

  /**
   * Obtiene resumen diario de pagos
   */
  @GetMapping("/resumen-diario")
  @PreAuthorize("hasRole('ADMIN') or hasRole('CAJA') or hasRole('GERENCIA')")
  public ResponseEntity<Map<String, Object>> obtenerResumenPagosDiario(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    LocalDate fechaReporte = fecha != null ? fecha : LocalDate.now();
    log.info("Obteniendo resumen de pagos para fecha: {}", fechaReporte);
    Map<String, Object> resumen = pagoService.obtenerResumenPagosDiario(fechaReporte);
    return ResponseEntity.ok(resumen);
  }
}
