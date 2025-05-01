package com.emersondev.service.interfaces;

import com.emersondev.api.request.ComprobanteRequest;
import com.emersondev.api.response.ComprobanteResponse;
import com.emersondev.api.response.ReporteComprobantesResponse;
import com.emersondev.domain.entity.Comprobante;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ComprobanteService {
  /**
   * Genera un nuevo comprobante para una venta
   */
  ComprobanteResponse generarComprobante(ComprobanteRequest comprobanteRequest);

  /**
   * Obtiene un comprobante por ID
   */
  ComprobanteResponse obtenerComprobante(Long id);

  /**
   * Obtiene un comprobante por serie y número
   */
  ComprobanteResponse obtenerComprobantePorSerieYNumero(String serie, String numero);

  /**
   * Obtiene un comprobante por tipo, serie y número
   */
  ComprobanteResponse obtenerComprobantePorTipoSerieYNumero(
          Comprobante.TipoDocumento tipo, String serie, String numero);

  /**
   * Obtiene el comprobante asociado a una venta
   */
  ComprobanteResponse obtenerComprobantePorVenta(Long ventaId);

  /**
   * Lista todos los comprobantes
   */
  List<ComprobanteResponse> listarComprobantes();

  /**
   * Lista comprobantes por tipo
   */
  List<ComprobanteResponse> listarComprobantesPorTipo(Comprobante.TipoDocumento tipoDocumento);

  /**
   * Lista comprobantes por estado
   */
  List<ComprobanteResponse> listarComprobantesPorEstado(Comprobante.EstadoComprobante estado);

  /**
   * Lista comprobantes por cliente
   */
  List<ComprobanteResponse> listarComprobantesPorCliente(Long clienteId);

  /**
   * Lista comprobantes por fecha
   */
  List<ComprobanteResponse> listarComprobantesPorFecha(LocalDate fecha);

  /**
   * Lista comprobantes entre fechas
   */
  List<ComprobanteResponse> listarComprobantesEntreFechas(LocalDate fechaInicio, LocalDate fechaFin);

  /**
   * Lista comprobantes por tipo entre fechas
   */
  List<ComprobanteResponse> listarComprobantesPorTipoEntreFechas(
          Comprobante.TipoDocumento tipoDocumento, LocalDate fechaInicio, LocalDate fechaFin);

  /**
   * Anula un comprobante
   */
  ComprobanteResponse anularComprobante(Long id, String motivo);

  /**
   * Obtiene el siguiente número de comprobante para una serie y tipo
   */
  String obtenerSiguienteNumeroComprobante(Comprobante.TipoDocumento tipo, String serie);

  /**
   * Genera un PDF para un comprobante
   */
  byte[] generarPdfComprobante(Long id);

  /**
   * Genera un XML para un comprobante
   */
  byte[] generarXmlComprobante(Long id);

  /**
   * Genera un reporte de comprobantes
   */
  ReporteComprobantesResponse generarReporteComprobantes(LocalDate fechaInicio, LocalDate fechaFin);

  /**
   * Obtiene un resumen diario de comprobantes
   */
  Map<String, Object> obtenerResumenDiario(LocalDate fecha);
}
