package com.emersondev.api.controller;

import com.emersondev.api.request.ComprobanteRequest;
import com.emersondev.api.response.ComprobanteResponse;
import com.emersondev.api.response.ReporteComprobantesResponse;
import com.emersondev.domain.entity.Comprobante;
import com.emersondev.service.interfaces.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
@Slf4j
public class ComprobanteController {
  private final ComprobanteService comprobanteService;

  /**
   * Genera un nuevo comprobante para una venta
   */
  @PostMapping("/generar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<ComprobanteResponse> generarComprobante(
          @Valid @RequestBody ComprobanteRequest comprobanteRequest) {
    log.info("Recibida solicitud para generar comprobante para venta ID: {}",
            comprobanteRequest.getVentaId());
    ComprobanteResponse comprobante = comprobanteService.generarComprobante(comprobanteRequest);
    return new ResponseEntity<>(comprobante, HttpStatus.CREATED);
  }

  /**
   * Obtiene un comprobante por su ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<ComprobanteResponse> obtenerComprobante(@PathVariable Long id) {
    log.info("Recibida solicitud para obtener comprobante ID: {}", id);
    ComprobanteResponse comprobante = comprobanteService.obtenerComprobante(id);
    return ResponseEntity.ok(comprobante);
  }

  /**
   * Obtiene un comprobante por serie y número
   */
  @GetMapping("/serie-numero")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<ComprobanteResponse> obtenerComprobantePorSerieYNumero(
          @RequestParam String serie,
          @RequestParam String numero) {
    log.info("Recibida solicitud para obtener comprobante con serie {} y número {}", serie, numero);
    ComprobanteResponse comprobante = comprobanteService.obtenerComprobantePorSerieYNumero(serie, numero);
    return ResponseEntity.ok(comprobante);
  }

  /**
   * Obtiene un comprobante por tipo, serie y número
   */
  @GetMapping("/tipo-serie-numero")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<ComprobanteResponse> obtenerComprobantePorTipoSerieYNumero(
          @RequestParam Comprobante.TipoDocumento tipoDocumento,
          @RequestParam String serie,
          @RequestParam String numero) {
    log.info("Recibida solicitud para obtener comprobante de tipo {}, serie {} y número {}",
            tipoDocumento, serie, numero);
    ComprobanteResponse comprobante = comprobanteService.obtenerComprobantePorTipoSerieYNumero(tipoDocumento, serie, numero);
    return ResponseEntity.ok(comprobante);
  }

  /**
   * Obtiene el comprobante asociado a una venta
   */
  @GetMapping("/venta/{ventaId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<ComprobanteResponse> obtenerComprobantePorVenta(@PathVariable Long ventaId) {
    log.info("Recibida solicitud para obtener comprobante para venta ID: {}", ventaId);
    ComprobanteResponse comprobante = comprobanteService.obtenerComprobantePorVenta(ventaId);
    return ResponseEntity.ok(comprobante);
  }

