package com.emersondev.service.impl;

import com.emersondev.api.request.DetalleVentaRequest;
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

    // Crear venta
    Venta venta = new Venta();
    venta.setNumeroVenta(serieGenerator.generarNumeroVenta());
    venta.setCliente(cliente);
    venta.setUsuario(usuario);
    venta.setTipoComprobante(ventaRequest.getTipoComprobante());
    venta.setSerieComprobante(ventaRequest.getSerieComprobante());
    venta.setNumeroComprobante(ventaRequest.getNumeroComprobante());
    venta.setObservaciones(ventaRequest.getObservaciones());
    venta.setEstado(Venta.EstadoVenta.COMPLETADA);

    // Validar y agregar detalles de la venta
    if (ventaRequest.getDetalles() == null || ventaRequest.getDetalles().isEmpty()) {
      throw new BusinessException("La venta debe tener al menos un detalle");
    }

    BigDecimal subtotal = BigDecimal.ZERO;

    // Procesar cada detalle
    for (DetalleVentaRequest detalleRequest : ventaRequest.getDetalles()) {
      // Validar producto
      Producto producto = productoRepository.findById(detalleRequest.getProductoId())
              .orElseThrow(() -> new ProductoNotFoundException(detalleRequest.getProductoId()));

      // Validar color
      Color color = colorRepository.findById(detalleRequest.getColorId())
              .orElseThrow(() -> new ResourceNotFoundException("Color", "id", detalleRequest.getColorId().toString()));

      // Validar que el color pertenezca al producto
      if (!color.getProducto().getId().equals(producto.getId())) {
        throw new BusinessException("El color seleccionado no pertenece al producto");
      }

      // Validar talla
      Talla talla = tallaRepository.findById(detalleRequest.getTallaId())
              .orElseThrow(() -> new ResourceNotFoundException("Talla", "id", detalleRequest.getTallaId().toString()));

      // Validar que la talla pertenezca al color
      if (!talla.getColor().getId().equals(color.getId())) {
        throw new BusinessException("La talla seleccionada no pertenece al color");
      }

      // Validar stock suficiente
      Integer stockDisponible = inventarioService.obtenerStockPorVariante(
              producto.getId(), color.getId(), talla.getId());

      if (stockDisponible < detalleRequest.getCantidad()) {
        throw new StockInsuficienteException(
                "Stock insuficiente para el producto " + producto.getNombre() +
                        " - Color: " + color.getNombre() + " - Talla: " + talla.getNumero(),
                stockDisponible,
                detalleRequest.getCantidad());
      }

      // Crear detalle de venta
      DetalleVenta detalle = new DetalleVenta();
      detalle.setProducto(producto);
      detalle.setColor(color);
      detalle.setTalla(talla);
      detalle.setCantidad(detalleRequest.getCantidad());
      detalle.setPrecioUnitario(producto.getPrecioVenta());
      detalle.setProductDescription();
      detalle.calcularSubtotal();

      venta.addDetalle(detalle);
      subtotal = subtotal.add(detalle.getSubtotal());

      // Actualizar stock
      inventarioService.actualizarStockPorVenta(
              producto.getId(), color.getId(), talla.getId(), detalleRequest.getCantidad());
    }

    // Establecer los totales
    venta.setSubtotal(subtotal);
    venta.setIgv(subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_EVEN));
    venta.setTotal(subtotal.add(venta.getIgv()).setScale(2, RoundingMode.HALF_EVEN));

    // Guardar la venta
    venta = ventaRepository.save(venta);
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

    // Verificar que la venta no esté ya anulada
    if (Venta.EstadoVenta.ANULADA.equals(venta.getEstado())) {
      throw new BusinessException("La venta ya se encuentra anulada");
    }

    // Actualizar estado y observaciones
    venta.setEstado(Venta.EstadoVenta.ANULADA);
    String observaciones = venta.getObservaciones();
    venta.setObservaciones((observaciones != null ? observaciones + " | " : "") +
            "ANULADA: " + motivo + " [" + LocalDateTime.now() + "]");

    // Devolver stock al inventario
    for (DetalleVenta detalle : venta.getDetalles()) {
      Long productoId = detalle.getProducto().getId();
      Long colorId = detalle.getColor().getId();
      Long tallaId = detalle.getTalla().getId();

      // Devolver stock al inventario
      inventarioService.devolverStockPorAnulacion(
              productoId, colorId, tallaId, detalle.getCantidad());
    }

    // Guardar la venta actualizada
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

    // Solo permitir eliminar ventas en estado PENDIENTE
    if (!Venta.EstadoVenta.PENDIENTE.equals(venta.getEstado())) {
      throw new BusinessException("Solo se pueden eliminar ventas en estado PENDIENTE");
    }

    // Devolver stock al inventario antes de eliminar
    for (DetalleVenta detalle : venta.getDetalles()) {
      Long productoId = detalle.getProducto().getId();
      Long colorId = detalle.getColor().getId();
      Long tallaId = detalle.getTalla().getId();

      // Devolver stock al inventario
      inventarioService.devolverStockPorAnulacion(
              productoId, colorId, tallaId, detalle.getCantidad());
    }

    // Eliminar la venta y sus detalles (cascade)
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
            .collect(Collectors.toList());

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
            .collect(Collectors.toList());

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
            .collect(Collectors.toList());

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
            .collect(Collectors.toList());

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