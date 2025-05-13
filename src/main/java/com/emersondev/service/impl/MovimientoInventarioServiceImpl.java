package com.emersondev.service.impl;

import com.emersondev.api.request.MovimientoInventarioRequest;
import com.emersondev.api.response.MovimientoInventarioResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.domain.entity.Inventario;
import com.emersondev.domain.entity.MovimientoInventario;
import com.emersondev.domain.entity.Venta;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.*;
import com.emersondev.mapper.MovimientoInventarioMapper;
import com.emersondev.service.interfaces.MovimientoInventarioService;
import com.emersondev.util.PaginationUtils;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

  private final InventarioRepository inventarioRepository;
  private final MovimientoInventarioRepository movimientoRepository;
  private final MovimientoInventarioMapper movimientoMapper;
  private final VentaRepository ventaRepository;
  private final ProductoRepository productoRepository;
  private final ColorRepository colorRepository;
  private final TallaRepository tallaRepository;
  private final AlmacenRepository almacenRepository;
  private final SerieGenerator serieGenerator;

  @Override
  @Transactional
  @CacheEvict(value = {"productos", "inventarios"}, allEntries = true)
  public MovimientoInventarioResponse registrarMovimiento(MovimientoInventarioRequest movimientoRequest) {
    log.info("Registrando movimiento para inventario ID {}", movimientoRequest.getInventarioId());

    // Obtener el inventario
    Inventario inventario = inventarioRepository.findById(movimientoRequest.getInventarioId())
            .orElseThrow(() -> {
              log.error("Inventario con ID {} no encontrado", movimientoRequest.getInventarioId());
              return new ResourceNotFoundException("Inventario", "id", movimientoRequest.getInventarioId());
            });

    // Validar y convertir el tipo de movimiento
    MovimientoInventario.TipoMovimiento tipo;
    try {
      tipo = MovimientoInventario.TipoMovimiento.valueOf(movimientoRequest.getTipo());
    } catch (IllegalArgumentException e) {
      log.error("Tipo de movimiento inválido: {}", movimientoRequest.getTipo());
      throw new BusinessException("Tipo de movimiento inválido. Debe ser ENTRADA, SALIDA, AJUSTE o TRASLADO");
    }

    // Validar que la cantidad sea positiva para entradas y salidas
    if (movimientoRequest.getCantidad() <= 0 && tipo != MovimientoInventario.TipoMovimiento.AJUSTE) {
      throw new BusinessException("La cantidad debe ser mayor a cero para " + tipo);
    }
    if (tipo == MovimientoInventario.TipoMovimiento.AJUSTE && movimientoRequest.getCantidad() < 0) {
      throw new BusinessException("La cantidad de ajuste no puede ser negativa");
    }

    // Validar que hay suficiente stock para una salida
    if (tipo == MovimientoInventario.TipoMovimiento.SALIDA && inventario.getCantidad() < movimientoRequest.getCantidad()) {
      log.error("Stock insuficiente para realizar la salida. Stock actual: {}, Solicitado: {}",
              inventario.getCantidad(), movimientoRequest.getCantidad());
      throw new BusinessException("Stock insuficiente para realizar la salida");
    }

    // Obtener venta  (si aplica)
    Venta venta = null;
    if (movimientoRequest.getVentaId() != null) {
      venta = ventaRepository.findById(movimientoRequest.getVentaId())
              .orElseThrow(() -> {
                log.error("Venta con ID {} no encontrada", movimientoRequest.getVentaId());
                return new ResourceNotFoundException("Venta", "id", movimientoRequest.getVentaId());
              });
    }

    // Crear el movimiento
    MovimientoInventario movimiento = new MovimientoInventario();
    movimiento.setInventario(inventario);
    movimiento.setVenta(venta);
    movimiento.setReferencia(movimientoRequest.getReferencia());
    movimiento.setCantidad(movimientoRequest.getCantidad());
    movimiento.setTipo(tipo);
    movimiento.setDescripcion(movimientoRequest.getDescripcion());
    movimiento.setFechaMovimiento(LocalDateTime.now());

    // Obtener usuario del contexto de seguridad si no se proporciona
    String usuario = movimientoRequest.getUsuario();
    if (usuario == null || usuario.trim().isEmpty()) {
      try {
        usuario = SecurityContextHolder.getContext().getAuthentication().getName();
      } catch (Exception e) {
        usuario = "MigatteDev"; // Valor por defecto basado en la información proporcionada
      }
    }
    movimiento.setUsuario(usuario);

    // Actualizar la cantidad en el inventario según el tipo de movimiento
    switch (tipo) {
      case ENTRADA:
        inventario.setCantidad(inventario.getCantidad() + movimientoRequest.getCantidad());
        break;
      case SALIDA:
        inventario.setCantidad(inventario.getCantidad() - movimientoRequest.getCantidad());
        break;
      case AJUSTE:
        // Para ajustes, la cantidad representa el nuevo valor absoluto
        inventario.setCantidad(movimientoRequest.getCantidad());
        break;
      case TRASLADO:
        // 1. Validar que el inventario de origen y el destino tengan mismo producto/color/talla, solo cambia el almacén

        // Obtener datos del inventario de origen
        Long productoIdOrigen = inventario.getProducto().getId();
        Long colorIdOrigen = inventario.getColor().getId();
        Long tallaIdOrigen = inventario.getTalla().getId();

        Inventario inventarioDestino = null;

        if (movimientoRequest.getInventarioDestinoId() != null) {
          // Buscar inventario destino por ID
          inventarioDestino = inventarioRepository.findById(movimientoRequest.getInventarioDestinoId())
                  .orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", movimientoRequest.getInventarioDestinoId()));

          // Validar que destino tenga mismo producto/color/talla
          if (!inventarioDestino.getProducto().getId().equals(productoIdOrigen) ||
                  !inventarioDestino.getColor().getId().equals(colorIdOrigen) ||
                  !inventarioDestino.getTalla().getId().equals(tallaIdOrigen)) {
            throw new BusinessException("El inventario destino debe ser del mismo producto, color y talla que el inventario origen.");
          }
        } else {
          // Buscar inventario destino por producto/color/talla/almacenDestinoId
          if (movimientoRequest.getAlmacenDestinoId() == null) {
            throw new BusinessException("Debe especificar el ID del almacén destino para el traslado.");
          }

          inventarioDestino = inventarioRepository.findByProductoIdAndColorIdAndTallaIdAndAlmacenId(
                  productoIdOrigen, colorIdOrigen, tallaIdOrigen, movimientoRequest.getAlmacenDestinoId()
          ).orElse(null);

          // Si no existe, lo creas solo si es **la misma variante**
          if (inventarioDestino == null) {
            inventarioDestino = new Inventario();
            inventarioDestino.setProducto(inventario.getProducto());
            inventarioDestino.setColor(inventario.getColor());
            inventarioDestino.setTalla(inventario.getTalla());
            inventarioDestino.setAlmacen(almacenRepository.getReferenceById(movimientoRequest.getAlmacenDestinoId()));
            inventarioDestino.setCantidad(0);
            inventarioDestino.setEstado(Inventario.EstadoInventario.DISPONIBLE);

            // Generar serie para el nuevo inventario
            String nuevaSerie = serieGenerator.generarSerieInventario(
                    inventario.getProducto(),
                    inventario.getColor(),
                    inventario.getTalla());

            inventarioDestino.setSerie(nuevaSerie);

            inventarioDestino = inventarioRepository.save(inventarioDestino);
          }
        }

        // Validar stock en origen
        if (inventario.getCantidad() < movimientoRequest.getCantidad()) {
          throw new BusinessException("Stock insuficiente para traslado");
        }

        // Restar del inventario origen
        inventario.setCantidad(inventario.getCantidad() - movimientoRequest.getCantidad());
        inventario.actualizarEstado();

        // Sumar al inventario destino
        inventarioDestino.setCantidad(inventarioDestino.getCantidad() + movimientoRequest.getCantidad());
        inventarioDestino.actualizarEstado();

        // Registrar movimiento de salida en origen
        movimiento.setInventario(inventario);
        movimiento.setInventarioDestino(inventarioDestino);
        movimiento.setTipo(MovimientoInventario.TipoMovimiento.TRASLADO);
        inventario.agregarMovimiento(movimiento);

        // Registrar movimiento de entrada en destino
        MovimientoInventario movimientoEntrada = new MovimientoInventario();
        movimientoEntrada.setInventario(inventarioDestino);
        movimientoEntrada.setTipo(MovimientoInventario.TipoMovimiento.ENTRADA);
        movimientoEntrada.setCantidad(movimientoRequest.getCantidad());
        movimientoEntrada.setReferencia(movimientoRequest.getReferencia());
        movimientoEntrada.setDescripcion("Traslado desde inventario " + inventario.getId());
        movimientoEntrada.setUsuario(usuario);
        movimientoEntrada.setFechaMovimiento(LocalDateTime.now());
        inventarioDestino.agregarMovimiento(movimientoEntrada);

        inventarioRepository.save(inventario);
        inventarioRepository.save(inventarioDestino);

        return movimientoMapper.toResponse(movimiento); // El de salida, con inventarioDestino seteado
    }

    // Usar el método de la entidad para agregar el movimiento
    inventario.agregarMovimiento(movimiento);

    // Actualizar el estado del inventario
    inventario.actualizarEstado();

    // Guardar el inventario (cascadea al movimiento)
    inventario = inventarioRepository.save(inventario);

    log.info("Movimiento registrado exitosamente. Nuevo stock: {}, Estado: {}",
            inventario.getCantidad(), inventario.getEstado());

    return movimientoMapper.toResponse(movimiento);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventarios", key = "'movimientos-inventario-' + #inventarioId + '-' + #page + '-' + #size")
  public PagedResponse<MovimientoInventarioResponse> obtenerMovimientosPorInventario(
          Long inventarioId, int page, int size) {

    log.info("Obteniendo movimientos para inventario ID {}, page={}, size={}", inventarioId, page, size);

    // Verificar que el inventario existe
    if (!inventarioRepository.existsById(inventarioId)) {
      log.error("Inventario con ID {} no encontrado", inventarioId);
      throw new ResourceNotFoundException("Inventario", "id", inventarioId);
    }

    // Validar parámetros de paginación
    int[] validatedParams = PaginationUtils.validatePaginationParams(page, size);
    page = validatedParams[0];
    size = validatedParams[1];

    // Crear objeto pageable
    Pageable pageable = PaginationUtils.createPageable(page, size, "fechaMovimiento", "desc");

    // Ejecutar la consulta paginada
    Page<MovimientoInventario> movimientoPage = movimientoRepository.findByInventarioId(inventarioId, pageable);

    // Si no hay resultados, devolver una respuesta vacía
    if (movimientoPage.isEmpty()) {
      log.info("No se encontraron movimientos para el inventario ID {}", inventarioId);
      return PaginationUtils.emptyPagedResponse(page, size);
    }

    // Convertir y devolver las respuestas paginadas
    return PaginationUtils.createPagedResponse(movimientoPage, movimientoMapper::toResponse);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventarios", key = "'movimiento-' + #id")
  public MovimientoInventarioResponse obtenerMovimientoPorId(Long id) {
    log.info("Obteniendo movimiento con ID {}", id);

    MovimientoInventario movimiento = movimientoRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Movimiento con ID {} no encontrado", id);
              return new ResourceNotFoundException("MovimientoInventario", "id", id);
            });

    return movimientoMapper.toResponse(movimiento);
  }

  @Override
  @Transactional
  public PagedResponse<MovimientoInventarioResponse> buscarMovimientos(
          Long inventarioId, Long productoId, Long colorId, Long tallaId,
          MovimientoInventario.TipoMovimiento tipo, LocalDateTime fechaInicio, LocalDateTime fechaFin,
          int page, int size, String sortBy, String sortDir) {

    log.info("Buscando movimientos con filtros, page={}, size={}", page, size);

    // Validar parámetros de paginación
    int[] validatedParams = PaginationUtils.validatePaginationParams(page, size);
    page = validatedParams[0];
    size = validatedParams[1];

    // Crear objeto pageable
    Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);

    // Ejecutar la consulta paginada con filtros
    Page<MovimientoInventario> movimientoPage = movimientoRepository.buscarMovimientos(
            inventarioId, productoId, colorId, tallaId,
            tipo, fechaInicio, fechaFin, pageable);

    // Si no hay resultados, devolver una respuesta vacía
    if (movimientoPage.isEmpty()) {
      log.info("No se encontraron movimientos para los filtros especificados");
      return PaginationUtils.emptyPagedResponse(page, size);
    }

    // Convertir y devolver las respuestas paginadas
    return PaginationUtils.createPagedResponse(movimientoPage, movimientoMapper::toResponse);
  }
}
