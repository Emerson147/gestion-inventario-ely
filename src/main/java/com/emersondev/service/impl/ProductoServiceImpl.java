package com.emersondev.service.impl;

import com.emersondev.api.request.ProductoRequest;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.api.response.ProductoResponse;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ProductoNotFoundException;
import com.emersondev.domain.repository.InventarioRepository;
import com.emersondev.domain.repository.ProductoRepository;
import com.emersondev.mapper.ProductoMapper;
import com.emersondev.service.interfaces.FileStorageService;
import com.emersondev.service.interfaces.ProductoService;
import com.emersondev.util.PaginationUtils;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

  private final ProductoRepository productoRepository;
  private final InventarioRepository inventarioRepository;
  private final ProductoMapper productoMapper;
  private final FileStorageService fileStorageService;
  private final SerieGenerator serieGenerator;

  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public ProductoResponse crearProducto(ProductoRequest productoRequest) {
    log.info("Creando nuevo Producto: {}", productoRequest.getNombre());
    log.info("Código recibido en request: '{}'", productoRequest.getCodigo());

    //Verificar si ya existe un producto con el mismo código
    if (productoRequest.getCodigo() != null && !productoRequest.getCodigo().isEmpty() && productoRepository.existsByCodigo(productoRequest.getCodigo())) {
      log.error("Ya existe un producto con el código: {}", productoRequest.getCodigo());
      throw new BusinessException("Ya existe un producto con el código especificado");
    }

    //Mapear el objeto request de una entidad
    Producto producto = productoMapper.toEntity(productoRequest);
    log.info("Código después de mapeo: '{}'", producto.getCodigo());


    if (producto.getCodigo() == null || producto.getCodigo().isEmpty()) {
      producto.setCodigo(serieGenerator.generarCodigoProducto());
      log.info("Código generado automáticamente: '{}'", producto.getCodigo());
    } else {
      log.info("Usando código proporcionado: '{}'", producto.getCodigo());
    }


    //Guardar el producto en la base de datos
    producto = productoRepository.save(producto);
    log.info("Producto creado exitosamente con ID: {}", producto.getId());

    log.info("Producto guardado con código: '{}'", producto.getCodigo());


    return productoMapper.toResponse(producto);
  }

  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'page-' + #page + '-' + #size + '-' + #sortBy + '-' + #sortDir")
  public PagedResponse<ProductoResponse> obtenerTodosLosProductos(int page, int size, String sortBy, String sortDir) {
    log.debug("Obteniendo página {} de productos", page);

    // Validar parámetros de paginación
    int[] validatedParams = PaginationUtils.validatePaginationParams(page, size);
    page = validatedParams[0];
    size = validatedParams[1];

    // Crear el objeto Pageable
    Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);

    // Ejecutar la consulta paginada
    Page<Producto> productosPage = productoRepository.findAll(pageable);

    // Si no hay resultados, devolver una respuesta vacía
    if (productosPage.isEmpty()) {
      return PaginationUtils.emptyPagedResponse(page, size);
    }

    // Convertir y devolver la respuesta paginada
    return PaginationUtils.createPagedResponse(productosPage, productoMapper::toResponse);

  }

  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'id-' + #id")
  public ProductoResponse obtenerProductoPorId(Long id) {
    log.debug("Obteniendo producto con ID {}", id);

    // Buscar el producto por ID
    Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Producto con ID {} no encontrado", id);
              return new ProductoNotFoundException(id);
            });

    return productoMapper.toResponse(producto);
  }

  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'codigo-' + #codigo")
  public ProductoResponse obtenerProductoPorCodigo(String codigo) {
    log.debug("Obteniendo producto con código {}", codigo);

    // Buscar el producto por código
    Producto producto = productoRepository.findByCodigo(codigo)
            .orElseThrow(() -> {
              log.error("Producto no encontrado con código: {}", codigo);
              return new ProductoNotFoundException(codigo);
            });
    return productoMapper.toResponse(producto);
  }

  @Override
  @Transactional
  public List<ProductoResponse> buscarProductos(String termino, String filtro) {
    log.debug("Buscando productos con término: {} y filtro: {}", termino, filtro);

    List<Producto> productos;

    if (filtro != null && !filtro.isEmpty()) {
      productos = switch (filtro.toLowerCase()) {
        case "nombre" -> productoRepository.findByNombreContainingIgnoreCase(termino);
        case "codigo" -> productoRepository.findByCodigoContainingIgnoreCase(termino);
        case "marca" -> productoRepository.findByMarcaContainingIgnoreCase(termino);
        case "modelo" -> productoRepository.findByModeloContainingIgnoreCase(termino);
        default -> productoRepository.findByTermino(termino);
      };
    } else {
      productos = productoRepository.findByTermino(termino);
    }

    return productos.stream()
            .map(productoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public ProductoResponse actualizarProducto(Long id, ProductoRequest productoRequest) {
    log.info("Actualizando producto con ID {}", id);

    //Verificar si el producto existe
    Producto producto = getProductoById(id);

    //Verificar si el código ya esta siendo usado por otro producto
    if (productoRequest.getCodigo() != null && !productoRequest.getCodigo().isEmpty() && !producto.getCodigo().equals(productoRequest.getCodigo()) &&
            productoRepository.existsByCodigo(productoRequest.getCodigo())) {
      log.error("Ya existe un producto con el código: {}", productoRequest.getCodigo());
      throw new BusinessException("Ya existe un producto con el código especificado");
    }

    // Actualizar los datos del producto
    producto.setNombre(productoRequest.getNombre());
    producto.setDescripcion(productoRequest.getDescripcion());
    producto.setMarca(productoRequest.getMarca());
    producto.setModelo(productoRequest.getModelo());
    producto.setPrecioCompra(productoRequest.getPrecioCompra());
    producto.setPrecioVenta(productoRequest.getPrecioVenta());

    if (productoRequest.getCodigo() != null && !productoRequest.getCodigo().isEmpty()) {
      producto.setCodigo(productoRequest.getCodigo());
    }

    if (productoRequest.getImagen() != null && !productoRequest.getImagen().isEmpty()) {
      producto.setImagen(productoRequest.getImagen());
    }
    // La actualización de colores y tallas requeriría una lógica más compleja
    // para manejar adiciones, eliminaciones y modificaciones

    //Guardar los cambios
    producto = productoRepository.save(producto);
    log.info("Producto actualizado exitosamente con ID: {}", producto.getId());

    return productoMapper.toResponse(producto);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"productos", "inventario"}, allEntries = true)
  public void eliminarProducto(Long id) {
    log.info("Eliminando producto con ID: {}", id);

    //Verificar si el producto existe
    if (!productoRepository.existsById(id)) {
      log.error("Producto no encontrado con ID: {}", id);
      throw new ProductoNotFoundException(id);
    }

    //Verificar si el producto tiene inventario asociado
    if (inventarioRepository.existsById(id)) {
      log.error("No se puede eliminar el producto con ID: {} porque tiene inventario asociado", id);
      throw new BusinessException("No se puede eliminar el producto porque tiene inventario asociado");
    }

    //Eliminar el producto
    productoRepository.deleteById(id);
    log.info("Producto eliminado exitosamente con ID: {}", id);
  }


  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public ProductoResponse guardarImagenProducto(Long id, MultipartFile imagen) {
    log.info("Guardando imagen para producto con ID: {}", id);

    // Verificar si el producto existe
    Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Producto no encontrado con ID: {}", id);
              return new ProductoNotFoundException(id);
            });

    // Delegar la validación al FileStorageService
    fileStorageService.validateImageFile(imagen);

    // Guardar la imagen y obtener la ruta
    String fileName = fileStorageService.storeFile(imagen);

    // Actualizar la ruta de la imagen en el producto
    producto.setImagen(fileName);
    producto = productoRepository.save(producto);

    log.info("Imagen guardada exitosamente para producto con ID: {}", id);

    return productoMapper.toResponse(producto);

  }

  @Override
  public List<ProductoResponse> buscarPorSerie(String serie) {
    List<Producto> productos = productoRepository.findByInventariosSerie(serie);

    if (productos.isEmpty()) {
      return Collections.emptyList();
    }

    return productos.stream()
            .map(productoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  public List<String> obtenerTodasLasMarcas() {
    log.debug("Obteniendo todas las marcas de productos");
    return productoRepository.findDistinctMarcas();
  }

  @Override
  public long contarProductos() {
    return 0;
  }

  @Override
  @Transactional
  public List<ProductoResponse> obtenerProductosConStockBajo(Integer umbral) {
    log.debug("Obteniendo productos con stock bajo (umbral: {})", umbral);

    // Obtener IDs de productos con stock bajo
    List<Long> productosIds = inventarioRepository.findProductosConStockBajo(umbral);


    // Obtener productos por IDs
    List<Producto> productos = productoRepository.findAllById(productosIds);

    // Mapear a respuestas y devolver
    return productos.stream()
            .map(productoMapper::toResponse)
            .collect(Collectors.toList());
  }

  /*
    Metodo privado para evitar redundancia de  producto id
   */
  private Producto getProductoById(Long id) {
    return productoRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Producto no encontrado con ID: {}", id);
              return new ProductoNotFoundException(id);
            });
  }
}
