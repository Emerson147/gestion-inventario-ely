package com.emersondev.mapper;

import com.emersondev.api.response.PagoResponse;
import com.emersondev.domain.entity.Pago;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PagoMapper {

  /**
   * Convierte una entidad Pago a PagoResponse con saldo pendiente proporcionado
   */
  public PagoResponse toResponse(Pago pago, BigDecimal saldoPendiente) {
    if (pago == null) {
      return null;
    }

    PagoResponse response = new PagoResponse();
    response.setId(pago.getId());
    response.setNumeroPago(pago.getNumeroPago());

    // Mapear información de venta
    if (pago.getVenta() != null) {
      PagoResponse.VentaInfo venta = getVentaInfo(pago, saldoPendiente);

      response.setVenta(venta);
    }

    // Mapear información de usuario
    if (pago.getUsuario() != null) {
      PagoResponse.UsuarioInfo usuario = new PagoResponse.UsuarioInfo();
      usuario.setId(pago.getUsuario().getId());
      usuario.setNombre(pago.getUsuario().getNombres());
      usuario.setUsername(pago.getUsuario().getUsername());
      response.setUsuario(usuario);
    }

    response.setMonto(pago.getMonto());
    response.setMetodoPago(pago.getMetodoPago().name());
    response.setEstado(pago.getEstado().name());
    response.setNumeroReferencia(pago.getNumeroReferencia());
    response.setNombreTarjeta(pago.getNombreTarjeta());
    response.setUltimos4Digitos(pago.getUltimos4Digitos());
    response.setObservaciones(pago.getObservaciones());
    response.setFechaCreacion(pago.getFechaCreacion());
    response.setFechaActualizacion(pago.getFechaActualizacion());

    return response;
  }

  private static PagoResponse.VentaInfo getVentaInfo(Pago pago, BigDecimal saldoPendiente) {
    PagoResponse.VentaInfo venta = new PagoResponse.VentaInfo();
    venta.setId(pago.getVenta().getId());
    venta.setNumeroVenta(pago.getVenta().getNumeroVenta());

    // Cliente
    if (pago.getVenta().getCliente() != null) {
      venta.setCliente(pago.getVenta().getCliente().getNombres() + " " +
              pago.getVenta().getCliente().getApellidos());
    }

    venta.setTotal(pago.getVenta().getTotal());
    venta.setEstado(pago.getVenta().getEstado().name());

    // Usar el saldo pendiente proporcionado
    venta.setSaldoPendiente(saldoPendiente);
    return venta;
  }

  /**
   * Sobrecarga que establece el saldo pendiente como cero
   * Útil cuando no es necesario o no se dispone del saldo pendiente
   */
  public PagoResponse toResponse(Pago pago) {
    return toResponse(pago, BigDecimal.ZERO);
  }

}