package com.emersondev.service.interfaces;

import com.emersondev.api.request.VentaRequest;
import com.emersondev.api.response.ReporteVentasResponse;
import com.emersondev.api.response.VentaResponse;
import com.emersondev.domain.entity.Almacen;
import com.emersondev.domain.entity.Venta;
import com.emersondev.domain.entity.Venta.EstadoVenta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VentaService {

  // Operaciones básicas CRUD
  VentaResponse registrarVenta(VentaRequest ventaRequest);

  VentaResponse obtenerVentaPorId(Long id);

  VentaResponse obtenerVentaPorNumero(String numeroVenta);

  List<VentaResponse> obtenerTodasLasVentas();

  void eliminarVenta(Long id);

  // Operaciones de búsqueda y filtrado
  List<VentaResponse> obtenerVentasPorEstado(EstadoVenta estado);

  List<VentaResponse> obtenerVentasPorCliente(Long clienteId);

  List<VentaResponse> obtenerVentasPorUsuario(Long usuarioId);

  List<VentaResponse> obtenerVentasEntreFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

  List<VentaResponse> obtenerVentasPorFecha(LocalDate fecha);

  List<VentaResponse> buscarVentas(String termino);

  // Operaciones de negocio
  VentaResponse anularVenta(Long id, String motivo);

  VentaResponse actualizarEstadoVenta(Long id, EstadoVenta nuevoEstado);

  VentaResponse actualizarComprobante(Long id, String serieComprobante, String numeroComprobante);

  VentaResponse revertirVentaCompletada(Long id, String motivo);

  // Reportes y estadísticas
  ReporteVentasResponse generarReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);

  Map<String, Object> obtenerResumenDiario(LocalDate fecha);

  List<VentaResponse> obtenerVentasRecientes(int cantidad);

  Map<String, Object> obtenerEstadisticasPorModelo(LocalDate fechaInicio, LocalDate fechaFin);

  Map<String, Object> obtenerResumenComprasPorCliente(Long clienteId);

  Map<String, Object> obtenerVentasDiarias(LocalDate fecha, Long usuarioId);

  Map<String, Object> obtenerVentasSemanales(LocalDate fechaReferencia, Long usuarioId);

  Map<String, Object> obtenerVentasMensuales(int mes, int año, Long usuarioId);

  Map<String, Object> obtenerTopVendedores(LocalDate fechaInicio, LocalDate fechaFin, int limit, Long usuarioId);

  Map<String, Object> obtenerTopProductos(LocalDate fechaInicio, LocalDate fechaFin, int limit, Long usuarioId);

  Map<String, Object> obtenerProductosSinMovimiento(int dias, int limit, Long usuarioId);

  List<Venta> findByFechaAndAlmacen(LocalDate fecha, Almacen almacen);
  List<Venta> findByFecha(LocalDate fecha);
}
