package com.emersondev.mapper;

import com.emersondev.api.response.DetalleVentaResponse;
import com.emersondev.api.response.VentaResponse;
import com.emersondev.domain.entity.DetalleVenta;
import com.emersondev.domain.entity.Venta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VentaMapper {

  /**
   * Convierte una entidad Venta a VentaResponse
   */
  public VentaResponse toResponse(Venta venta) {
    if (venta == null) {
      return null;
    }

    VentaResponse response = new VentaResponse();
    response.setId(venta.getId());
    response.setNumeroVenta(venta.getNumeroVenta());

    // Mapear cliente
    if (venta.getCliente() != null) {
      VentaResponse.ClienteInfo cliente = new VentaResponse.ClienteInfo();
      cliente.setId(venta.getCliente().getId());
      cliente.setNombres(venta.getCliente().getNombres());
      cliente.setApellidos(venta.getCliente().getApellidos());

      // Usar DNI como documento principal, si está disponible
      if (venta.getCliente().getDni() != null && !venta.getCliente().getDni().isEmpty()) {
        cliente.setDocumento("DNI: " + venta.getCliente().getDni());
      }
      // Si no hay DNI pero hay RUC, usar RUC
      else if (venta.getCliente().getRuc() != null && !venta.getCliente().getRuc().isEmpty()) {
        cliente.setDocumento("RUC: " + venta.getCliente().getRuc());
      }

      response.setCliente(cliente);
    }

    // Mapear usuario
    if (venta.getUsuario() != null) {
      VentaResponse.UsuarioInfo usuario = new VentaResponse.UsuarioInfo();
      usuario.setId(venta.getUsuario().getId());
      usuario.setNombre(venta.getUsuario().getNombre());
      usuario.setUsername(venta.getUsuario().getUsername());
      response.setUsuario(usuario);
    }

    response.setSubtotal(venta.getSubtotal());
    response.setIgv(venta.getIgv());
    response.setTotal(venta.getTotal());
    response.setEstado(venta.getEstado().name());

    if (venta.getTipoComprobante() != null) {
      response.setTipoComprobante(venta.getTipoComprobante().name());
    }

    response.setSerieComprobante(venta.getSerieComprobante());
    response.setNumeroComprobante(venta.getNumeroComprobante());
    response.setObservaciones(venta.getObservaciones());

    // Mapear detalles
    response.setDetalles(venta.getDetalles().stream()
            .map(this::toDetalleResponse)
            .collect(Collectors.toList()));

    response.setFechaCreacion(venta.getFechaCreacion());
    response.setFechaActualizacion(venta.getFechaActualizacion());

    return response;
  }

  /**
   * Convierte una entidad DetalleVenta a DetalleVentaResponse
   */
  public DetalleVentaResponse toDetalleResponse(DetalleVenta detalle) {
    if (detalle == null) {
      return null;
    }

    DetalleVentaResponse response = new DetalleVentaResponse();
    response.setId(detalle.getId());

    // Mapear producto
    if (detalle.getProducto() != null) {
      DetalleVentaResponse.ProductoInfo producto = new DetalleVentaResponse.ProductoInfo();
      producto.setId(detalle.getProducto().getId());
      producto.setCodigo(detalle.getProducto().getCodigo());
      producto.setNombre(detalle.getProducto().getNombre());
      response.setProducto(producto);
    }

    // Mapear color
    if (detalle.getColor() != null) {
      DetalleVentaResponse.ColorInfo color = new DetalleVentaResponse.ColorInfo();
      color.setId(detalle.getColor().getId());
      color.setNombre(detalle.getColor().getNombre());
      response.setColor(color);
    }

    // Mapear talla
    if (detalle.getTalla() != null) {
      DetalleVentaResponse.TallaInfo talla = new DetalleVentaResponse.TallaInfo();
      talla.setId(detalle.getTalla().getId());
      talla.setNumero(detalle.getTalla().getNumero());
      response.setTalla(talla);
    }

    response.setCantidad(detalle.getCantidad());
    response.setPrecioUnitario(detalle.getPrecioUnitario());
    response.setSubtotal(detalle.getSubtotal());
    response.setDescripcionProducto(detalle.getDescripcionProducto());

    return response;
  }
}