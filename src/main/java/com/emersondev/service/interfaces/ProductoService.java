package com.emersondev.service.interfaces;

import com.emersondev.api.request.ProductRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.ProductoResponse;

import java.util.List;

public interface ProductoService {
  ProductoResponse crearProducto(ProductRequest productoRequest);

  PagedResponse<ProductoResponse> obtenerProductos(int page, int size, String sortBy, String sortDir);

  ProductoResponse obtenerProductoPorId(Long id);

  ProductoResponse obtenerProductoPorCodigo(String codigo);

  List<ProductoResponse> buscarProductos(String termino);

  ProductoResponse actualizarProducto(Long id, ProductRequest productoRequest);

  void eliminarProducto(Long id);

  List<ProductoResponse> buscarPorSerie(String serie);

  List<ProductoResponse> obtenerProductosConStockBajo(Integer umbral);
}
