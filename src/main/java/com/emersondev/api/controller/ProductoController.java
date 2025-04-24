package com.emersondev.api.controller;

import com.emersondev.api.request.ProductoRequest;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.ProductoResponse;
import com.emersondev.service.interfaces.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

  private final ProductoService productoService;

  /**
   * Crea un nuevo producto
   *
   * @param productoRequest Objeto que contiene los datos del producto a crear
   * @return ResponseEntity<ProductoResponse> Objeto que contiene la respuesta de la creación del producto
   */
  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<ProductoResponse> crearProducto(@Valid @RequestBody ProductoRequest productoRequest) {
    ProductoResponse nuevoProducto = productoService.crearProducto(productoRequest);
    return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
  }

  /**
   * Obtiene todos los productos con paginación
   */
  @GetMapping
  public ResponseEntity<PagedResponse<ProductoResponse>> obtenerProductos(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir) {
    PagedResponse<ProductoResponse> productos = productoService.obtenerTodosLosProductos(page, size, sortBy, sortDir);
    return ResponseEntity.ok(productos);
  }

  /**
   * Obtiene todos los productos por ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<ProductoResponse> obtenerProductoPorId(@PathVariable Long id) {
    return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
  }

  /**
   * Obtiene todos los productos por código
   */
  @GetMapping("/codigo/{codigo}")
  public ResponseEntity<ProductoResponse> obtenerProductoPorCodigo(@PathVariable String codigo) {
    return ResponseEntity.ok(productoService.obtenerProductoPorCodigo(codigo));
  }

  /**
   * Busca productos por término en diferentes campos según el filtro especificado
   */
  @GetMapping("/buscar")
  public ResponseEntity<List<ProductoResponse>> buscarProductos(
          @RequestParam String termino,
          @RequestParam(required = false) String filtro) {
    return ResponseEntity.ok(productoService.buscarProductos(termino, filtro));
  }


  @GetMapping("/serie/{serie}")
  public ResponseEntity<List<ProductoResponse>> buscarPorSerie(@PathVariable String serie) {
    List<ProductoResponse> productos = productoService.buscarPorSerie(serie);
    return ResponseEntity.ok(productos);
  }

  /*
    Actualiza el producto existente
   */
  @PutMapping("/actualizar/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<ProductoResponse> actualizarProducto(
          @PathVariable Long id,
          @Valid @RequestBody ProductoRequest productoRequest) {
    return ResponseEntity.ok(productoService.actualizarProducto(id, productoRequest));
  }

  /*
    Elimina un producto por su ID
   */
  @DeleteMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarProducto(@PathVariable Long id) {
    productoService.eliminarProducto(id);
    return ResponseEntity.ok(new MensajeResponse("Producto eliminado correctamente"));
  }

  /*
    Sube una imagen para un producto
   */
  @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<ProductoResponse> subirImagenProducto(
          @PathVariable Long id,
          @RequestParam("imagen") MultipartFile imagen) {
    ProductoResponse producto = productoService.guardarImagenProducto(id, imagen);
    return ResponseEntity.ok(producto);
  }

  /**
   * Obtiene productos con stock bajo según un umbral
   */
  @GetMapping("/stock-bajo")
  public ResponseEntity<List<ProductoResponse>> obtenerProductosStockBajo(
          @RequestParam(defaultValue = "5") Integer umbral) {
    return ResponseEntity.ok(productoService.obtenerProductosConStockBajo(umbral));
  }

  /**
   * Obtiene todas las marcas distintas de productos
   */
  @GetMapping("/marcas")
  public ResponseEntity<List<String>> obtenerMarcas() {
    return ResponseEntity.ok(productoService.obtenerTodasLasMarcas());
  }



}
