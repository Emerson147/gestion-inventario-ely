package com.emersondev.mapper;

import com.emersondev.api.response.ComprobanteResponse;
import com.emersondev.api.response.DetalleComprobanteResponse;
import com.emersondev.domain.entity.Comprobante;
import com.emersondev.domain.entity.DetalleComprobante;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ComprobanteMapper {

  /**
   * Convierte una entidad Comprobante a ComprobanteResponse
   */
  public ComprobanteResponse toResponse(Comprobante comprobante) {
    if (comprobante == null) {
      return null;
    }

    ComprobanteResponse response = new ComprobanteResponse();
    response.setId(comprobante.getId());
    response.setTipoDocumento(comprobante.getTipoDocumento().name());
    response.setSerie(comprobante.getSerie());
    response.setNumero(comprobante.getNumero());
    response.setFechaEmision(comprobante.getFechaEmision());
    response.setCodigoHash(comprobante.getCodigoHash());

    // Mapear información de venta
    if (comprobante.getVenta() != null) {
      ComprobanteResponse.VentaInfo ventaInfo = new ComprobanteResponse.VentaInfo();
      ventaInfo.setId(comprobante.getVenta().getId());
      ventaInfo.setNumeroVenta(comprobante.getVenta().getNumeroVenta());
      ventaInfo.setEstado(comprobante.getVenta().getEstado().name());
      response.setVenta(ventaInfo);
    }

    // Mapear información del cliente
    if (comprobante.getCliente() != null) {
      ComprobanteResponse.ClienteInfo clienteInfo = new ComprobanteResponse.ClienteInfo();
      clienteInfo.setId(comprobante.getCliente().getId());
      clienteInfo.setNombres(comprobante.getCliente().getNombres());
      clienteInfo.setApellidos(comprobante.getCliente().getApellidos());
      clienteInfo.setDni(comprobante.getCliente().getDni());
      clienteInfo.setRuc(comprobante.getCliente().getRuc());
      clienteInfo.setDireccion(comprobante.getCliente().getDireccion());
      response.setCliente(clienteInfo);
    }

    // Mapear información del usuario
    if (comprobante.getUsuario() != null) {
      ComprobanteResponse.UsuarioInfo usuarioInfo = new ComprobanteResponse.UsuarioInfo();
      usuarioInfo.setId(comprobante.getUsuario().getId());
      usuarioInfo.setNombre(comprobante.getUsuario().getNombres());
      usuarioInfo.setUsername(comprobante.getUsuario().getUsername());
      response.setUsuario(usuarioInfo);
    }

    // Mapear montos
    response.setSubtotal(comprobante.getSubtotal());
    response.setIgv(comprobante.getIgv());
    response.setTotal(comprobante.getTotal());

    // Mapear estado y observaciones
    response.setEstado(comprobante.getEstado().name());
    response.setObservaciones(comprobante.getObservaciones());

    // Mapear detalles
    if (comprobante.getDetalles() != null && !comprobante.getDetalles().isEmpty()) {
      response.setDetalles(comprobante.getDetalles().stream()
              .map(this::toDetalleResponse)
              .collect(Collectors.toList()));
    }

    // Mapear rutas de archivos
    response.setRutaArchivoPdf(comprobante.getRutaArchivoPdf());
    response.setRutaArchivoXml(comprobante.getRutaArchivoXml());

    // Mapear fechas de anulación/creación/actualización
    response.setFechaAnulacion(comprobante.getFechaAnulacion());
    response.setMotivoAnulacion(comprobante.getMotivoAnulacion());
    response.setFechaCreacion(comprobante.getFechaCreacion());
    response.setFechaActualizacion(comprobante.getFechaActualizacion());

    return response;
  }

  /**
   * Convierte una entidad DetalleComprobante a DetalleComprobanteResponse
   */
  public DetalleComprobanteResponse toDetalleResponse(DetalleComprobante detalle) {
    if (detalle == null) {
      return null;
    }

    DetalleComprobanteResponse response = new DetalleComprobanteResponse();
    response.setId(detalle.getId());

    // Mapear información del producto
    if (detalle.getProducto() != null) {
      DetalleComprobanteResponse.ProductoInfo productoInfo = new DetalleComprobanteResponse.ProductoInfo();
      productoInfo.setId(detalle.getProducto().getId());
      productoInfo.setCodigo(detalle.getProducto().getCodigo());
      productoInfo.setNombre(detalle.getProducto().getNombre());
      response.setProducto(productoInfo);
    }

    // Mapear información del color
    if (detalle.getColor() != null) {
      DetalleComprobanteResponse.ColorInfo colorInfo = new DetalleComprobanteResponse.ColorInfo();
      colorInfo.setId(detalle.getColor().getId());
      colorInfo.setNombre(detalle.getColor().getNombre());
      response.setColor(colorInfo);
    }

    // Mapear información de la talla
    if (detalle.getTalla() != null) {
      DetalleComprobanteResponse.TallaInfo tallaInfo = new DetalleComprobanteResponse.TallaInfo();
      tallaInfo.setId(detalle.getTalla().getId());
      tallaInfo.setNumero(detalle.getTalla().getNumero());
      response.setTalla(tallaInfo);
    }

    // Mapear datos adicionales
    response.setCantidad(detalle.getCantidad());
    response.setPrecioUnitario(detalle.getPrecioUnitario());
    response.setSubtotal(detalle.getSubtotal());
    response.setIgv(detalle.getIgv());
    response.setTotal(detalle.getTotal());
    response.setDescripcion(detalle.getDescripcion());
    response.setUnidadMedida(detalle.getUnidadMedida());
    response.setCodigoProducto(detalle.getCodigoProducto());

    return response;
  }
}
