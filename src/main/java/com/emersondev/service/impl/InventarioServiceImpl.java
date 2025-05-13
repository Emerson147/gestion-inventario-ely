package com.emersondev.service.impl;

import com.emersondev.api.request.InventarioRequest;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.domain.entity.*;
import com.emersondev.domain.exception.*;
import com.emersondev.domain.repository.*;
import com.emersondev.mapper.InventarioMapper;
import com.emersondev.service.interfaces.InventarioService;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceImpl implements InventarioService {

  private final InventarioRepository inventarioRepository;
  private final ProductoRepository productoRepository;
  private final ColorRepository colorRepository;
  private final TallaRepository tallaRepository;
  private final AlmacenRepository almacenRepository;
  private final InventarioMapper inventarioMapper;
  private final SerieGenerator serieGenerator;
  private final VentaRepository ventaRepository;

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public InventarioResponse agregarInventario(InventarioRequest request) {
    log.info("Agregando nuevo inventario para producto ID: {}", request.getProductoId());

    // Obtener y validar producto
    Producto producto = productoRepository.findById(request.getProductoId())
            .orElseThrow(() -> new ProductoNotFoundException(request.getProductoId()));

    // Obtener y validar color
    Color color = colorRepository.findById(request.getColorId())
            .orElseThrow(() -> new ResourceNotFoundException("Color", "id", request.getColorId().toString()));

    // Validar que el color pertenezca al producto
    if (!color.getProducto().getId().equals(producto.getId())) {
      throw new BusinessException("El color especificado no pertenece al producto");
    }

    // Obtener y validar talla
    Talla talla = tallaRepository.findById(request.getTallaId())
            .orElseThrow(() -> new ResourceNotFoundException("Talla", "id", request.getTallaId().toString()));

    // Validar que la talla pertenezca al color
    if (!talla.getColor().getId().equals(color.getId())) {
      throw new BusinessException("La talla especificada no pertenece al color");
    }

    // Verificamos que la cantidad no pueda ser negativa
    if (request.getCantidad() <= 0) {
      throw new BusinessException("La cantidad no puede ser negativa");
    }

    // Obtener y validar almacén
    Almacen almacen = almacenRepository.findById(request.getAlmacenId())
            .orElseThrow(() -> new AlmacenNotFoundException(request.getAlmacenId()));

    // Validar si ya existe inventario para esta combinación
    boolean existeInventario = inventarioRepository.existsByProductoIdAndColorIdAndTallaIdAndAlmacenId(
            request.getProductoId(),
            request.getColorId(),
            request.getTallaId(),
            request.getAlmacenId()
    );

    if (existeInventario) {
      throw new BusinessException("Ya existe un registro de inventario para esta combinación producto/color/talla/almacén");
    }

    // Crear nuevo inventario
    Inventario inventario = new Inventario();
    inventario.setProducto(producto);
    inventario.setColor(color);
    inventario.setTalla(talla);
    inventario.setAlmacen(almacen);
    inventario.setCantidad(request.getCantidad());
    inventario.setSerie(serieGenerator.generarSerieInventario(producto, color, talla));
    inventario.actualizarEstado();

    // Guardar en base de datos
    inventario = inventarioRepository.save(inventario);
    log.info("Inventario agregado correctamente con ID: {}", inventario.getId());

    return inventarioMapper.toResponse(inventario);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario")
  public List<InventarioResponse> obtenerTodoElInventario() {
    log.debug("Obteniendo todo el inventario");
    List<Inventario> inventarios = inventarioRepository.findAll();

    return inventarios.stream()
            .map(inventarioMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'id-' + #id")
  public InventarioResponse obtenerInventarioPorId(Long id) {
    log.debug("Obteniendo inventario con ID: {}", id);
    Inventario inventario = inventarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Inventario no encontrado con ID: {}", id);
              return new InventarioNotFoundException(id);
            });

    return inventarioMapper.toResponse(inventario);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'serie-' + #serie")
  public InventarioResponse obtenerInventarioPorSerie(String serie) {
    log.debug("Obteniendo inventario con serie: {}", serie);

    // Verificamos que la serie no sea nula o vacía
    Inventario inventario = inventarioRepository.findBySerie(serie)
            .orElseThrow(() -> {
              log.error("Inventario no encontrado con serie: {}", serie);
              return new InventarioNotFoundException(serie);
            });

    return inventarioMapper.toResponse(inventario);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'producto-' + #productoId")
  public List<InventarioResponse> obtenerInventarioPorProducto(Long productoId) {
    log.debug("Obteniendo inventario para producto con ID: {}", productoId);

    // Verificamos que el producto exista
    if (!productoRepository.existsById(productoId)) {
      log.error("Producto no encontrado con ID: {}", productoId);
      throw new ProductoNotFoundException(productoId);
    }

    log.debug("Obteniendo inventario para producto con ID: {}", productoId);
    List<Inventario> inventarios = inventarioRepository.findByProductoId(productoId);

    return inventarios.stream()
            .map(inventarioMapper::toResponse)
            .collect(Collectors.toList());
  }


  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public InventarioResponse actualizarInventario(Long id, InventarioRequest request) {
    log.info("Actualizando inventario con ID: {}", id);

    //Obtener y validar el inventario
    Inventario inventario = inventarioRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Inventario no encontrado con ID: {}", id);
              return new InventarioNotFoundException(id);
            });

    // Verificar si hay cambio de producto/color/talla/almacén
    boolean cambioRelaciones = false;

    if (!inventario.getProducto().getId().equals(request.getProductoId()) ||
            !inventario.getColor().getId().equals(request.getColorId()) ||
            !inventario.getTalla().getId().equals(request.getTallaId()) ||
            !inventario.getAlmacen().getId().equals(request.getAlmacenId())) {
      cambioRelaciones = true;
    }

    // Si hay cambio, verificar que no exista otro inventario para esa combinación
    if (cambioRelaciones) {
      boolean existeInventario = inventarioRepository.existsByProductoIdAndColorIdAndTallaIdAndAlmacenIdAndIdNot(
              request.getProductoId(),
              request.getColorId(),
              request.getTallaId(),
              request.getAlmacenId(),
              id
      );

      if (existeInventario) {
        throw new BusinessException("Ya existe un registro de inventario para esta combinación producto/color/talla/almacén");
      }

      // Obtener y validar producto
      Producto producto = productoRepository.findById(request.getProductoId())
              .orElseThrow(() -> new ProductoNotFoundException(request.getProductoId()));

      // Obtener y validar color
      Color color = colorRepository.findById(request.getColorId())
              .orElseThrow(() -> new ResourceNotFoundException("Color", "id", request.getColorId().toString()));

      // Validar que el color pertenezca al producto
      if (!color.getProducto().getId().equals(producto.getId())) {
        throw new BusinessException("El color especificado no pertenece al producto");
      }

      // Obtener y validar talla
      Talla talla = tallaRepository.findById(request.getTallaId())
              .orElseThrow(() -> new ResourceNotFoundException("Talla", "id", request.getTallaId().toString()));

      // Validar que la talla pertenezca al color
      if (!talla.getColor().getId().equals(color.getId())) {
        throw new BusinessException("La talla especificada no pertenece al color");
      }

      // Verificamos que la cantidad no pueda ser negativa
      if (request.getCantidad() <= 0) {
        throw new BusinessException("La cantidad no puede ser negativa");
      }

      // Obtener y validar almacén
      Almacen almacen = almacenRepository.findById(request.getAlmacenId())
              .orElseThrow(() -> new AlmacenNotFoundException(request.getAlmacenId()));

      // Actualizar relaciones
      inventario.setProducto(producto);
      inventario.setColor(color);
      inventario.setTalla(talla);
      inventario.setAlmacen(almacen);
    }

    // Actualizar cantidad
    inventario.setCantidad(request.getCantidad());

    // Guardar cambios
    inventario = inventarioRepository.save(inventario);
    log.info("Inventario actualizado exitosamente");

    return inventarioMapper.toResponse(inventario);

  }

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public void eliminarInventario(Long id) {
    log.info("Eliminando inventario con ID: {}", id);

    //Verificar que exista el inventario
    if (!inventarioRepository.existsById(id)) {
      log.error("Inventario no encontrado con ID: {}", id);
      throw new InventarioNotFoundException(id);
    }

    //Eliminar inventario
    inventarioRepository.deleteById(id);
    log.info("Inventario eliminado correctamente");
  }

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public void transferirInventario(Long inventarioId, Long almacenDestinoId, Integer cantidad) {
    log.info("Transfiriendo {} unidades del inventario {} al almacén {}",
            cantidad, inventarioId, almacenDestinoId);

    // Validamos que el inventario y almacén destino existan
    Inventario inventarioOrigen = inventarioRepository.findById(inventarioId)
            .orElseThrow(() -> {
              log.error("Inventario origen no encontrado con ID: {}", inventarioId);
              return new InventarioNotFoundException(inventarioId);
            });

    // Validar stock disponible
    if (inventarioOrigen.getCantidad() < cantidad) {
      throw new StockInsuficienteException(
              "No hay suficiente stock disponible para transferir",
              inventarioOrigen.getCantidad(),
              cantidad);
    }

    // Obtener y validar almacen destino
    Almacen almacenDestino = almacenRepository.findById(almacenDestinoId)
            .orElseThrow(() -> {
              log.error("Almacén destino no encontrado con ID: {}", almacenDestinoId);
              return new AlmacenNotFoundException(almacenDestinoId);
            });

    // Validamos que no sea el mismo almacén
    if (inventarioOrigen.getAlmacen().getId().equals(almacenDestinoId)) {
      throw new BusinessException("No se puede transferir al mismo almacén origen");
    }

    // Validamos que la cantidad a transferir sea válida
    if (cantidad <= 0) {
      throw new BusinessException("La cantidad a transferir debe ser mayor a cero");
    }


    // Verificamos si ya existe un registro de inventario para esta combinación en el almacén destino
    Inventario inventarioDestino = inventarioRepository.findByProductoColorTallaAlmacen(
            inventarioOrigen.getProducto().getId(),
            inventarioOrigen.getColor().getId(),
            inventarioOrigen.getTalla().getId(),
            almacenDestino.getId()
    ).orElse(null);

    if (inventarioDestino != null) {
      // Si ya existe, actualizamos la cantidad
      log.debug("Actualizando inventario destino existente, cantidad anterior: {}", inventarioDestino.getCantidad());
      inventarioDestino.setCantidad(inventarioDestino.getCantidad() + cantidad);
      inventarioDestino.actualizarEstado();
      inventarioRepository.save(inventarioDestino);

    } else {
      // Si no existe, creamos un nuevo registro
      log.debug("Creando nuevo registro de inventario en destino");
      inventarioDestino = new Inventario();
      inventarioDestino.setProducto(inventarioOrigen.getProducto());
      inventarioDestino.setColor(inventarioOrigen.getColor());
      inventarioDestino.setTalla(inventarioOrigen.getTalla());
      inventarioDestino.setAlmacen(almacenDestino);
      inventarioDestino.setCantidad(cantidad);
      inventarioDestino.setSerie(serieGenerator.generarSerieInventario(
              inventarioOrigen.getProducto(),
              inventarioOrigen.getColor(),
              inventarioOrigen.getTalla()
      ));
      inventarioDestino.actualizarEstado();
      inventarioRepository.save(inventarioDestino);
    }

    // Actualizar cantidad en origen
    inventarioOrigen.setCantidad(inventarioOrigen.getCantidad() - cantidad);
    log.debug("Actualizado inventario origen, nueva cantidad: {}", inventarioOrigen.getCantidad());
    inventarioRepository.save(inventarioOrigen);

    log.info("Transferencia completada exitosamente");
  }

  @Override
  @Transactional
  public List<InventarioResponse> obtenerInventarioConStockBajo(Integer umbral) {
    log.debug("Obteniendo inventario con stock bajo (umbral: {})", umbral);

    List<Inventario> inventarios = inventarioRepository.findByCantidadLessThanEqual(umbral);

    return inventarios.stream()
            .map(inventarioMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'stock-total-producto-' + #productoId")
  public Integer obtenerStockTotalProducto(Long productoId) {

    //Verificar que el producto exista
    if (!productoRepository.existsById(productoId)) {
      throw new ProductoNotFoundException(productoId);
    }

    return inventarioRepository.calcularStockTotalProducto(productoId);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'stock-variante-' + #productoId + '-' + #colorId + '-' + #tallaId")
  public Integer obtenerStockPorVariante(Long productoId, Long colorId, Long tallaId) {
    log.debug("Obteniendo stock para producto: {}, color: {}, talla: {}",
            productoId, colorId, tallaId);

    // Verificamos que existan producto, color y talla
    if (!productoRepository.existsById(productoId)) {
      throw new ProductoNotFoundException(productoId);
    }

    if (!colorRepository.existsById(colorId)) {
      throw new ResourceNotFoundException("Color", "id", colorId);
    }

    if (!tallaRepository.existsById(tallaId)) {
      throw new ResourceNotFoundException("Talla", "id", tallaId);
    }

    return inventarioRepository.calcularStockPorVariante(productoId, colorId, tallaId);

  }

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public void actualizarStockPorVenta(Long productoId, Long colorId, Long tallaId, Integer cantidad, Long ventaId) {
    log.info("Actualizando stock por venta: producto {}, color {}, talla {}, cantidad {}",
            productoId, colorId, tallaId, cantidad);

    // Verificar el ID de ventas
    Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", ventaId));

    // Verificar que haya suficiente stock
    Integer stockDisponible = obtenerStockPorVariante(productoId, colorId, tallaId);
    if (stockDisponible < cantidad) {
      throw new StockInsuficienteException(
              "No hay suficiente stock disponible para realizar la venta",
              stockDisponible,
              cantidad);
    }

    // Buscar inventarios para esta variante
    List<Inventario> inventarios = inventarioRepository.findByProductoIdAndColorIdAndTallaIdOrderByFechaCreacionAsc(
            productoId, colorId, tallaId);

    int cantidadRestante = cantidad;

    // Recorrer inventarios y actualizar cantidades (FIFO)
    for (Inventario inventario : inventarios) {
      if (cantidadRestante <= 0) break;
      int cantidadDisponible = inventario.getCantidad();
      int cantidadAActualizar = Math.min(cantidadDisponible, cantidadRestante);

      // Crear movimiento
      MovimientoInventario movimiento = new MovimientoInventario();
      movimiento.setInventario(inventario);
      movimiento.setVenta(venta);
      movimiento.setReferencia("Venta #" + venta.getNumeroVenta());
      movimiento.setTipo(MovimientoInventario.TipoMovimiento.SALIDA);
      movimiento.setCantidad(-cantidadAActualizar);
      movimiento.setDescripcion("Venta de " + cantidadAActualizar + " unidades");
      String usuario;
      try {
        usuario = SecurityContextHolder.getContext().getAuthentication().getName();
      } catch (Exception e) {
        usuario = "anonymous";
      }
      movimiento.setUsuario(usuario);
      movimiento.setFechaMovimiento(LocalDateTime.now());

      inventario.agregarMovimiento(movimiento);
      inventario.setCantidad(cantidadDisponible -  cantidadAActualizar);
      cantidadRestante -= cantidadAActualizar;
      inventarioRepository.save(inventario);
    }

    log.info("Stock actualizado correctamente por venta");

  }

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public void disminuirStock(Long inventarioId, Integer cantidad) {
    log.info("Disminuyendo stock de inventario ID: {} en {} unidades", inventarioId, cantidad);

    Inventario inventario = inventarioRepository.findById(inventarioId)
            .orElseThrow(() -> {
              log.error("Inventario no encontrado con ID: {}", inventarioId);
              return new InventarioNotFoundException(inventarioId);
            });

    if (inventario.getCantidad() < cantidad) {
      log.error("Stock insuficiente. Solicitado: {}, Disponible: {}", cantidad, inventario.getCantidad());
      throw new StockInsuficienteException(
              inventario.getSerie(),
              cantidad,
              inventario.getCantidad()
      );
    }

    inventario.setCantidad(inventario.getCantidad() - cantidad);
    inventarioRepository.save(inventario);
    log.debug("Stock disminuido correctamente. Nueva cantidad: {}", inventario.getCantidad());
  }

  @Override
  @Transactional
  @CacheEvict(value = {"inventario", "productos"}, allEntries = true)
  public void devolverStockPorAnulacion(Long productoId, Long colorId, Long tallaId, Integer cantidad, Long ventaId) {
    log.info("Devolviendo stock por anulacion: producto {}, color {}, talla {}, cantidad {}", productoId, colorId, tallaId, cantidad);

    if (cantidad <= 0) {
      throw new BusinessException("La cantidad a devolver debe ser mayor a cero");
    }

    Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> {
              log.error("Venta no encontrada con ID: {}", ventaId);
              return new ResourceNotFoundException("Venta", "id", ventaId);
            });

    Inventario inventario = inventarioRepository.findByProductoIdAndColorIdAndTallaIdOrderByFechaCreacionAsc(
            productoId, colorId, tallaId)
            .stream().findFirst()
            .orElseThrow(() -> new InventarioNotFoundException("No se encontró inventario para la combinación especificada"));

    MovimientoInventario movimiento = new MovimientoInventario();
    movimiento.setInventario(inventario);
    movimiento.setVenta(venta);
    movimiento.setReferencia("Anulación Venta #" + venta.getNumeroVenta()); // o ID de Venta
    movimiento.setTipo(MovimientoInventario.TipoMovimiento.ENTRADA);
    movimiento.setCantidad(cantidad);
    movimiento.setDescripcion("Devolución por anulación");
    String usuario;
    try {
      usuario = SecurityContextHolder.getContext().getAuthentication().getName();
    } catch (Exception e) {
      usuario = "anonymous";
    }
    movimiento.setUsuario(usuario);
    movimiento.setFechaMovimiento(LocalDateTime.now());

    inventario.agregarMovimiento(movimiento);
    inventario.setCantidad(inventario.getCantidad() + cantidad);
    inventario.actualizarEstado();
    inventarioRepository.save(inventario);

    log.info("Stock devuelto correctamente por anulación");
  }

  @Override
  public long contarProductosSinStock() {
    return 0;
  }



}
