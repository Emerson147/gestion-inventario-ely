package com.emersondev.service.interfaces;

import com.emersondev.api.response.FacturaResponse;
import jakarta.annotation.Resource;

import java.util.List;

public interface FacturaService {
  FacturaResponse obtenerFacturaPorId(Long id);

  FacturaResponse obtenerFacturaPorNumero(String numeroFactura);

  FacturaResponse obtenerFacturaPorVenta(Long ventaId);

  List<FacturaResponse> obtenerTodasLasFacturas();

  Resource generarPdfFactura(Long facturaId);

  Resource generarPdfFacturaPorVenta(Long ventaId);
}
