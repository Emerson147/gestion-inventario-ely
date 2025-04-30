package com.emersondev.service.interfaces;

import com.emersondev.api.request.PagoRequest;
import com.emersondev.api.response.PagoResponse;
import com.emersondev.api.response.ReportePagosResponse;
import com.emersondev.domain.entity.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PagoService {

  /**
   * Registra un nuevo pago
   * @param pagoRequest datos del pago
   * @return datos del pago registrado
   */
  PagoResponse registrarPago(PagoRequest pagoRequest);

  /**
   * Obtiene un pago por su ID
   * @param id ID del pago
   * @return datos del pago
   */
  PagoResponse obtenerPagoPorId(Long id);

  /**
   * Obtiene todos los pagos de una venta
   * @param ventaId ID de la venta
   * @return lista de pagos
   */
  List<PagoResponse> obtenerPagosPorVenta(Long ventaId);

  /**
   * Obtiene pagos por método de pago
   * @param metodoPago método de pago
   * @return lista de pagos
   */
  List<PagoResponse> obtenerPagosPorMetodo(Pago.MetodoPago metodoPago);

  /**
   * Obtiene pagos por estado
   * @param estado estado del pago
   * @return lista de pagos
   */
  List<PagoResponse> obtenerPagosPorEstado(Pago.EstadoPago estado);

  /**
   * Obtiene pagos en un rango de fechas
   * @param fechaInicio fecha inicial
   * @param fechaFin fecha final
   * @return lista de pagos
   */
  List<PagoResponse> obtenerPagosEntreFechas(LocalDate fechaInicio, LocalDate fechaFin);

  /**
   * Actualiza el estado de un pago
   * @param id ID del pago
   * @param nuevoEstado nuevo estado
   * @param observacion observación sobre el cambio (opcional)
   * @return datos del pago actualizado
   */
  PagoResponse actualizarEstadoPago(Long id, Pago.EstadoPago nuevoEstado, String observacion);

  /**
   * Reembolsa un pago
   * @param id ID del pago
   * @param motivo motivo del reembolso
   * @return datos del pago reembolsado
   */
   PagoResponse reembolsarPago(Long id, String motivo);

  /**
   * Obtiene totales por método de pago en un período
   * @param fechaInicio fecha inicial
   * @param fechaFin fecha final
   * @return mapa con totales por método de pago
   */
  Map<String, Object> obtenerTotalesPorMetodoPago(LocalDate fechaInicio, LocalDate fechaFin);


  /**
   * Obtiene un pago por su número
   */
  PagoResponse obtenerPagoPorNumero(String numeroPago);


  /**
   * Obtiene el total pagado para una venta
   */
  BigDecimal obtenerTotalPagadoPorVenta(Long ventaId);

  /**
   * Calcula el saldo pendiente de una venta
   */
  BigDecimal calcularSaldoPendiente(Long ventaId);


  /**
   * Obtiene pagos por fecha
   */
  List<PagoResponse> obtenerPagosPorFecha(LocalDate fecha);

  /**
   * Obtiene pagos entre fechas
   */
  List<PagoResponse> obtenerPagosEntreFechas(LocalDateTime inicio, LocalDateTime fin);

  /**
   * Obtiene pagos por usuario
   */
  List<PagoResponse> obtenerPagosPorUsuario(Long usuarioId);


  /**
   * Genera un reporte de pagos
   */
  ReportePagosResponse generarReportePagos(LocalDateTime inicio, LocalDateTime fin);

  /**
   * Obtiene resumen de pagos por día
   */
  Map<String, Object> obtenerResumenPagosDiario(LocalDate fecha);

  /**
   * Verifica si una venta está completamente pagada
   */
  boolean verificarVentaPagada(Long ventaId);

}

