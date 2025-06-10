package com.emersondev.service.impl;

import com.emersondev.api.request.DetalleVentaRequest;
import com.emersondev.api.request.MovimientoInventarioRequest;
import com.emersondev.api.request.VentaRequest;
import com.emersondev.api.response.ReporteVentasResponse;
import com.emersondev.api.response.VentaResponse;
import com.emersondev.domain.entity.*;
import com.emersondev.domain.exception.*;
import com.emersondev.domain.repository.*;
import com.emersondev.mapper.VentaMapper;
import com.emersondev.service.interfaces.InventarioService;
import com.emersondev.service.interfaces.VentaService;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaServiceImpl implements VentaService {

  private final VentaRepository ventaRepository;
  private final DetalleVentaRepository detalleVentaRepository;
  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;
  private final ProductoRepository productoRepository;
  private final ColorRepository colorRepository;
  private final TallaRepository tallaRepository;
  private final InventarioService inventarioService;
  private final VentaMapper ventaMapper;
  private final SerieGenerator serieGenerator;
  private final InventarioRepository inventarioRepository;
  private final MovimientoInventarioServiceImpl movimientoService;

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "inventario", "reportes"}, allEntries = true)
  public VentaResponse registrarVenta(VentaRequest ventaRequest) {
    log.info("Registrando nueva venta para cliente ID: {}", ventaRequest.getClienteId());

    // Validar cliente
    Clientes cliente = clienteRepository.findById(ventaRequest.getClienteId())
            .orElseThrow(() -> new ClienteNotFoundException(ventaRequest.getClienteId()));

    // Validar que el cliente esté activo
    if (!cliente.getEstado()) {
      throw new BusinessException("No se puede realizar una venta con un cliente inactivo");
    }

    // Validar usuario
    Usuario usuario = usuarioRepository.findById(ventaRequest.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", ventaRequest.getUsuarioId().toString()));

    // Validar y agregar detalles de la venta
    if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
      throw new BusinessException("La venta debe tener al menos un detalle");
    }

    // Crear venta
    Venta venta = new Venta();
    venta.setNumeroVenta(serieGenerator.generarNumeroVenta());
    venta.setCliente(cliente);
    venta.setUsuario(usuario);
    venta.setTipoComprobante(ventaRequest.getTipoComprobante());
    venta.setSerieComprobante(ventaRequest.getSerieComprobante());
    venta.setNumeroComprobante(ventaRequest.getNumeroComprobante());
    venta.setObservaciones(ventaRequest.getObservaciones());
    venta.setEstado(Venta.EstadoVenta.PENDIENTE);

    BigDecimal subtotal = BigDecimal.ZERO;
    List<DetalleVenta> detalleVentas = new ArrayList<>();

    // Procesar cada detalle
    for (DetalleVentaRequest detalleRequest : ventaRequest.getDetalles()) {
      // Validar Cantidad
      if (detalleRequest.getCantidad() <= 0 ) {
        throw new BusinessException("La cantidad debe ser mayor a cero");
      }

      // Buscar inventario por ID
      Inventario inventario = inventarioRepository.findById(detalleRequest.getInventarioId())
              .orElseThrow(() -> new InventarioNotFoundException("No se encontro inventario con el id: " + detalleRequest.getInventarioId()));

      // Validar stock suficiente
      if (inventario.getCantidad() < detalleRequest.getCantidad() ||
              inventario.getEstado() == Inventario.EstadoInventario.AGOTADO ||
              inventario.getEstado() == Inventario.EstadoInventario.RESERVADO) {
        throw new StockInsuficienteException(
                "Stock insuficiente o inventario no disponible para el producto " + inventario.getProducto().getNombre() +
                        " - Color: " + inventario.getColor().getNombre() +
                        " - Talla: " + inventario.getTalla().getNumero(),
                inventario.getCantidad(),
                detalleRequest.getCantidad());
      }

      // Crear detalle de venta
      DetalleVenta detalle = new DetalleVenta();
      detalle.setProducto(inventario.getProducto());
      detalle.setColor(inventario.getColor());
      detalle.setTalla(inventario.getTalla());
      detalle.setCantidad(detalleRequest.getCantidad());
      detalle.setPrecioUnitario(inventario.getProducto().getPrecioVenta());
      detalle.setProductDescription();
      detalle.calcularSubtotal();
      venta.addDetalle(detalle);
      subtotal = subtotal.add(detalle.getSubtotal());
      detalleVentas.add(detalle);
    }
    // Establecer los totales
    venta.setSubtotal(subtotal);
    venta.setIgv(subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_EVEN));
    venta.setTotal(subtotal.add(venta.getIgv()).setScale(2, RoundingMode.HALF_EVEN));
    // venta.setEstado(Venta.EstadoVenta.COMPLETADA);  Cambia a completada tras registrar movimiento

    // 1. Guarda la venta
    try {
      venta = ventaRepository.save(venta);
    } catch (Exception e) {
      log.error("Error al guardar la venta: {}", e.getMessage());
      throw new BusinessException("No se pudo guardar la venta: " + e.getMessage());
    }

    // 2. Registrar movimientos de inventario usando el ID de inventario directamente
    for (int i = 0; i < detalleVentas.size(); i++) {
      DetalleVenta detalle = detalleVentas.get(i);
      DetalleVentaRequest detalleRequest = ventaRequest.getDetalles().get(i); // mismo orden

      MovimientoInventarioRequest movimientoRequest = new MovimientoInventarioRequest();
      movimientoRequest.setInventarioId(detalleRequest.getInventarioId());
      movimientoRequest.setVentaId(venta.getId());
      movimientoRequest.setReferencia("Venta # " + venta.getNumeroVenta());
      movimientoRequest.setTipo("SALIDA");
      movimientoRequest.setCantidad(detalle.getCantidad());
      movimientoRequest.setDescripcion("Venta de " + detalle.getCantidad() + " unidades de " +
              detalle.getProducto().getNombre() + " - " + detalle.getColor().getNombre() + " - Talla " + detalle.getTalla().getNumero());
      movimientoRequest.setUsuario(usuario.getUsername());

      try {
        movimientoService.registrarMovimiento(movimientoRequest);
      } catch (Exception e) {
        log.error("Error al registrar movimiento para detalle {}: {}", detalle.getId(), e.getMessage());
        throw new BusinessException("No se pudo registrar el movimiento de inventario: " + e.getMessage());
      }
    }

    log.info("Venta registrada exitosamente con número: {}", venta.getNumeroVenta());
    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'id-' + #id")
  public VentaResponse obtenerVentaPorId(Long id) {
    log.debug("Obteniendo venta con ID: {}", id);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Venta no encontrada con ID: {}", id);
              return new VentaNotFoundException(id);
            });

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'numero-' + #numeroVenta")
  public VentaResponse obtenerVentaPorNumero(String numeroVenta) {
    log.debug("Obteniendo venta con número: {}", numeroVenta);

    Venta venta = ventaRepository.findByNumeroVenta(numeroVenta)
            .orElseThrow(() -> {
              log.error("Venta no encontrada con número: {}", numeroVenta);
              return new VentaNotFoundException("número", numeroVenta);
            });

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas")
  public List<VentaResponse> obtenerTodasLasVentas() {
    log.debug("Obteniendo todas las ventas");

    List<Venta> ventas = ventaRepository.findAll();

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'estado-' + #estado")
  public List<VentaResponse> obtenerVentasPorEstado(Venta.EstadoVenta estado) {
    log.debug("Obteniendo ventas por estado: {}", estado);

    List<Venta> ventas = ventaRepository.findByEstado(estado);

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'cliente-' + #clienteId")
  public List<VentaResponse> obtenerVentasPorCliente(Long clienteId) {
    log.debug("Obteniendo ventas para cliente ID: {}", clienteId);

    // Verificar que el cliente existe
    if (!clienteRepository.existsById(clienteId)) {
      throw new ClienteNotFoundException(clienteId);
    }

    List<Venta> ventas = ventaRepository.findByClienteId(clienteId);

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'usuario-' + #usuarioId")
  public List<VentaResponse> obtenerVentasPorUsuario(Long usuarioId) {
    log.debug("Obteniendo ventas para usuario ID: {}", usuarioId);

    // Verificar que el usuario existe
    if (!usuarioRepository.existsById(usuarioId)) {
      throw new ResourceNotFoundException("Usuario", "id", usuarioId.toString());
    }

    List<Venta> ventas = ventaRepository.findByUsuarioId(usuarioId);

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "ventas", key = "'fechas-' + #fechaInicio + '-' + #fechaFin")
  public List<VentaResponse> obtenerVentasEntreFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
    log.debug("Obteniendo ventas entre fechas: {} y {}", fechaInicio, fechaFin);

    if (fechaInicio == null || fechaFin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (fechaInicio.isAfter(fechaFin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    List<Venta> ventas = ventaRepository.findByFechaCreacionBetween(fechaInicio, fechaFin);

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "inventario", "reportes"}, allEntries = true)
  public VentaResponse anularVenta(Long id, String motivo) {
    log.info("Anulando venta con ID: {}", id);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new VentaNotFoundException(id));

    if (Venta.EstadoVenta.ANULADA.equals(venta.getEstado())) {
      throw new BusinessException("La venta ya se encuentra anulada");
    }

    // Registrar movimientos de entrada para devolver stock
    for (DetalleVenta detalle : venta.getDetalles()) {
      Inventario inventario = inventarioRepository.findByProductoIdAndColorIdAndTallaIdOrderByFechaCreacionAsc(
                      detalle.getProducto().getId(), detalle.getColor().getId(), detalle.getTalla().getId())
              .stream().findFirst()
              .orElseThrow(() -> new InventarioNotFoundException("No se encontró inventario para la variante"));

      MovimientoInventarioRequest movimientoRequest = new MovimientoInventarioRequest();
      movimientoRequest.setInventarioId(inventario.getId());
      movimientoRequest.setVentaId(venta.getId());
      movimientoRequest.setReferencia("Anulación Venta #" + venta.getNumeroVenta());
      movimientoRequest.setTipo("ENTRADA");
      movimientoRequest.setCantidad(detalle.getCantidad());
      movimientoRequest.setDescripcion("Devolución por anulación de " + detalle.getCantidad() + " unidades de " +
              detalle.getProducto().getNombre() + " - " + detalle.getColor().getNombre() + " - Talla " + detalle.getTalla().getNumero());

      String usuarioActual = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
              ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
              : "system";
      movimientoRequest.setUsuario(usuarioActual);

      movimientoService.registrarMovimiento(movimientoRequest);
    }

    // Actualizar estado y observaciones
    venta.setEstado(Venta.EstadoVenta.ANULADA);
    String observaciones = venta.getObservaciones();
    venta.setObservaciones((observaciones != null ? observaciones + " | " : "") +
            "ANULADA: " + motivo + " [" + LocalDateTime.now() + "]");

    venta = ventaRepository.save(venta);
    log.info("Venta anulada exitosamente");

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "inventario", "reportes"}, allEntries = true)
  public void eliminarVenta(Long id) {
    log.info("Eliminando venta con ID: {}", id);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new VentaNotFoundException(id));

    if (!Venta.EstadoVenta.PENDIENTE.equals(venta.getEstado())) {
      throw new BusinessException("Solo se pueden eliminar ventas en estado PENDIENTE");
    }

    // Registrar movimientos de entrada para devolver stock
    for (DetalleVenta detalle : venta.getDetalles()) {
      Inventario inventario = inventarioRepository.findByProductoIdAndColorIdAndTallaIdOrderByFechaCreacionAsc(
                      detalle.getProducto().getId(), detalle.getColor().getId(), detalle.getTalla().getId())
              .stream().findFirst()
              .orElseThrow(() -> new InventarioNotFoundException("No se encontró inventario para la variante"));

      MovimientoInventarioRequest movimientoRequest = new MovimientoInventarioRequest();
      movimientoRequest.setInventarioId(inventario.getId());
      movimientoRequest.setVentaId(venta.getId());
      movimientoRequest.setReferencia("Eliminación Venta #" + venta.getNumeroVenta());
      movimientoRequest.setTipo("ENTRADA");
      movimientoRequest.setCantidad(detalle.getCantidad());
      movimientoRequest.setDescripcion("Devolución por eliminación de " + detalle.getCantidad() + " unidades de " +
              detalle.getProducto().getNombre() + " - " + detalle.getColor().getNombre() + " - Talla " + detalle.getTalla().getNumero());
      movimientoRequest.setUsuario("system");
      movimientoService.registrarMovimiento(movimientoRequest);
    }

    ventaRepository.delete(venta);
    log.info("Venta eliminada exitosamente");
  }

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "reportes"}, allEntries = true)
  public VentaResponse actualizarEstadoVenta(Long id, Venta.EstadoVenta nuevoEstado) {
    log.info("Actualizando estado de venta ID: {} a {}", id, nuevoEstado);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new VentaNotFoundException(id));

    // Verificar reglas de negocio para cambios de estado
    if (Venta.EstadoVenta.ANULADA.equals(venta.getEstado()) && !Venta.EstadoVenta.ANULADA.equals(nuevoEstado)) {
      throw new BusinessException("Una venta anulada no puede cambiar de estado");
    }
    // Actualizar estado
    venta.setEstado(nuevoEstado);
    // Guardar cambios
    venta = ventaRepository.save(venta);
    log.info("Estado de venta actualizado exitosamente");

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "reportes"}, allEntries = true)
  public VentaResponse actualizarComprobante(Long id, String serieComprobante, String numeroComprobante) {
    log.info("Actualizando datos de comprobante para venta ID: {}", id);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new VentaNotFoundException(id));

    // Verificar que la venta no esté anulada
    if (Venta.EstadoVenta.ANULADA.equals(venta.getEstado())) {
      throw new BusinessException("No se pueden modificar los datos de comprobante de una venta anulada");
    }

    // Actualizar datos de comprobante
    venta.setSerieComprobante(serieComprobante);
    venta.setNumeroComprobante(numeroComprobante);

    // Guardar cambios
    venta = ventaRepository.save(venta);
    log.info("Datos de comprobante actualizados exitosamente");

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "reportes", key = "'reporte-ventas-' + #fechaInicio + '-' + #fechaFin")
  public ReporteVentasResponse generarReporteVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
    log.info("Generando reporte de ventas desde {} hasta {}", fechaInicio, fechaFin);

    if (fechaInicio == null || fechaFin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (fechaInicio.isAfter(fechaFin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    // Obtener ventas en el período
    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndEstado(
            fechaInicio, fechaFin, Venta.EstadoVenta.COMPLETADA);

    // Calcular totales
    BigDecimal totalVentas = BigDecimal.ZERO;
    BigDecimal totalIgv = BigDecimal.ZERO;
    int cantidadVentas = ventas.size();

    Map<Long, BigDecimal> ventasPorCliente = new HashMap<>();
    Map<Long, BigDecimal> ventasPorUsuario = new HashMap<>();
    Map<Long, Integer> productosMasVendidos = new HashMap<>();

    for (Venta venta : ventas) {
      totalVentas = totalVentas.add(venta.getTotal());
      totalIgv = totalIgv.add(venta.getIgv());

      // Agrupar por cliente
      Long clienteId = venta.getCliente().getId();
      ventasPorCliente.put(clienteId,
              ventasPorCliente.getOrDefault(clienteId, BigDecimal.ZERO).add(venta.getTotal()));

      // Agrupar por usuario
      Long usuarioId = venta.getUsuario().getId();
      ventasPorUsuario.put(usuarioId,
              ventasPorUsuario.getOrDefault(usuarioId, BigDecimal.ZERO).add(venta.getTotal()));

      // Productos más vendidos
      for (DetalleVenta detalle : venta.getDetalles()) {
        Long productoId = detalle.getProducto().getId();
        productosMasVendidos.put(productoId,
                productosMasVendidos.getOrDefault(productoId, 0) + detalle.getCantidad());
      }
    }

    // Crear objeto de reporte
    ReporteVentasResponse reporte = new ReporteVentasResponse();
    reporte.setFechaInicio(fechaInicio);
    reporte.setFechaFin(fechaFin);
    reporte.setTotalVentas(totalVentas);
    reporte.setTotalIgv(totalIgv);
    reporte.setCantidadVentas(cantidadVentas);

    // Obtener top 5 clientes
    List<Map.Entry<Long, BigDecimal>> topClientes = ventasPorCliente.entrySet().stream()
            .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
            .limit(5)
            .toList();

    for (Map.Entry<Long, BigDecimal> entry : topClientes) {
      Clientes cliente = clienteRepository.findById(entry.getKey())
              .orElseThrow(() -> new ClienteNotFoundException(entry.getKey()));

      ReporteVentasResponse.ClienteVentas cv = new ReporteVentasResponse.ClienteVentas();
      cv.setClienteId(cliente.getId());
      cv.setNombreCliente(cliente.getNombres() + " " + cliente.getApellidos());
      cv.setTotal(entry.getValue());

      reporte.getTopClientes().add(cv);
    }

    // Obtener top 5 productos
    List<Map.Entry<Long, Integer>> topProductos = productosMasVendidos.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(5)
            .toList();

    for (Map.Entry<Long, Integer> entry : topProductos) {
      Producto producto = productoRepository.findById(entry.getKey())
              .orElseThrow(() -> new ProductoNotFoundException(entry.getKey()));

      ReporteVentasResponse.ProductoVendido pv = new ReporteVentasResponse.ProductoVendido();
      pv.setProductoId(producto.getId());
      pv.setNombreProducto(producto.getNombre());
      pv.setCantidadVendida(entry.getValue());

      reporte.getTopProductos().add(pv);
    }

    log.info("Reporte de ventas generado exitosamente");
    return reporte;
  }

  @Override
  @Transactional(readOnly = true)
  public List<VentaResponse> obtenerVentasPorFecha(LocalDate fecha) {
    log.debug("Obteniendo ventas para fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(23, 59, 59);

    return obtenerVentasEntreFechas(inicio, fin);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> obtenerResumenDiario(LocalDate fecha) {
    log.debug("Obteniendo resumen diario para fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(23, 59, 59);

    // Obtener ventas del día
    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndEstado(
            inicio, fin, Venta.EstadoVenta.COMPLETADA);

    // Calcular totales
    BigDecimal totalVentas = BigDecimal.ZERO;
    int cantidadVentas = ventas.size();
    int cantidadProductos = 0;

    // Ventas por tipo de comprobante
    Map<String, BigDecimal> totalPorComprobante = new HashMap<>();

    // Productos vendidos
    Map<Long, Integer> productosCantidad = new HashMap<>();

    for (Venta venta : ventas) {
      totalVentas = totalVentas.add(venta.getTotal());

      // Contar por tipo de comprobante
      if (venta.getTipoComprobante() != null) {
        String tipoComprobante = venta.getTipoComprobante().name();
        totalPorComprobante.put(tipoComprobante,
                totalPorComprobante.getOrDefault(tipoComprobante, BigDecimal.ZERO).add(venta.getTotal()));
      }

      // Contar productos
      for (DetalleVenta detalle : venta.getDetalles()) {
        cantidadProductos += detalle.getCantidad();

        // Agrupar por producto
        Long productoId = detalle.getProducto().getId();
        productosCantidad.put(productoId,
                productosCantidad.getOrDefault(productoId, 0) + detalle.getCantidad());
      }
    }

    // Preparar resultado
    Map<String, Object> resumen = new HashMap<>();
    resumen.put("fecha", fecha);
    resumen.put("totalVentas", totalVentas);
    resumen.put("cantidadVentas", cantidadVentas);
    resumen.put("cantidadProductos", cantidadProductos);
    resumen.put("ventasPorComprobante", totalPorComprobante);

    // Top 10 productos más vendidos del día
    List<Map.Entry<Long, Integer>> topProductos = productosCantidad.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(10)
            .toList();

    List<Map<String, Object>> productosDetalle = new ArrayList<>();
    for (Map.Entry<Long, Integer> entry : topProductos) {
      Producto producto = productoRepository.findById(entry.getKey())
              .orElseThrow(() -> new ProductoNotFoundException(entry.getKey()));

      Map<String, Object> productoInfo = new HashMap<>();
      productoInfo.put("id", producto.getId());
      productoInfo.put("codigo", producto.getCodigo());
      productoInfo.put("nombre", producto.getNombre());
      productoInfo.put("cantidad", entry.getValue());

      productosDetalle.add(productoInfo);
    }

    resumen.put("productosMasVendidos", productosDetalle);

    return resumen;
  }

  @Override
  @Transactional(readOnly = true)
  public List<VentaResponse> obtenerVentasRecientes(int cantidad) {
    log.debug("Obteniendo {} ventas más recientes", cantidad);

    List<Venta> ventas = ventaRepository.findAll(
                    PageRequest.of(0, cantidad, Sort.by(Sort.Direction.DESC, "fechaCreacion")))
            .getContent();

    return ventas.stream()
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<VentaResponse> buscarVentas(String termino) {
    log.debug("Buscando ventas con término: {}", termino);

    // En una implementación real, tendrías un método repository para esta búsqueda
    // Aquí simulamos buscando por número de venta
    List<Venta> ventas = ventaRepository.findAll();

    return ventas.stream()
            .filter(v -> coincideTermino(v, termino))
            .map(ventaMapper::toResponse)
            .collect(Collectors.toList());
  }

  private boolean coincideTermino(Venta venta, String termino) {
    if (termino == null || termino.isBlank()) {
      return false;
    }

    String terminoLower = termino.toLowerCase();

    // Buscar en número de venta
    if (venta.getNumeroVenta().toLowerCase().contains(terminoLower)) {
      return true;
    }

    // Buscar en cliente (nombre o apellido)
    if (venta.getCliente().getNombres().toLowerCase().contains(terminoLower) ||
            venta.getCliente().getApellidos().toLowerCase().contains(terminoLower)) {
      return true;
    }

    // Buscar en número de comprobante
    return venta.getNumeroComprobante() != null &&
            venta.getNumeroComprobante().toLowerCase().contains(terminoLower);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"ventas", "reportes"}, allEntries = true)
  public VentaResponse revertirVentaCompletada(Long id, String motivo) {
    log.info("Revirtiendo venta completada ID: {}", id);

    Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new VentaNotFoundException(id));

    // Solo permitir revertir ventas completadas
    if (venta.getEstado() != Venta.EstadoVenta.COMPLETADA) {
      throw new BusinessException("Solo se pueden revertir ventas en estado COMPLETADA");
    }

    // Actualizar estado y observaciones
    venta.setEstado(Venta.EstadoVenta.PENDIENTE);
    String observaciones = venta.getObservaciones();
    venta.setObservaciones((observaciones != null ? observaciones + " | " : "") +
            "REVERTIDA A PENDIENTE: " + motivo + " [" + LocalDateTime.now() + "]");

    // Guardar cambios
    venta = ventaRepository.save(venta);
    log.info("Venta revertida exitosamente a estado PENDIENTE");

    return ventaMapper.toResponse(venta);
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> obtenerEstadisticasPorModelo(LocalDate fechaInicio, LocalDate fechaFin) {
    log.debug("Obteniendo estadísticas por modelo entre {} y {}", fechaInicio, fechaFin);

    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(23, 59, 59);

    // Obtener ventas del período
    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndEstado(
            inicio, fin, Venta.EstadoVenta.COMPLETADA);

    // Agrupar por modelo (producto)
    Map<Long, ModeloStats> estadisticasPorModelo = new HashMap<>();

    for (Venta venta : ventas) {
      for (DetalleVenta detalle : venta.getDetalles()) {
        Long productoId = detalle.getProducto().getId();
        String productoNombre = detalle.getProducto().getNombre();
        String codigo = detalle.getProducto().getCodigo();

        ModeloStats stats = estadisticasPorModelo.getOrDefault(productoId,
                new ModeloStats(productoId, productoNombre, codigo));

        stats.cantidadVendida += detalle.getCantidad();
        stats.totalVendido = stats.totalVendido.add(detalle.getSubtotal());

        // Contar por talla
        String talla = detalle.getTalla().getNumero();
        stats.cantidadPorTalla.put(talla,
                stats.cantidadPorTalla.getOrDefault(talla, 0) + detalle.getCantidad());

        // Contar por color
        String color = detalle.getColor().getNombre();
        stats.cantidadPorColor.put(color,
                stats.cantidadPorColor.getOrDefault(color, 0) + detalle.getCantidad());

        estadisticasPorModelo.put(productoId, stats);
      }
    }

    // Preparar resultados
    List<Map<String, Object>> resultados = estadisticasPorModelo.values().stream()
            .sorted((a, b) -> b.cantidadVendida - a.cantidadVendida) // Ordenar por más vendidos
            .map(stats -> {
              Map<String, Object> modelo = new HashMap<>();
              modelo.put("id", stats.id);
              modelo.put("nombre", stats.nombre);
              modelo.put("codigo", stats.codigo);
              modelo.put("cantidadVendida", stats.cantidadVendida);
              modelo.put("totalVendido", stats.totalVendido);
              modelo.put("ventasPorTalla", stats.cantidadPorTalla);
              modelo.put("ventasPorColor", stats.cantidadPorColor);
              return modelo;
            })
            .collect(Collectors.toList());

    Map<String, Object> respuesta = new HashMap<>();
    respuesta.put("fechaInicio", fechaInicio);
    respuesta.put("fechaFin", fechaFin);
    respuesta.put("estadisticas", resultados);

    return respuesta;
  }

  @Override
  public Map<String, Object> obtenerResumenComprasPorCliente(Long clienteId) {
    log.debug("Obteniendo resumen de compras para cliente ID: {}", clienteId);

    // Verificar que el cliente existe
    Clientes cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new ClienteNotFoundException(clienteId));

    // Obtener todas las ventas completadas del cliente
    List<Venta> ventas = ventaRepository.findByClienteIdAndEstado(clienteId, Venta.EstadoVenta.COMPLETADA);

    // Calcular totales
    BigDecimal totalComprado = BigDecimal.ZERO;
    int cantidadCompras = ventas.size();
    int cantidadProductos = 0;
    LocalDateTime primeraCompra = null;
    LocalDateTime ultimaCompra = null;

    // Productos comprados
    Map<Long, Integer> productosCantidad = new HashMap<>();
    Map<Long, BigDecimal> productosMontos = new HashMap<>();

    for (Venta venta : ventas) {
      totalComprado = totalComprado.add(venta.getTotal());

      // Actualizar fechas de primera y última compra
      if (primeraCompra == null || venta.getFechaCreacion().isBefore(primeraCompra)) {
        primeraCompra = venta.getFechaCreacion();
      }

      if (ultimaCompra == null || venta.getFechaCreacion().isAfter(ultimaCompra)) {
        ultimaCompra = venta.getFechaCreacion();
      }

      // Contar productos
      for (DetalleVenta detalle : venta.getDetalles()) {
        cantidadProductos += detalle.getCantidad();

        // Agrupar por producto
        Long productoId = detalle.getProducto().getId();
        productosCantidad.put(productoId,
                productosCantidad.getOrDefault(productoId, 0) + detalle.getCantidad());

        // Sumar montos por producto
        productosMontos.put(productoId,
                productosMontos.getOrDefault(productoId, BigDecimal.ZERO).add(detalle.getSubtotal()));
      }
    }

    // Preparar resultado
    Map<String, Object> resumen = new HashMap<>();
    resumen.put("clienteId", cliente.getId());
    resumen.put("nombreCliente", cliente.getNombres() + " " + cliente.getApellidos());
    resumen.put("documento", cliente.getDni());
    resumen.put("totalComprado", totalComprado);
    resumen.put("cantidadCompras", cantidadCompras);
    resumen.put("cantidadProductos", cantidadProductos);
    resumen.put("primeraCompra", primeraCompra);
    resumen.put("ultimaCompra", ultimaCompra);

    // Calcular ticket promedio
    if (cantidadCompras > 0) {
      resumen.put("ticketPromedio", totalComprado.divide(new BigDecimal(cantidadCompras), 2, RoundingMode.HALF_EVEN));
    } else {
      resumen.put("ticketPromedio", BigDecimal.ZERO);
    }

    // Top 5 productos más comprados
    List<Map.Entry<Long, Integer>> topProductos = productosCantidad.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(5)
            .toList();

    List<Map<String, Object>> productosDetalle = new ArrayList<>();
    for (Map.Entry<Long, Integer> entry : topProductos) {
      Producto producto = productoRepository.findById(entry.getKey())
              .orElseThrow(() -> new ProductoNotFoundException(entry.getKey()));

      Map<String, Object> productoInfo = new HashMap<>();
      productoInfo.put("id", producto.getId());
      productoInfo.put("codigo", producto.getCodigo());
      productoInfo.put("nombre", producto.getNombre());
      productoInfo.put("cantidad", entry.getValue());
      productoInfo.put("monto", productosMontos.get(entry.getKey()));

      productosDetalle.add(productoInfo);
    }

    resumen.put("productosFavoritos", productosDetalle);

    return resumen;
  }

  @Override
  @Transactional
  public Map<String, Object> obtenerVentasDiarias(LocalDate fecha, Long usuarioId) {
    log.info("Generando reporte de ventas diarias para fecha {} y usuario {}", fecha, usuarioId);
    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(23, 59, 59);

    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndUsuarioIdAndEstado(
            inicio, fin, usuarioId, Venta.EstadoVenta.COMPLETADA);

    Map<String, Object> resultado = new HashMap<>();
    resultado.put("fecha", fecha);
    resultado.put("totalVentas", ventas.size());
    resultado.put("totalIngresos", ventas.stream()
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    resultado.put("totalIgv", ventas.stream()
            .map(Venta::getIgv)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    return resultado;
  }

  @Override
  @Transactional
  public Map<String, Object> obtenerVentasSemanales(LocalDate fechaReferencia, Long usuarioId) {
    log.info("Generando reporte de ventas semanales para fecha {} y usuario {}", fechaReferencia, usuarioId);
    int semana = fechaReferencia.get(WeekFields.ISO.weekOfWeekBasedYear());
    int año = fechaReferencia.getYear();
    LocalDateTime inicio = LocalDate.of(año, 1, 1)
            .with(WeekFields.ISO.weekOfWeekBasedYear(), semana)
            .atStartOfDay();
    LocalDateTime fin = inicio.plusDays(7)
            .withHour(23).withMinute(59).withSecond(59);

    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndUsuarioIdAndEstado(
            inicio, fin, usuarioId, Venta.EstadoVenta.COMPLETADA);

    Map<String, Object> resultado = new HashMap<>();
    resultado.put("semana", semana);
    resultado.put("año", año);
    resultado.put("totalVentas", ventas.size());
    resultado.put("totalIngresos", ventas.stream()
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    resultado.put("totalIgv", ventas.stream()
            .map(Venta::getIgv)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    return resultado;
  }

  @Override
  public Map<String, Object> obtenerVentasMensuales(int mes, int año, Long usuarioId) {
    log.info("Generando reporte de ventas mensuales para mes {} y año {} y usuario {}", mes, año, usuarioId);
    LocalDateTime inicio = LocalDateTime.of(año, mes, 1, 0, 0);
    LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

    List<Venta> ventas = ventaRepository.findByFechaCreacionBetweenAndUsuarioIdAndEstado(
            inicio, fin, usuarioId, Venta.EstadoVenta.COMPLETADA);

    Map<String, Object> resultado = new HashMap<>();
    resultado.put("mes", mes);
    resultado.put("año", año);
    resultado.put("totalVentas", ventas.size());
    resultado.put("totalIngresos", ventas.stream()
            .map(Venta::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    resultado.put("totalIgv", ventas.stream()
            .map(Venta::getIgv)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    return resultado;
  }

  @Override
  public Map<String, Object> obtenerTopVendedores(LocalDate fechaInicio, LocalDate fechaFin, int limit, Long usuarioId) {
    return Map.of();
  }

  @Override
  public Map<String, Object> obtenerTopProductos(LocalDate fechaInicio, LocalDate fechaFin, int limit, Long usuarioId) {
    return Map.of();
  }

  @Override
  public Map<String, Object> obtenerProductosSinMovimiento(int dias, int limit, Long usuarioId) {
    return Map.of();
  }

  @Override
  public List<Venta> findByFechaAndAlmacen(LocalDate fecha, Almacen almacen) {
    return List.of();
  }

  @Override
  public List<Venta> findByFecha(LocalDate fecha) {
    return List.of();
  }

  /**
   * Clase auxiliar para estadísticas de modelo
   */
  private static class ModeloStats {
    Long id;
    String nombre;
    String codigo;
    int cantidadVendida;
    BigDecimal totalVendido;
    Map<String, Integer> cantidadPorTalla;
    Map<String, Integer> cantidadPorColor;

    public ModeloStats(Long id, String nombre, String codigo) {
      this.id = id;
      this.nombre = nombre;
      this.codigo = codigo;
      this.cantidadVendida = 0;
      this.totalVendido = BigDecimal.ZERO;
      this.cantidadPorTalla = new HashMap<>();
      this.cantidadPorColor = new HashMap<>();
    }
  }
}