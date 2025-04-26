package com.emersondev.mapper;

import com.emersondev.api.request.InventarioRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.api.response.ProductoResponse;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Inventario;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.entity.Talla;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventarioMapper {

  /**
   * Convierte una entidad Inventario a un InventarioResponse
   */
  public InventarioResponse toResponse(Inventario inventario) {
    if (inventario == null) {
      return null;
    }

    InventarioResponse response = new InventarioResponse();
    response.setId(inventario.getId());
    response.setSerie(inventario.getSerie());
    response.setCantidad(inventario.getCantidad());
    response.setFechaCreacion(inventario.getFechaCreacion());
    response.setFechaActualizacion(inventario.getFechaActualizacion());

    // Mapeo de producto
    if (inventario.getProducto() != null) {
      InventarioResponse.ProductoSimpleResponse producto = new InventarioResponse.ProductoSimpleResponse();
      producto.setId(inventario.getProducto().getId());
      producto.setCodigo(inventario.getProducto().getCodigo());
      producto.setNombre(inventario.getProducto().getNombre());
      response.setProducto(producto);
    }

    // Mapeo de color
    if (inventario.getColor() != null) {
      InventarioResponse.ColorSimpleResponse color = new InventarioResponse.ColorSimpleResponse();
      color.setId(inventario.getColor().getId());
      color.setNombre(inventario.getColor().getNombre());
      response.setColor(color);
    }

    // Mapeo de talla
    if (inventario.getTalla() != null) {
      InventarioResponse.TallaSimpleResponse talla = new InventarioResponse.TallaSimpleResponse();
      talla.setId(inventario.getTalla().getId());
      talla.setNumero(inventario.getTalla().getNumero());
      response.setTalla(talla);
    }

    // Mapeo de almacén
    if (inventario.getAlmacen() != null) {
      InventarioResponse.AlmacenSimpleResponse almacen = new InventarioResponse.AlmacenSimpleResponse();
      almacen.setId(inventario.getAlmacen().getId());
      almacen.setNombre(inventario.getAlmacen().getNombre());
      almacen.setUbicacion(inventario.getAlmacen().getUbicacion());
      response.setAlmacen(almacen);
    }

    return response;
  }
}
