package com.emersondev.service.interfaces;

import com.emersondev.api.response.ResumenVentaResponse;
import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.Map;

public interface ReporteService {
  Map<String, Object> generarReporteVentasDiarias(LocalDate fecha);

  Map<String, Object> generarReporteVentasMensuales(int mes, int anio);

  ResumenVentaResponse obtenerResumenVentas(LocalDate fechaInicio, LocalDate fechaFin);

  Map<String, Object> generarReporteProductosStockBajo(Integer umbral);

  Resource generarReporteVentasPdf(LocalDate fechaInicio, LocalDate fechaFin);

  Resource generarReporteInventarioPdf();

  Resource generarReporteVentasExcel(LocalDate fechaInicio, LocalDate fechaFin);
}
