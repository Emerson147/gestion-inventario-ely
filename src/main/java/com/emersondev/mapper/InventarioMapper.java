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

@Component
@RequiredArgsConstructor
public class InventarioMapper {

  private final ProductoMapper productoMapper;
  private final AlmacenMapper almacenMapper;

  public Inventario toEntity(InventarioRequest request) {
    if (request == null) {
      return null;
    }

    Inventario inventario = new Inventario();
    inventario.setCantidad(request.getCantidad());
    inventario.setSerie(request.getSerie());
    inventario.setUbicacionExacta(request.getUbicacionExacta());

    return inventario;
  }

  public InventarioResponse toResponse(Inventario inventario) {
    if (inventario == null) {
      return null;
    }

    InventarioResponse response = new InventarioResponse();
    response.setId(inventario.getId());
    response.setCantidad(inventario.getCantidad());
    response.setSerie(inventario.getSerie());
    response.setUbicacionExacta(inventario.getUbicacionExacta());

    if (inventario.getProducto() != null) {
      response.setProducto(mapSimplificadoProducto(inventario.getProducto()));
    }

    if (inventario.getColor() != null) {
      response.setColor(mapSimplificadoColor(inventario.getColor()));
    }

    if (inventario.getTalla() != null) {
      response.setTalla(mapSimplificadoTalla(inventario.getTalla()));
    }

    if (inventario.getAlmacen() != null) {
      response.setAlmacen(almacenMapper.toResponse(inventario.getAlmacen()));
    }

    return response;
  }

  // Métodos auxiliares para mapear objetos simplificados sin relaciones anidadas

  private ProductoResponse mapSimplificadoProducto(Producto producto) {
    if (producto == null) {
      return null;
    }

    ProductoResponse response = new ProductoResponse();
    response.setId(producto.getId());
    response.setCodigo(producto.getCodigo());
    response.setNombre(producto.getNombre());
    response.setMarca(producto.getMarca());
    response.setModelo(producto.getModelo());
    response.setPrecioVenta(producto.getPrecioVenta());

    return response;
  }

  private ColorResponse mapSimplificadoColor(Color color) {
    if (color == null) {
      return null;
    }

    ColorResponse response = new ColorResponse();
    response.setId(color.getId());
    response.setNombre(color.getNombre());

    return response;
  }

  private TallaResponse mapSimplificadoTalla(Talla talla) {
    if (talla == null) {
      return null;
    }

    TallaResponse response = new TallaResponse();
    response.setId(talla.getId());
    response.setNumero(talla.getNumero());

    return response;
  }
}
