package com.emersondev.mapper;

import com.emersondev.api.response.DetalleVentaResponse;
import com.emersondev.domain.entity.DetalleVenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DetalleVentaMapper {

  private final InventarioMapper inventarioMapper;

//  public DetalleVentaResponse toResponse(DetalleVenta detalle) {
//    if (detalle == null) {
//      return null;
//    }
//
//    DetalleVentaResponse response = new DetalleVentaResponse();
//    response.setId(detalle.getId());
//    response.setCantidad(detalle.getCantidad());
//    response.setPrecioUnitario(detalle.getPrecioUnitario());
//    response.setSubtotal(detalle.getSubtotal());
//
//    if (detalle.getInventario() != null) {
//      response.setInventario(inventarioMapper.toResponse(detalle.getInventario()));
//    }
//
//    return response;
//  }
}
