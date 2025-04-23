package com.emersondev.service.impl;

import com.emersondev.api.request.ColorRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Producto;
import com.emersondev.domain.entity.Talla;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ProductoNotFoundException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.ColorRepository;
import com.emersondev.domain.repository.InventarioRepository;
import com.emersondev.domain.repository.ProductoRepository;
import com.emersondev.mapper.ColorMapper;
import com.emersondev.service.interfaces.ColorService;
import com.emersondev.util.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

  private final ColorRepository colorRepository;
  private final ProductoRepository productoRepository;
  private final InventarioRepository inventarioRepository;
  private final ColorMapper colorMapper;

  /**
   * Método para crear un nuevo color asociado a un producto
   * @param productoId ID del producto al que se le asociará el color
   * @param colorRequest objeto que contiene la información del color a crear
   * @return objeto ColorResponse con la información del color creado
   */
  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public ColorResponse crearColor(Long productoId, ColorRequest colorRequest) {
    log.info("Creando nuevo color {} para el producto ID {}", colorRequest, productoId);

    // Verficar si el producto existe
    Producto producto = productoRepository.findById(productoId)
        .orElseThrow(() -> {
          log.error("Producto no encontrado con ID: {}", productoId);
          return new ProductoNotFoundException(productoId);
        });

    // Verificar si ya existe un color con el mismo nombre para este producto
    if (colorRepository.existsByNombreAndProductoId(colorRequest.getNombre(), productoId)) {
      log.error("Ya existe un color con el nombre {} para el producto ID {}", colorRequest.getNombre(), productoId);
      throw new BusinessException("Ya existe un color con el nombre " + colorRequest.getNombre());
    }

    // Crear y guardar el nuevo color
    Color color = new Color();
    color.setNombre(colorRequest.getNombre());
    color.setProducto(producto);
    color.setTallas(new HashSet<>());

    // Mapear tallas si se proporcionan
    if (colorRequest.getTallas() != null && !colorRequest.getTallas().isEmpty()) {
      colorMapper.mapTallas(color, colorRequest.getTallas());
    }

    color = colorRepository.save(color);
    log.info("Color creado exitosamente con ID {}", color.getId());

    return colorMapper.toResponse(color);
  }

  @Override
  @Transactional
  public PagedResponse<ColorResponse> obtenerColores(int page, int size, String sortBy, String sortDir) {
    log.debug("Obteniendo pagina {} de usuarios con tamaño {} ordenada por {} en dirección {}", page, size, sortBy, sortDir);

    //Validar parametros de paginacion
    int[] validatedParams = PaginationUtils.validatePaginationParams(page, size);
    page = validatedParams[0];
    size = validatedParams[1];

    //Crear el obejto pageable
    Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);

    //Ejecutar la consulata paginada
    Page<Color> colorPage = colorRepository.findAll(pageable);

    //Si no hay resultados, devolver una respuesta vacia
    if (colorPage.isEmpty()) {
      log.info("No se encontraron Colores");
      return PaginationUtils.emptyPagedResponse(page, size);
    }

    //Convertir y devolver las respuestas paginadas
    return PaginationUtils.createPagedResponse(colorPage, colorMapper::toResponse);
  }

  /**
   * Método para obtener un color por su ID
   * @param id ID del color a buscar
   * @return objeto ColorResponse con la información del color encontrado
   */
  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'color-' + #id")
  public ColorResponse obtenerColorPorId(Long id) {
    log.debug("Obteniendo color por ID {}", id);

    // Buscar el color por ID
    Color color = colorRepository.findById(id)
        .orElseThrow(() -> {
          log.error("Color con ID {} no encontrado", id);
          return new ResourceNotFoundException("Color", "id", id);
        });

    // Mapear a respuesta y devolver
    return colorMapper.toResponse(color);
  }

  /**
   * Método para obtener una lista de colores asociados a un producto
   * @param productoId ID del producto del cual se desean obtener los colores
   * @return lista de objetos ColorResponse con la información de los colores encontrados
   */
  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'colores-producto-' + #productoId")
  public List<ColorResponse> obtenerColoresPorProducto(Long productoId) {
    log.debug("Obteniendo colores para el producto ID {}", productoId);

    //Verificar si el producto existe
    if (!productoRepository.existsById(productoId)) {
      log.error("Producto con ID {} no encontrado", productoId);
      throw new ProductoNotFoundException(productoId);
    }

    //Obtener los colores asociados al producto
    List<Color> colores = colorRepository.findByProductoId(productoId);

    //Mapear a respuestas y devolver
    return colores.stream()
            .map(colorMapper::toResponse)
            .collect(Collectors.toList());
  }

  /**
   * Método para actualizar un color existente
   * @param id ID del color a actualizar
   * @param colorRequest objeto que contiene la nueva información del color
   * @return objeto ColorResponse con la información del color actualizado
   */
  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public ColorResponse actualizarColor(Long id, ColorRequest colorRequest) {
    log.info("Actualizando color con ID {}", id);

    // Buscar el color por ID
    Color color = colorRepository.findById(id)
        .orElseThrow(() -> {
          log.error("Color con ID {} no encontrado", id);
          return new ResourceNotFoundException("Color", "id", id);
        });

    // Verificar si ya existe un color con el mismo nombre para este producto
    if (!color.getNombre().equals(colorRequest.getNombre()) && colorRepository.existsByNombreAndProductoId(colorRequest.getNombre(), color.getProducto().getId())) {
      log.error("Ya existe un color con el nombre {} para este producto", colorRequest.getNombre());
      throw new BusinessException("Ya existe un color con el nombre para este producto");
    }

    // Actualizar datos del color
    color.setNombre(colorRequest.getNombre());

    //Actualizar tallas si se proporcionan
    if (colorRequest.getTallas() != null && !colorRequest.getTallas().isEmpty()) {
      //Si ya existian tallas, limpiarlas primero para evitar duplicados
      color.getTallas().clear();

      //Mapear nuevas tallas
      colorMapper.mapTallas(color, colorRequest.getTallas());
    }

    // Guardar el color actualizado
    color = colorRepository.save(color);
    log.info("Color actualizado exitosamente con ID {}", color.getId());

    return colorMapper.toResponse(color);
  }

  /**
   * Método para eliminar un color por su ID
   * @param id ID del color a eliminar
   */
  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public void eliminarColor(Long id) {
    log.info("Elimniado color con ID {}", id);

    //Verificar si el color existe
    Color color = colorRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Color con ID {} no encontrado", id);
              return new ResourceNotFoundException("Color", "id", id);
            });

    //Verificar si el color tiene inventario asociado
    if (inventarioRepository.existsByColorId(id)) {
      log.error("No se puede eliminar el color con ID {} porque tiene inventario asociado", id);
      throw new BusinessException("No se puede eliminar el color porque tiene inventario asociado");
    }

    //Eliminar tallas asociadas primero
    for (Talla talla : color.getTallas()) {
      //Verificar si la talla tiene inventario asociado
      if (inventarioRepository.existsByTallaId(talla.getId())) {
        log.error("No se puede eliminar la talla con ID {} porque tiene inventario asociado", talla.getId());
        throw new BusinessException("No se puede eliminar la talla porque tiene inventario asociado");
      }
    }

    //Eliminar el color
    colorRepository.delete(color);
    log.info("Color eliminado exitosamente con ID {}", id);

  }
}
