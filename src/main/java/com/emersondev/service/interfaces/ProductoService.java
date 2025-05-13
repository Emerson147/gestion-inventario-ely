package com.emersondev.service.interfaces;

import com.emersondev.api.request.ProductoRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.ProductoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductoService {

  ProductoResponse crearProducto(ProductoRequest productoRequest);

  PagedResponse<ProductoResponse> obtenerTodosLosProductos(int page, int size, String sortBy, String sortDir);

  ProductoResponse obtenerProductoPorId(Long id);

  ProductoResponse obtenerProductoPorCodigo(String codigo);

  List<ProductoResponse> buscarProductos(String termino, String filtro);

  ProductoResponse actualizarProducto(Long id, ProductoRequest productoRequest);

  void eliminarProducto(Long id);

  ProductoResponse guardarImagenProducto(Long id, MultipartFile imagen);

  List<ProductoResponse> obtenerProductosConStockBajo(Integer umbral);

  List<ProductoResponse> buscarPorSerie(String serie);

  List<String> obtenerTodasLasMarcas();

  long contarProductos();
}
