package com.emersondev.service.interfaces;

import com.emersondev.api.request.VentaRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.VentaResponse;
import com.emersondev.domain.entity.Factura;

import java.time.LocalDate;
import java.util.List;

public interface VentaService {
  VentaResponse crearVenta(VentaRequest ventaRequest);

  VentaResponse obtenerVentaPorId(Long id);

  VentaResponse obtenerVentaPorNumero(String numeroVenta);

  PagedResponse<VentaResponse> obtenerVentas(int page, int size, String sortBy, String sortDir);

  List<VentaResponse> obtenerVentasPorFecha(LocalDate fecha);

  List<VentaResponse> obtenerVentasPorCliente(Long clienteId);

  VentaResponse anularVenta(Long id);

  VentaResponse generarFactura(Long ventaId, Factura.TipoComprobante tipoComprobante);
}
