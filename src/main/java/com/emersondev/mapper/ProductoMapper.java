package com.emersondev.mapper;

import com.emersondev.api.request.ProductoRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.ProductoResponse;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.entity.Talla;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class ProductoMapper {

  public Producto toEntity(ProductoRequest request) {
    if (request == null) {
      return null;
    }

    Producto producto = new Producto();
    producto.setCodigo(request.getCodigo());
    producto.setNombre(request.getNombre());
    producto.setDescripcion(request.getDescripcion());
    producto.setMarca(request.getMarca());
    producto.setModelo(request.getModelo());
    producto.setPrecioCompra(request.getPrecioCompra());
    producto.setPrecioVenta(request.getPrecioVenta());
    producto.setImagen(request.getImagen());

    return producto;
  }

  public ProductoResponse toResponse(Producto producto) {
    if (producto == null) {
      return null;
    }

    ProductoResponse response = new ProductoResponse();
    response.setId(producto.getId());
    response.setCodigo(producto.getCodigo());
    response.setNombre(producto.getNombre());
    response.setDescripcion(producto.getDescripcion());
    response.setMarca(producto.getMarca());
    response.setModelo(producto.getModelo());
    response.setPrecioCompra(producto.getPrecioCompra());
    response.setPrecioVenta(producto.getPrecioVenta());
    response.setImagen(producto.getImagen());
    response.setFechaCreacion(producto.getFechaCreacion());
    response.setFechaActualizacion(producto.getFechaActualizacion());

    if (producto.getColores() != null) {
      response.setColores(producto.getColores().stream()
              .map(this::mapColorToResponse)
              .collect(Collectors.toList()));
    } else {
      response.setColores(new ArrayList<>());
    }

    return response;
  }

  private ColorResponse mapColorToResponse(Color color) {
    if (color == null) {
      return null;
    }

    ColorResponse response = new ColorResponse();
    response.setId(color.getId());
    response.setNombre(color.getNombre());

    if (color.getTallas() != null) {
      response.setTallas(color.getTallas().stream()
              .map(this::mapTallaToResponse)
              .collect(Collectors.toList()));
    } else {
      response.setTallas(new ArrayList<>());
    }

    return response;
  }

  private TallaResponse mapTallaToResponse(Talla talla) {
    if (talla == null) {
      return null;
    }

    TallaResponse response = new TallaResponse();
    response.setId(talla.getId());
    response.setNumero(talla.getNumero());

    return response;
  }
}