  /**
   * Lista todos los comprobantes
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantes() {
    log.info("Recibida solicitud para listar todos los comprobantes");
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantes();
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes por tipo
   */
  @GetMapping("/tipo/{tipoDocumento}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesPorTipo(
          @PathVariable Comprobante.TipoDocumento tipoDocumento) {
    log.info("Recibida solicitud para listar comprobantes de tipo: {}", tipoDocumento);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesPorTipo(tipoDocumento);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes por estado
   */
  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesPorEstado(
          @PathVariable Comprobante.EstadoComprobante estado) {
    log.info("Recibida solicitud para listar comprobantes con estado: {}", estado);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesPorEstado(estado);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes por cliente
   */
  @GetMapping("/cliente/{clienteId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesPorCliente(
          @PathVariable Long clienteId) {
    log.info("Recibida solicitud para listar comprobantes del cliente ID: {}", clienteId);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesPorCliente(clienteId);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes por fecha
   */
  @GetMapping("/fecha")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesPorFecha(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    log.info("Recibida solicitud para listar comprobantes de la fecha: {}", fecha);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesPorFecha(fecha);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes entre fechas
   */
  @GetMapping("/fechas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesEntreFechas(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para listar comprobantes entre fechas: {} y {}", fechaInicio, fechaFin);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesEntreFechas(fechaInicio, fechaFin);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Lista comprobantes por tipo entre fechas
   */
  @GetMapping("/tipo-fechas")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<List<ComprobanteResponse>> listarComprobantesPorTipoEntreFechas(
          @RequestParam Comprobante.TipoDocumento tipoDocumento,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para listar comprobantes de tipo {} entre fechas: {} y {}",
            tipoDocumento, fechaInicio, fechaFin);
    List<ComprobanteResponse> comprobantes = comprobanteService.listarComprobantesPorTipoEntreFechas(
            tipoDocumento, fechaInicio, fechaFin);
    return ResponseEntity.ok(comprobantes);
  }

  /**
   * Anula un comprobante
   */
  @PutMapping("/{id}/anular")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<ComprobanteResponse> anularComprobante(
          @PathVariable Long id,
          @RequestParam String motivo) {
    log.info("Recibida solicitud para anular comprobante ID: {} con motivo: {}", id, motivo);
    ComprobanteResponse comprobante = comprobanteService.anularComprobante(id, motivo);
    return ResponseEntity.ok(comprobante);
  }

  /**
   * Obtiene el siguiente número de comprobante para una serie y tipo
   */
  @GetMapping("/siguiente-numero")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<String> obtenerSiguienteNumeroComprobante(
          @RequestParam Comprobante.TipoDocumento tipoDocumento,
          @RequestParam String serie) {
    log.info("Recibida solicitud para obtener siguiente número de comprobante para tipo {} y serie {}",
            tipoDocumento, serie);
    String siguienteNumero = comprobanteService.obtenerSiguienteNumeroComprobante(tipoDocumento, serie);
    return ResponseEntity.ok(siguienteNumero);
  }

  /**
   * Genera un PDF para un comprobante
   */
  @GetMapping("/{id}/pdf")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('VENTAS')")
  public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
    log.info("Recibida solicitud para generar PDF de comprobante ID: {}", id);
    byte[] pdf = comprobanteService.generarPdfComprobante(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", "comprobante-" + id + ".pdf");

    return ResponseEntity.ok()
            .headers(headers)
            .body(pdf);
  }

  /**
   * Genera un XML para un comprobante
   */
  @GetMapping("/{id}/xml")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION')")
  public ResponseEntity<byte[]> generarXml(@PathVariable Long id) {
    log.info("Recibida solicitud para generar XML de comprobante ID: {}", id);
    byte[] xml = comprobanteService.generarXmlComprobante(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    headers.setContentDispositionFormData("attachment", "comprobante-" + id + ".xml");

    return ResponseEntity.ok()
            .headers(headers)
            .body(xml);
  }

  /**
   * Genera un reporte de comprobantes
   */
  @GetMapping("/reportes/comprobantes")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('GERENCIA')")
  public ResponseEntity<ReporteComprobantesResponse> generarReporteComprobantes(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    log.info("Recibida solicitud para generar reporte de comprobantes entre {} y {}", fechaInicio, fechaFin);
    ReporteComprobantesResponse reporte = comprobanteService.generarReporteComprobantes(fechaInicio, fechaFin);
    return ResponseEntity.ok(reporte);
  }

  /**
   * Obtiene un resumen diario de comprobantes
   */
  @GetMapping("/resumen-diario")
  @PreAuthorize("hasRole('ADMIN') or hasRole('FACTURACION') or hasRole('GERENCIA')")
  public ResponseEntity<Map<String, Object>> obtenerResumenDiario(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
    LocalDate fechaReporte = fecha != null ? fecha : LocalDate.now();
    log.info("Recibida solicitud para obtener resumen diario de comprobantes para fecha: {}", fechaReporte);
    Map<String, Object> resumen = comprobanteService.obtenerResumenDiario(fechaReporte);
    return ResponseEntity.ok(resumen);
  }

}
