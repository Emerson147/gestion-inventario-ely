package com.emersondev.service.impl;

import com.emersondev.api.request.PagoRequest;
import com.emersondev.api.response.PagoResponse;
import com.emersondev.api.response.ReportePagosResponse;
import com.emersondev.domain.entity.Pago;
import com.emersondev.domain.entity.Usuario;
import com.emersondev.domain.entity.Venta;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.exception.VentaNotFoundException;
import com.emersondev.domain.repository.PagoRepository;
import com.emersondev.domain.repository.UsuarioRepository;
import com.emersondev.domain.repository.VentaRepository;
import com.emersondev.mapper.PagoMapper;
import com.emersondev.service.interfaces.PagoService;
import com.emersondev.service.interfaces.VentaService;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

  private final PagoRepository pagoRepository;
  private final VentaRepository ventaRepository;
  private final UsuarioRepository usuarioRepository;
  private final VentaService ventaService;
  private final PagoMapper pagoMapper;
  private final SerieGenerator serieGenerator;

  @Override
  @Transactional
  @CacheEvict(value = {"pagos", "ventas"}, allEntries = true)
  public PagoResponse registrarPago(PagoRequest pagoRequest) {
    log.info("Registrando nuevo pago para venta ID: {}", pagoRequest.getVentaId());

    // Validar venta
    Venta venta = ventaRepository.findById(pagoRequest.getVentaId())
            .orElseThrow(() -> new VentaNotFoundException(pagoRequest.getVentaId()));

    // Validar que la venta no esté anulada
    if (venta.getEstado() == Venta.EstadoVenta.ANULADA) {
      throw new BusinessException("No se puede registrar pagos para una venta anulada");
    }

    // Validar usuario
    Usuario usuario = usuarioRepository.findById(pagoRequest.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", pagoRequest.getUsuarioId().toString()));

    // Validar monto de pago vs saldo pendiente
    BigDecimal saldoPendiente = calcularSaldoPendiente(venta.getId());

    if (pagoRequest.getMonto().compareTo(saldoPendiente) > 0) {
      throw new BusinessException("El monto del pago (" + pagoRequest.getMonto() +
              ") excede el saldo pendiente de la venta (" + saldoPendiente + ")");
    }

    // Crear pago
    Pago pago = new Pago();
    pago.setNumeroPago(serieGenerator.generarNumeroPago(venta.getId()));
    pago.setVenta(venta);
    pago.setUsuario(usuario);
    pago.setMonto(pagoRequest.getMonto());
    pago.setMetodoPago(pagoRequest.getMetodoPago());
    pago.setEstado(Pago.EstadoPago.CONFIRMADO);

    // Campos opcionales para diferentes métodos de pago
    pago.setNumeroReferencia(pagoRequest.getNumeroReferencia());
    pago.setNombreTarjeta(pagoRequest.getNombreTarjeta());
    pago.setUltimos4Digitos(pagoRequest.getUltimos4Digitos());
    pago.setObservaciones(pagoRequest.getObservaciones());

    // Guardar pago
    pago = pagoRepository.save(pago);
    log.info("Pago registrado exitosamente con número: {}", pago.getNumeroPago());

    // Verificar si la venta está completamente pagada
    if (verificarVentaPagada(venta.getId()) && venta.getEstado() == Venta.EstadoVenta.PENDIENTE) {
      // Actualizar estado de la venta
      ventaService.actualizarEstadoVenta(venta.getId(), Venta.EstadoVenta.COMPLETADA);
      log.info("Venta ID: {} completada después de registrar el pago", venta.getId());
    }

    return pagoMapper.toResponse(pago);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'id-' + #id")
  public PagoResponse obtenerPagoPorId(Long id) {
    log.debug("Obteniendo pago con ID: {}", id);

    Pago pago = pagoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pago", "id", id.toString()));

    return pagoMapper.toResponse(pago);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'numero-' + #numeroPago")
  public PagoResponse obtenerPagoPorNumero(String numeroPago) {
    log.debug("Obteniendo pago con número: {}", numeroPago);

    Pago pago = pagoRepository.findByNumeroPago(numeroPago)
            .orElseThrow(() -> new ResourceNotFoundException("Pago", "número", numeroPago));

    return pagoMapper.toResponse(pago);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'venta-' + #ventaId")
  public List<PagoResponse> obtenerPagosPorVenta(Long ventaId) {
    log.debug("Obteniendo pagos para venta ID: {}", ventaId);

    // Verificar que la venta existe
    if (!ventaRepository.existsById(ventaId)) {
      throw new VentaNotFoundException(ventaId);
    }

    List<Pago> pagos = pagoRepository.findByVentaId(ventaId);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal obtenerTotalPagadoPorVenta(Long ventaId) {
    log.debug("Calculando total pagado para venta ID: {}", ventaId);

    // Verificar que la venta existe
    if (!ventaRepository.existsById(ventaId)) {
      throw new VentaNotFoundException(ventaId);
    }

    BigDecimal totalPagado = pagoRepository.calcularTotalPagadoPorVenta(ventaId);

    return totalPagado != null ? totalPagado : BigDecimal.ZERO;
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal calcularSaldoPendiente(Long ventaId) {
    log.debug("Calculando saldo pendiente para venta ID: {}", ventaId);

    Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new VentaNotFoundException(ventaId));

    BigDecimal totalPagado = obtenerTotalPagadoPorVenta(ventaId);
    BigDecimal totalVenta = venta.getTotal();

    return totalVenta.subtract(totalPagado);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'metodo-' + #metodoPago")
  public List<PagoResponse> obtenerPagosPorMetodo(Pago.MetodoPago metodoPago) {
    log.debug("Obteniendo pagos por método: {}", metodoPago);

    List<Pago> pagos = pagoRepository.findByMetodoPago(metodoPago);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'estado-' + #estado")
  public List<PagoResponse> obtenerPagosPorEstado(Pago.EstadoPago estado) {
    log.debug("Obteniendo pagos por estado: {}", estado);

    List<Pago> pagos = pagoRepository.findByEstado(estado);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  public List<PagoResponse> obtenerPagosEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    return List.of();
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'fecha-' + #fecha")
  public List<PagoResponse> obtenerPagosPorFecha(LocalDate fecha) {
    log.debug("Obteniendo pagos para fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(LocalTime.MAX);

    List<Pago> pagos = pagoRepository.findByFechaCreacionBetween(inicio, fin);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'fechas-' + #inicio + '-' + #fin")
  public List<PagoResponse> obtenerPagosEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
    log.debug("Obteniendo pagos entre: {} y {}", inicio, fin);

    if (inicio == null || fin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (inicio.isAfter(fin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    List<Pago> pagos = pagoRepository.findByFechaCreacionBetween(inicio, fin);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "pagos", key = "'usuario-' + #usuarioId")
  public List<PagoResponse> obtenerPagosPorUsuario(Long usuarioId) {
    log.debug("Obteniendo pagos realizados por usuario ID: {}", usuarioId);

    // Verificar que el usuario existe
    if (!usuarioRepository.existsById(usuarioId)) {
      throw new ResourceNotFoundException("Usuario", "id", usuarioId.toString());
    }

    List<Pago> pagos = pagoRepository.findByUsuarioId(usuarioId);

    return pagos.stream()
            .map(pagoMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = {"pagos", "ventas"}, allEntries = true)
  public PagoResponse actualizarEstadoPago(Long id, Pago.EstadoPago nuevoEstado, String observacion) {
    log.info("Actualizando estado de pago ID: {} a: {}", id, nuevoEstado);

    Pago pago = pagoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pago", "id", id.toString()));

    // Verificar reglas de negocio para cambios de estado
    if (pago.getEstado() == Pago.EstadoPago.REEMBOLSADO) {
      throw new BusinessException("No se puede cambiar el estado de un pago reembolsado");
    }

    // Guardar estado anterior para verificar cambios relevantes
    Pago.EstadoPago estadoAnterior = pago.getEstado();

    // Actualizar estado
    pago.setEstado(nuevoEstado);

    // Añadir observaciones si se proporcionan
    if (observacion != null && !observacion.isBlank()) {
      String observaciones = pago.getObservaciones();
      pago.setObservaciones((observaciones != null ? observaciones + " | " : "") +
              "CAMBIO DE ESTADO: " + estadoAnterior + " -> " + nuevoEstado +
              ": " + observacion + " [" + LocalDateTime.now() + "]");
    }

    // Guardar cambios
    pago = pagoRepository.save(pago);
    log.info("Estado de pago actualizado exitosamente");

    // Si el estado cambió de manera que afecta al total pagado de la venta
    // (ej: de CONFIRMADO a RECHAZADO o viceversa)
    if ((estadoAnterior == Pago.EstadoPago.CONFIRMADO && nuevoEstado != Pago.EstadoPago.CONFIRMADO) ||
            (estadoAnterior != Pago.EstadoPago.CONFIRMADO && nuevoEstado == Pago.EstadoPago.CONFIRMADO)) {

      Long ventaId = pago.getVenta().getId();

      // Verificar si la venta ahora está pagada o dejó de estarlo
      boolean ventaPagada = verificarVentaPagada(ventaId);

      // Actualizar estado de la venta según corresponda
      if (ventaPagada && pago.getVenta().getEstado() == Venta.EstadoVenta.PENDIENTE) {
        ventaService.actualizarEstadoVenta(ventaId, Venta.EstadoVenta.COMPLETADA);
        log.info("Venta ID: {} completada tras confirmación de pago", ventaId);
      } else if (!ventaPagada && pago.getVenta().getEstado() == Venta.EstadoVenta.COMPLETADA) {
        ventaService.actualizarEstadoVenta(ventaId, Venta.EstadoVenta.PENDIENTE);
        log.info("Venta ID: {} revertida a pendiente tras rechazo de pago", ventaId);
      }
    }

    return pagoMapper.toResponse(pago);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"pagos", "ventas"}, allEntries = true)
  public PagoResponse reembolsarPago(Long id, String motivo) {
    log.info("Reembolsando pago ID: {}", id);

    // Reutilizar método de actualización de estado
    return actualizarEstadoPago(id, Pago.EstadoPago.REEMBOLSADO,
            motivo != null ? "REEMBOLSO: " + motivo : "REEMBOLSO");
  }

  @Override
  public Map<String, Object> obtenerTotalesPorMetodoPago(LocalDate fechaInicio, LocalDate fechaFin) {
    return Map.of();
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "reportes-pagos", key = "'reporte-' + #inicio + '-' + #fin")
  public ReportePagosResponse generarReportePagos(LocalDateTime inicio, LocalDateTime fin) {
    log.info("Generando reporte de pagos desde {} hasta {}", inicio, fin);

    if (inicio == null || fin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (inicio.isAfter(fin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    // Obtener estadísticas por método de pago
    List<Map<String, Object>> estadisticasPorMetodo = pagoRepository.obtenerEstadisticasPorMetodoPago(inicio, fin);

    // Obtener estadísticas por fecha
    List<Map<String, Object>> estadisticasPorFecha = pagoRepository.obtenerEstadisticasPorFecha(inicio, fin);

    // Obtener todos los pagos en el período para cálculos adicionales
    List<Pago> pagos = pagoRepository.findByFechaCreacionBetween(inicio, fin);

    // Calcular totales generales
    BigDecimal montoTotal = BigDecimal.ZERO;
    int cantidadPagos = 0;

    for (Pago pago : pagos) {
      if (pago.getEstado() == Pago.EstadoPago.CONFIRMADO) {
        montoTotal = montoTotal.add(pago.getMonto());
        cantidadPagos++;
      }
    }

    // Crear objeto de respuesta
    ReportePagosResponse reporte = new ReportePagosResponse();
    reporte.setFechaInicio(inicio);
    reporte.setFechaFin(fin);
    reporte.setMontoTotal(montoTotal);
    reporte.setCantidadPagos(cantidadPagos);
    reporte.setEstadisticasPorMetodo(estadisticasPorMetodo);
    reporte.setEstadisticasPorFecha(estadisticasPorFecha);

    // Calcular distribución porcentual por método de pago
    List<Map<String, Object>> distribucionPorcentual = new ArrayList<>();

    if (montoTotal.compareTo(BigDecimal.ZERO) > 0) {
      for (Map<String, Object> metodo : estadisticasPorMetodo) {
        Map<String, Object> item = new HashMap<>(metodo);
        BigDecimal monto = (BigDecimal) metodo.get("total");
        BigDecimal porcentaje = monto.multiply(new BigDecimal("100")).divide(montoTotal, 2, RoundingMode.HALF_UP);
        item.put("porcentaje", porcentaje);
        distribucionPorcentual.add(item);
      }
    }

    reporte.setDistribucionPorcentual(distribucionPorcentual);

    log.info("Reporte de pagos generado exitosamente");
    return reporte;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> obtenerResumenPagosDiario(LocalDate fecha) {
    log.debug("Obteniendo resumen de pagos para la fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(LocalTime.MAX);

    // Obtener pagos del día
    List<Pago> pagos = pagoRepository.findByFechaCreacionBetween(inicio, fin);

    // Agrupar por método de pago
    Map<Pago.MetodoPago, BigDecimal> totalPorMetodo = new HashMap<>();
    Map<Pago.MetodoPago, Integer> cantidadPorMetodo = new HashMap<>();
    BigDecimal montoTotal = BigDecimal.ZERO;
    int cantidadPagos = 0;

    for (Pago pago : pagos) {
      if (pago.getEstado() == Pago.EstadoPago.CONFIRMADO) {
        Pago.MetodoPago metodo = pago.getMetodoPago();

        // Actualizar monto total por método
        totalPorMetodo.put(metodo, totalPorMetodo.getOrDefault(metodo, BigDecimal.ZERO).add(pago.getMonto()));

        // Actualizar cantidad por método
        cantidadPorMetodo.put(metodo, cantidadPorMetodo.getOrDefault(metodo, 0) + 1);

        // Actualizar totales generales
        montoTotal = montoTotal.add(pago.getMonto());
        cantidadPagos++;
      }
    }

    // Convertir a formato para respuesta
    List<Map<String, Object>> detalleMetodos = new ArrayList<>();

    for (Pago.MetodoPago metodo : totalPorMetodo.keySet()) {
      Map<String, Object> detalleMetodo = new HashMap<>();
      detalleMetodo.put("metodo", metodo.name());
      detalleMetodo.put("total", totalPorMetodo.get(metodo));
      detalleMetodo.put("cantidad", cantidadPorMetodo.get(metodo));
      detalleMetodos.add(detalleMetodo);
    }

    // Preparar resultado
    Map<String, Object> resumen = new HashMap<>();
    resumen.put("fecha", fecha);
    resumen.put("montoTotal", montoTotal);
    resumen.put("cantidadPagos", cantidadPagos);
    resumen.put("detallePorMetodo", detalleMetodos);

    return resumen;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean verificarVentaPagada(Long ventaId) {
    log.debug("Verificando si la venta ID: {} está completamente pagada", ventaId);

    Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new VentaNotFoundException(ventaId));

    BigDecimal totalPagado = obtenerTotalPagadoPorVenta(ventaId);
    BigDecimal totalVenta = venta.getTotal();

    // Comparación con margen pequeño para evitar problemas de precisión decimal
    return totalPagado.compareTo(totalVenta) >= 0;
  }

}
