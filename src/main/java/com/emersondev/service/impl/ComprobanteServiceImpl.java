package com.emersondev.service.impl;

import com.emersondev.api.request.ComprobanteRequest;
import com.emersondev.api.response.ComprobanteResponse;
import com.emersondev.api.response.ReporteComprobantesResponse;
import com.emersondev.domain.entity.*;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ClienteNotFoundException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.exception.VentaNotFoundException;
import com.emersondev.domain.repository.*;
import com.emersondev.mapper.ComprobanteMapper;
import com.emersondev.service.interfaces.ComprobanteService;
import com.emersondev.service.interfaces.DocumentoGeneratorService;
import com.emersondev.util.SerieGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComprobanteServiceImpl implements ComprobanteService {
  private final ComprobanteRepository comprobanteRepository;
  private final DetalleComprobanteRepository detalleComprobanteRepository;
  private final VentaRepository ventaRepository;
  private final ClienteRepository clienteRepository;
  private final UsuarioRepository usuarioRepository;
  private final ComprobanteMapper comprobanteMapper;
  private final DocumentoGeneratorService documentoGeneratorService;
  private final SerieGenerator serieGenerator;

  @Override
  @Transactional
  @CacheEvict(value = {"comprobantes", "ventas"}, allEntries = true)
  public ComprobanteResponse generarComprobante(ComprobanteRequest comprobanteRequest) {
    log.info("Generando nuevo comprobante para venta ID: {}", comprobanteRequest.getVentaId());

    // Validar que la venta exista
    Venta venta = ventaRepository.findById(comprobanteRequest.getVentaId())
            .orElseThrow(() -> new VentaNotFoundException(comprobanteRequest.getVentaId()));

    // Validar que la venta no esté anulada
    if (venta.getEstado() == Venta.EstadoVenta.ANULADA) {
      throw new BusinessException("No se puede generar comprobante para una venta anulada");
    }

    // Validar que no exista ya un comprobante para esta venta
    if (comprobanteRepository.findByVentaId(venta.getId()).isPresent()) {
      throw new BusinessException("Ya existe un comprobante para esta venta");
    }

    // Obtener cliente y usuario
    Clientes cliente = venta.getCliente();
    Usuario usuario = venta.getUsuario();

    // Validar tipo de documento según cliente
    validarTipoDocumento(comprobanteRequest.getTipoDocumento(), cliente);

    // === INICIO: LOGICA ROBUSTA DE SERIE Y NUMERO ===
    Comprobante.TipoDocumento tipo = comprobanteRequest.getTipoDocumento();

    // 1. Determinar la serie a usasr
    String serie = comprobanteRequest.getSerie();
    if (serie == null || serie.isEmpty()) {
      // Asignar serie predeterminada según tipo de documento
      serie = obtenerSerieDefaultPorTipo(tipo);
    }

    // 2. (Opcional) Cambiar de serie automáticamente si se llenó la actual
    // Si quieres cambiar de serie automáticamente al alcanzar el máximo, descomenta y usa esto:
       serie = serieGenerator.obtenerSiguienteSerie(tipo, serie);

    // 3. Obtener el correlativo seguro para esa serie
    String numero = obtenerSiguienteNumeroComprobante(tipo, serie);
    // === FIN: LOGICA ROBUSTA DE SERIE Y NUMERO ===

    // Crear comprobante
    Comprobante comprobante = new Comprobante();
    comprobante.setTipoDocumento(comprobanteRequest.getTipoDocumento());
    comprobante.setSerie(serie);
    comprobante.setNumero(numero);
    comprobante.setFechaEmision(LocalDateTime.now());
    comprobante.setVenta(venta);
    comprobante.setCliente(cliente);
    comprobante.setUsuario(usuario);
    comprobante.setSubtotal(venta.getSubtotal());
    comprobante.setIgv(venta.getIgv());
    comprobante.setTotal(venta.getTotal());
    comprobante.setEstado(Comprobante.EstadoComprobante.EMITIDO);
    comprobante.setObservaciones(comprobanteRequest.getObservaciones());

    // Crear detalles del comprobante a partir de los detalles de la venta
    for (DetalleVenta detalleVenta : venta.getDetalles()) {
      DetalleComprobante detalle = new DetalleComprobante();
      detalle.setProducto(detalleVenta.getProducto());
      detalle.setColor(detalleVenta.getColor());
      detalle.setTalla(detalleVenta.getTalla());
      detalle.setCantidad(detalleVenta.getCantidad());
      detalle.setPrecioUnitario(detalleVenta.getPrecioUnitario());
      // Calcular montos
      detalle.calcularMontos();
      // Generar descripción
      detalle.generarDescripcion();

      comprobante.addDetalle(detalle);
    }

    // Generar código hash para el comprobante
    comprobante.generarCodigoHash();

    // Guardar comprobante
    comprobante = comprobanteRepository.save(comprobante);
    log.info("Comprobante generado exitosamente: {}-{}", serie, numero);

    // Actualizar venta con referencia al comprobante
    venta.setTipoComprobante(mapTipoComprobanteVenta(comprobante.getTipoDocumento()));
    venta.setSerieComprobante(comprobante.getSerie());
    venta.setNumeroComprobante(comprobante.getNumero());
    ventaRepository.save(venta);

    // Generar PDF y XML
    try {
      byte[] pdfBytes = documentoGeneratorService.generarPdfComprobante(comprobante.getId());
      byte[] xmlBytes = documentoGeneratorService.generarXmlComprobante(comprobante.getId());

      // En un sistema real, guardaríamos estos archivos en disco o en la nube
      // y actualizaríamos las rutas en la entidad
      String rutaPdf = "comprobantes/" + comprobante.getTipoDocumento() + "_" +
              comprobante.getSerie() + "_" + comprobante.getNumero() + ".pdf";
      String rutaXml = "comprobantes/" + comprobante.getTipoDocumento() + "_" +
              comprobante.getSerie() + "_" + comprobante.getNumero() + ".xml";

      comprobante.setRutaArchivoPdf(rutaPdf);
      comprobante.setRutaArchivoXml(rutaXml);
      comprobanteRepository.save(comprobante);
    } catch (Exception e) {
      log.error("Error al generar documentos del comprobante: {}", e.getMessage());
      // No revertimos la transacción, ya que el comprobante se creó correctamente
      // Solo registramos el error
    }

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'id-' + #id")
  public ComprobanteResponse obtenerComprobante(Long id) {
    log.debug("Obteniendo comprobante con ID: {}", id);

    Comprobante comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", id.toString()));

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'serie-numero-' + #serie + '-' + #numero")
  public ComprobanteResponse obtenerComprobantePorSerieYNumero(String serie, String numero) {
    log.debug("Obteniendo comprobante con serie-número: {}-{}", serie, numero);

    Comprobante comprobante = comprobanteRepository.findBySerieAndNumero(serie, numero)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "serie-número", serie + "-" + numero));

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'tipo-serie-numero-' + #tipo + '-' + #serie + '-' + #numero")
  public ComprobanteResponse obtenerComprobantePorTipoSerieYNumero(
          Comprobante.TipoDocumento tipo, String serie, String numero) {
    log.debug("Obteniendo comprobante con tipo-serie-número: {}-{}-{}", tipo, serie, numero);

    Comprobante comprobante = comprobanteRepository.findByTipoDocumentoAndSerieAndNumero(tipo, serie, numero)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante",
                    "tipo-serie-número", tipo + "-" + serie + "-" + numero));

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'venta-' + #ventaId")
  public ComprobanteResponse obtenerComprobantePorVenta(Long ventaId) {
    log.debug("Obteniendo comprobante para venta ID: {}", ventaId);

    // Verificar que la venta existe
    if (!ventaRepository.existsById(ventaId)) {
      throw new VentaNotFoundException(ventaId);
    }

    Comprobante comprobante = comprobanteRepository.findByVentaId(ventaId)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "ventaId", ventaId.toString()));

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes")
  public List<ComprobanteResponse> listarComprobantes() {
    log.debug("Listando todos los comprobantes");

    List<Comprobante> comprobantes = comprobanteRepository.findAll();

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'tipo-' + #tipoDocumento")
  public List<ComprobanteResponse> listarComprobantesPorTipo(Comprobante.TipoDocumento tipoDocumento) {
    log.debug("Listando comprobantes por tipo: {}", tipoDocumento);

    List<Comprobante> comprobantes = comprobanteRepository.findByTipoDocumento(tipoDocumento);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'estado-' + #estado")
  public List<ComprobanteResponse> listarComprobantesPorEstado(Comprobante.EstadoComprobante estado) {
    log.debug("Listando comprobantes por estado: {}", estado);

    List<Comprobante> comprobantes = comprobanteRepository.findByEstado(estado);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'cliente-' + #clienteId")
  public List<ComprobanteResponse> listarComprobantesPorCliente(Long clienteId) {
    log.debug("Listando comprobantes para cliente ID: {}", clienteId);

    // Verificar que el cliente existe
    if (!clienteRepository.existsById(clienteId)) {
      throw new ClienteNotFoundException(clienteId);
    }

    List<Comprobante> comprobantes = comprobanteRepository.findByClienteId(clienteId);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'fecha-' + #fecha")
  public List<ComprobanteResponse> listarComprobantesPorFecha(LocalDate fecha) {
    log.debug("Listando comprobantes para fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(LocalTime.MAX);

    List<Comprobante> comprobantes = comprobanteRepository.findByFechaEmisionBetween(inicio, fin);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'fechas-' + #fechaInicio + '-' + #fechaFin")
  public List<ComprobanteResponse> listarComprobantesEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    log.debug("Listando comprobantes entre fechas: {} y {}", fechaInicio, fechaFin);

    if (fechaInicio == null || fechaFin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (fechaInicio.isAfter(fechaFin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

    List<Comprobante> comprobantes = comprobanteRepository.findByFechaEmisionBetween(inicio, fin);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "comprobantes", key = "'tipo-fechas-' + #tipoDocumento + '-' + #fechaInicio + '-' + #fechaFin")
  public List<ComprobanteResponse> listarComprobantesPorTipoEntreFechas(
          Comprobante.TipoDocumento tipoDocumento, LocalDate fechaInicio, LocalDate fechaFin) {
    log.debug("Listando comprobantes de tipo {} entre fechas: {} y {}", tipoDocumento, fechaInicio, fechaFin);

    if (fechaInicio == null || fechaFin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (fechaInicio.isAfter(fechaFin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

    List<Comprobante> comprobantes = comprobanteRepository.findByTipoDocumentoAndFechaEmisionBetween(
            tipoDocumento, inicio, fin);

    return comprobantes.stream()
            .map(comprobanteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = {"comprobantes", "ventas"}, allEntries = true)
  public ComprobanteResponse anularComprobante(Long id, String motivo) {
    log.info("Anulando comprobante ID: {}", id);

    Comprobante comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", id.toString()));

    // Verificar que no esté ya anulado
    if (comprobante.getEstado() == Comprobante.EstadoComprobante.ANULADO) {
      throw new BusinessException("El comprobante ya se encuentra anulado");
    }

    // Actualizar estado
    comprobante.setEstado(Comprobante.EstadoComprobante.ANULADO);
    comprobante.setFechaAnulacion(LocalDateTime.now());
    comprobante.setMotivoAnulacion(motivo);

    // Guardar cambios
    comprobante = comprobanteRepository.save(comprobante);
    log.info("Comprobante anulado exitosamente");

    // Si la venta no está anulada, restauramos su estado de comprobante
    Venta venta = comprobante.getVenta();
    if (venta.getEstado() != Venta.EstadoVenta.ANULADA) {
      venta.setSerieComprobante(null);
      venta.setNumeroComprobante(null);
      venta.setTipoComprobante(null);
      ventaRepository.save(venta);
    }

    return comprobanteMapper.toResponse(comprobante);
  }

  @Override
  public String obtenerSiguienteNumeroComprobante(Comprobante.TipoDocumento tipo, String serie) {
    log.debug("Obteniendo siguiente número para comprobante de tipo {} y serie {}", tipo, serie);

    String ultimoNumero = comprobanteRepository.findMaxNumeroByTipoAndSerie(tipo, serie);

    int siguiente;
    if (ultimoNumero == null) {
      siguiente = 1;
    } else {
      try {
        siguiente = Integer.parseInt(ultimoNumero) + 1;
      } catch (NumberFormatException e) {
        log.error("Error al convertir el último número: {}", ultimoNumero, e);
        siguiente = 1;
      }
    }

    // Formatear con ceros a la izquierda (8 dígitos)
    return String.format("%08d", siguiente);
  }

  @Override
  public byte[] generarPdfComprobante(Long id) {
    log.debug("Generando PDF para comprobante ID: {}", id);

    // Verificar que el comprobante existe
    Comprobante comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", id.toString()));

    // Generar PDF
    return documentoGeneratorService.generarPdfComprobante(id);
  }

  @Override
  public byte[] generarXmlComprobante(Long id) {
    log.debug("Generando XML para comprobante ID: {}", id);

    // Verificar que el comprobante existe
    Comprobante comprobante = comprobanteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", id.toString()));

    // Generar XML
    return documentoGeneratorService.generarXmlComprobante(id);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = "reportes-comprobantes", key = "'reporte-' + #fechaInicio + '-' + #fechaFin")
  public ReporteComprobantesResponse generarReporteComprobantes(LocalDate fechaInicio, LocalDate fechaFin) {
    log.info("Generando reporte de comprobantes desde {} hasta {}", fechaInicio, fechaFin);

    if (fechaInicio == null || fechaFin == null) {
      throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
    }

    if (fechaInicio.isAfter(fechaFin)) {
      throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
    }

    LocalDateTime inicio = fechaInicio.atStartOfDay();
    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

    // Obtener comprobantes en el rango de fechas
    List<Comprobante> comprobantes = comprobanteRepository.findByFechaEmisionBetween(inicio, fin);

    // Preparar respuesta
    ReporteComprobantesResponse reporte = new ReporteComprobantesResponse();
    reporte.setFechaInicio(fechaInicio);
    reporte.setFechaFin(fechaFin);

    // Calcular totales
    BigDecimal totalFacturado = BigDecimal.ZERO;
    BigDecimal totalIGV = BigDecimal.ZERO;
    int cantidadComprobantes = 0;

    Map<Comprobante.TipoDocumento, Integer> cantidadPorTipo = new HashMap<>();
    Map<Comprobante.TipoDocumento, BigDecimal> montoPorTipo = new HashMap<>();

    for (Comprobante comprobante : comprobantes) {
      if (comprobante.getEstado() != Comprobante.EstadoComprobante.ANULADO) {
        totalFacturado = totalFacturado.add(comprobante.getTotal());
        totalIGV = totalIGV.add(comprobante.getIgv());
        cantidadComprobantes++;

        // Contabilizar por tipo
        Comprobante.TipoDocumento tipo = comprobante.getTipoDocumento();
        cantidadPorTipo.put(tipo, cantidadPorTipo.getOrDefault(tipo, 0) + 1);
        montoPorTipo.put(tipo, montoPorTipo.getOrDefault(tipo, BigDecimal.ZERO).add(comprobante.getTotal()));
      }
    }

    reporte.setTotalFacturado(totalFacturado);
    reporte.setTotalIGV(totalIGV);
    reporte.setCantidadComprobantes(cantidadComprobantes);

    // Preparar resumen por tipo
    List<Map<String, Object>> resumenPorTipo = new ArrayList<>();
    for (Map.Entry<Comprobante.TipoDocumento, Integer> entry : cantidadPorTipo.entrySet()) {
      Map<String, Object> item = new HashMap<>();
      item.put("tipo", entry.getKey().name());
      item.put("cantidad", entry.getValue());
      item.put("monto", montoPorTipo.get(entry.getKey()));
      resumenPorTipo.add(item);
    }

    reporte.setResumenPorTipo(resumenPorTipo);

    // Top 10 clientes
    Map<Long, ClienteFacturacion> facturacionPorCliente = new HashMap<>();

    for (Comprobante comprobante : comprobantes) {
      if (comprobante.getEstado() != Comprobante.EstadoComprobante.ANULADO) {
        Long clienteId = comprobante.getCliente().getId();
        String nombreCliente = comprobante.getCliente().getNombres() + " " +
                comprobante.getCliente().getApellidos();

        ClienteFacturacion cf = facturacionPorCliente.getOrDefault(clienteId,
                new ClienteFacturacion(clienteId, nombreCliente));

        cf.montoTotal = cf.montoTotal.add(comprobante.getTotal());
        cf.cantidadComprobantes++;

        facturacionPorCliente.put(clienteId, cf);
      }
    }

    // Obtener top 10
    List<Map<String, Object>> topClientes = facturacionPorCliente.values().stream()
            .sorted((a, b) -> b.montoTotal.compareTo(a.montoTotal))
            .limit(10)
            .map(cf -> {
              Map<String, Object> item = new HashMap<>();
              item.put("clienteId", cf.clienteId);
              item.put("nombreCliente", cf.nombreCliente);
              item.put("montoTotal", cf.montoTotal);
              item.put("cantidadComprobantes", cf.cantidadComprobantes);
              return item;
            })
            .collect(Collectors.toList());

    reporte.setTopClientes(topClientes);

    return reporte;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> obtenerResumenDiario(LocalDate fecha) {
    log.debug("Obteniendo resumen diario de comprobantes para fecha: {}", fecha);

    LocalDateTime inicio = fecha.atStartOfDay();
    LocalDateTime fin = fecha.atTime(LocalTime.MAX);

    // Obtener comprobantes de la fecha
    List<Comprobante> comprobantes = comprobanteRepository.findByFechaEmisionBetween(inicio, fin);

    // Preparar respuesta
    Map<String, Object> resumen = new HashMap<>();
    resumen.put("fecha", fecha);

    // Calcular totales
    BigDecimal totalFacturado = BigDecimal.ZERO;
    BigDecimal totalIGV = BigDecimal.ZERO;
    int cantidadComprobantes = 0;

    Map<Comprobante.TipoDocumento, Integer> cantidadPorTipo = new HashMap<>();
    Map<Comprobante.TipoDocumento, BigDecimal> montoPorTipo = new HashMap<>();
    Map<Comprobante.EstadoComprobante, Integer> cantidadPorEstado = new HashMap<>();

    for (Comprobante comprobante : comprobantes) {
      if (comprobante.getEstado() != Comprobante.EstadoComprobante.ANULADO) {
        totalFacturado = totalFacturado.add(comprobante.getTotal());
        totalIGV = totalIGV.add(comprobante.getIgv());
        cantidadComprobantes++;

        // Contabilizar por tipo
        Comprobante.TipoDocumento tipo = comprobante.getTipoDocumento();
        cantidadPorTipo.put(tipo, cantidadPorTipo.getOrDefault(tipo, 0) + 1);
        montoPorTipo.put(tipo, montoPorTipo.getOrDefault(tipo, BigDecimal.ZERO).add(comprobante.getTotal()));
      }

      // Contabilizar por estado
      Comprobante.EstadoComprobante estado = comprobante.getEstado();
      cantidadPorEstado.put(estado, cantidadPorEstado.getOrDefault(estado, 0) + 1);
    }

    resumen.put("totalFacturado", totalFacturado);
    resumen.put("totalIGV", totalIGV);
    resumen.put("cantidadComprobantes", cantidadComprobantes);

    // Preparar resumen por tipo
    List<Map<String, Object>> resumenPorTipo = new ArrayList<>();
    for (Map.Entry<Comprobante.TipoDocumento, Integer> entry : cantidadPorTipo.entrySet()) {
      Map<String, Object> item = new HashMap<>();
      item.put("tipo", entry.getKey().name());
      item.put("cantidad", entry.getValue());
      item.put("monto", montoPorTipo.get(entry.getKey()));
      resumenPorTipo.add(item);
    }

    resumen.put("resumenPorTipo", resumenPorTipo);

    // Preparar resumen por estado
    List<Map<String, Object>> resumenPorEstado = new ArrayList<>();
    for (Map.Entry<Comprobante.EstadoComprobante, Integer> entry : cantidadPorEstado.entrySet()) {
      Map<String, Object> item = new HashMap<>();
      item.put("estado", entry.getKey().name());
      item.put("cantidad", entry.getValue());
      resumenPorEstado.add(item);
    }

    resumen.put("resumenPorEstado", resumenPorEstado);

    return resumen;
  }

  /**
   * Valida que el tipo de documento sea adecuado para el cliente
   */
  private void validarTipoDocumento(Comprobante.TipoDocumento tipoDocumento, Clientes cliente) {
    if (tipoDocumento == Comprobante.TipoDocumento.FACTURA) {
      // Para facturas el cliente debe tener RUC
      if (cliente.getRuc() == null || cliente.getRuc().isBlank()) {
        throw new BusinessException("Para emitir una factura el cliente debe tener un RUC válido");
      }
    }
  }

  /**
   * Obtiene la serie predeterminada según el tipo de documento
   */
  private String obtenerSerieDefaultPorTipo(Comprobante.TipoDocumento tipoDocumento) {
    switch (tipoDocumento) {
      case FACTURA:
        return "F001";
      case BOLETA:
        return "B001";
      case NOTA_VENTA:
        return "NV01";
      case TICKET:
        return "T001";
      default:
        return "B001";
    }
  }

  /**
   * Mapea el tipo de documento del comprobante al tipo de comprobante de la venta
   */
  private Venta.TipoComprobante mapTipoComprobanteVenta(Comprobante.TipoDocumento tipoDocumento) {
    switch (tipoDocumento) {
      case FACTURA:
        return Venta.TipoComprobante.FACTURA;
      case BOLETA:
        return Venta.TipoComprobante.BOLETA;
      case NOTA_VENTA:
        return Venta.TipoComprobante.NOTA_VENTA;
      case TICKET:
        return Venta.TipoComprobante.TICKET;
      default:
        return Venta.TipoComprobante.BOLETA;
    }
  }

  /**
   * Clase auxiliar para agrupar información de facturación por cliente
   */
  private static class ClienteFacturacion {
    Long clienteId;
    String nombreCliente;
    BigDecimal montoTotal;
    int cantidadComprobantes;

    public ClienteFacturacion(Long clienteId, String nombreCliente) {
      this.clienteId = clienteId;
      this.nombreCliente = nombreCliente;
      this.montoTotal = BigDecimal.ZERO;
      this.cantidadComprobantes = 0;
    }
  }

}
