package com.emersondev.service.impl;

import com.emersondev.domain.entity.Comprobante;
import com.emersondev.domain.entity.DetalleComprobante;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.ComprobanteRepository;
import com.emersondev.service.interfaces.DocumentoGeneratorService;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoGeneratorServiceImpl implements DocumentoGeneratorService {

  private final ComprobanteRepository comprobanteRepository;

  @Value("${app.documentos.ruta-descarga:src/main/resources/comprobantes}")
  private String rutaDescarga;

  @Override
  public byte[] generarPdfComprobante(Long comprobanteId) {
    log.debug("Generando PDF para comprobante ID: {}", comprobanteId);

    // Obtener el comprobante
    Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", comprobanteId.toString()));

    // Preparar los datos para el PDF
    Map<String, Object> datos = prepararDatosComprobante(comprobante);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      // Crear el documento PDF
      PdfWriter writer = new PdfWriter(baos);
      PdfDocument pdfDoc = new PdfDocument(writer);
      Document document = new Document(pdfDoc, PageSize.A4);
      document.setMargins(36, 36, 36, 36);

      // Configuración de fuentes
      PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
      PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
      PdfFont lightFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

      // ---------- ENCABEZADO MODERNO ----------
      Table header = new Table(UnitValue.createPercentArray(new float[]{30, 40, 30}));
      header.setWidth(UnitValue.createPercentValue(100));

      // Logo de la empresa (puedes reemplazar con un logo real)
      Cell logoCell = new Cell();
      try {
        ImageData logoData = ImageDataFactory.create(getClass().getResource("/static/images/logo.png"));
        Image logo = new Image(logoData).setHeight(100).setWidth(100).setAutoScale(false);
        logoCell.add(logo);
      } catch (Exception e) {
        // Si no hay logo, usar texto
        logoCell.add(new Paragraph("LA PERUANITA").setFont(boldFont).setFontSize(16));
      }
      logoCell.setBorder(Border.NO_BORDER);
      header.addCell(logoCell);

      // Información de la empresa
      Cell empresaCell = new Cell();
      empresaCell.add(new Paragraph("LA PERUANITA S.A.C.").setFont(boldFont).setFontSize(12).setFontColor(new DeviceRgb(40, 40, 40)));
      empresaCell.add(new Paragraph("RUC: 20123456789").setFont(normalFont).setFontSize(10));
      empresaCell.add(new Paragraph("Av. Principal 123, Lima").setFont(lightFont).setFontSize(9).setFontColor(new DeviceRgb(80, 80, 80)));
      empresaCell.add(new Paragraph("Tel: (01) 555-1234").setFont(lightFont).setFontSize(9).setFontColor(new DeviceRgb(80, 80, 80)));
      empresaCell.add(new Paragraph("contacto@laperuanita.pe").setFont(lightFont).setFontSize(9).setFontColor(new DeviceRgb(80, 80, 80)));
      empresaCell.setBorder(Border.NO_BORDER);
      header.addCell(empresaCell);

      // Información del comprobante en un recuadro destacado
      Cell comprobanteCell = new Cell();
      comprobanteCell.add(new Paragraph((String) datos.get("tipoDocumento")).setFont(boldFont).setFontSize(14)
              .setTextAlignment(TextAlignment.CENTER));
      comprobanteCell.add(new Paragraph("N° " + datos.get("serie") + "-" + datos.get("numero")).setFont(boldFont).setFontSize(12)
              .setTextAlignment(TextAlignment.CENTER));
      comprobanteCell.add(new Paragraph("Fecha de emisión:").setFont(normalFont).setFontSize(8)
              .setTextAlignment(TextAlignment.CENTER));
      comprobanteCell.add(new Paragraph(datos.get("fechaEmision").toString()).setFont(normalFont).setFontSize(10)
              .setTextAlignment(TextAlignment.CENTER));
      comprobanteCell.setBorder(new SolidBorder(new DeviceRgb(59, 89, 152), 1)); // Color azul corporativo
      comprobanteCell.setBackgroundColor(new DeviceRgb(242, 246, 252)); // Fondo celeste suave
      comprobanteCell.setPadding(8);
      comprobanteCell.setBorderRadius(new BorderRadius(5));
      header.addCell(comprobanteCell);

      document.add(header);
      document.add(new Paragraph("\n"));

      // ---------- INFORMACIÓN DEL CLIENTE CON DISEÑO MEJORADO ----------
      Map<String, Object> cliente = (Map<String, Object>) datos.get("cliente");

      Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{100}));
      clienteTable.setWidth(UnitValue.createPercentValue(100));

      Cell clienteTitleCell = new Cell();
      clienteTitleCell.add(new Paragraph("DATOS DEL CLIENTE")
              .setFont(boldFont)
              .setFontSize(10)
              .setFontColor(ColorConstants.WHITE));
      clienteTitleCell.setBackgroundColor(new DeviceRgb(59, 89, 152)); // Azul corporativo
      clienteTitleCell.setPadding(5);
      clienteTitleCell.setBorderRadius(new BorderRadius(3));
      clienteTable.addCell(clienteTitleCell);

      Cell clienteDetalleCell = new Cell();
      Table clienteDetalle = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
      clienteDetalle.setWidth(UnitValue.createPercentValue(100));

      String nombreCompleto = cliente.get("nombres") + " " + cliente.get("apellidos");
      clienteDetalle.addCell(createLabelCell("Cliente:", boldFont));
      clienteDetalle.addCell(createValueCell(nombreCompleto, normalFont));

      String identificacion = cliente.get("ruc") != null ? cliente.get("ruc").toString() : cliente.get("dni").toString();
      String tipoDoc = cliente.get("ruc") != null ? "RUC:" : "DNI:";
      clienteDetalle.addCell(createLabelCell(tipoDoc, boldFont));
      clienteDetalle.addCell(createValueCell(identificacion, normalFont));

      clienteDetalle.addCell(createLabelCell("Dirección:", boldFont));
      clienteDetalle.addCell(createValueCell(cliente.get("direccion").toString(), normalFont));

      clienteDetalleCell.add(clienteDetalle);
      clienteDetalleCell.setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 1));
      clienteDetalleCell.setPadding(8);
      clienteTable.addCell(clienteDetalleCell);

      document.add(clienteTable);
      document.add(new Paragraph("\n"));

      // ---------- DETALLES DEL COMPROBANTE ----------
      Table detallesTable = new Table(UnitValue.createPercentArray(new float[]{100}));
      detallesTable.setWidth(UnitValue.createPercentValue(100));

      Cell detallesTitleCell = new Cell();
      detallesTitleCell.add(new Paragraph("DETALLE DE PRODUCTOS")
              .setFont(boldFont)
              .setFontSize(10)
              .setFontColor(ColorConstants.WHITE));
      detallesTitleCell.setBackgroundColor(new DeviceRgb(59, 89, 152));
      detallesTitleCell.setPadding(5);
      detallesTitleCell.setBorderRadius(new BorderRadius(3));
      detallesTable.addCell(detallesTitleCell);

      Cell detallesContenidoCell = new Cell();
      Table detalles = new Table(UnitValue.createPercentArray(new float[]{5, 40, 10, 15, 15, 15}));
      detalles.setWidth(UnitValue.createPercentValue(100));

      // Cabecera de la tabla de detalles
      detalles.addHeaderCell(createHeaderCell("#", boldFont));
      detalles.addHeaderCell(createHeaderCell("Descripción", boldFont));
      detalles.addHeaderCell(createHeaderCell("Cant.", boldFont));
      detalles.addHeaderCell(createHeaderCell("P. Unit.", boldFont));
      detalles.addHeaderCell(createHeaderCell("IGV", boldFont));
      detalles.addHeaderCell(createHeaderCell("Total", boldFont));

      // Agregar filas de detalles
      List<Map<String, Object>> detallesList = (List<Map<String, Object>>) datos.get("detalles");
      int num = 1;

      for (Map<String, Object> detalle : detallesList) {
        Map<String, Object> producto = (Map<String, Object>) detalle.get("producto");
        Map<String, Object> color = (Map<String, Object>) detalle.get("color");
        Map<String, Object> talla = (Map<String, Object>) detalle.get("talla");

        String descripcionProducto = producto.get("nombre") + "\n" +
                "Color: " + color.get("nombre") + " | " +
                "Talla: " + talla.get("numero");

        // Agregar filas con colores alternados
        DeviceRgb rowColor = (num % 2 == 0) ? new DeviceRgb(245, 245, 245) : new DeviceRgb(255, 255, 255);

        detalles.addCell(createDetailCell(String.valueOf(num++), normalFont, rowColor, TextAlignment.CENTER));
        detalles.addCell(createDetailCell(descripcionProducto, normalFont, rowColor, TextAlignment.LEFT));
        detalles.addCell(createDetailCell(detalle.get("cantidad").toString(), normalFont, rowColor, TextAlignment.CENTER));
        detalles.addCell(createDetailCell(String.format("S/ %.2f", detalle.get("precioUnitario")), normalFont, rowColor, TextAlignment.RIGHT));
        detalles.addCell(createDetailCell(String.format("S/ %.2f", detalle.get("igv")), normalFont, rowColor, TextAlignment.RIGHT));
        detalles.addCell(createDetailCell(String.format("S/ %.2f", detalle.get("total")), boldFont, rowColor, TextAlignment.RIGHT));
      }

      detallesContenidoCell.add(detalles);
      detallesContenidoCell.setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 1));
      detallesContenidoCell.setPadding(0);
      detallesTable.addCell(detallesContenidoCell);

      document.add(detallesTable);
      document.add(new Paragraph("\n"));

      // ---------- TOTALES MEJORADOS ----------
      Table totalesOuterTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
      totalesOuterTable.setWidth(UnitValue.createPercentValue(100));

      // QR Code en lado izquierdo
      Cell qrCell = new Cell();
      qrCell.setBorder(Border.NO_BORDER);

      // Generar QR con datos del comprobante
      String qrContent = "RUC:20123456789|" + datos.get("tipoDocumento") + "|" +
              datos.get("serie") + "-" + datos.get("numero") + "|" +
              datos.get("igv") + "|" + datos.get("total") + "|" +
              datos.get("fechaEmision");

      BarcodeQRCode qrCode = new BarcodeQRCode(qrContent);
      PdfFormXObject qrCodeObject = qrCode.createFormXObject(ColorConstants.BLACK, pdfDoc);
      Image qrCodeImage = new Image(qrCodeObject).setWidth(80).setHeight(80);

      Table qrInfoTable = new Table(1);
      qrInfoTable.addCell(new Cell().add(qrCodeImage).setBorder(Border.NO_BORDER).setHorizontalAlignment(HorizontalAlignment.CENTER));
      qrInfoTable.addCell(new Cell().add(new Paragraph("Consulte su comprobante\nescaneando este código QR")
                      .setFontSize(8)
                      .setTextAlignment(TextAlignment.CENTER))
              .setBorder(Border.NO_BORDER));

      qrCell.add(qrInfoTable);
      totalesOuterTable.addCell(qrCell);

      // Totales en lado derecho
      Cell totalesCell = new Cell();
      totalesCell.setBorder(Border.NO_BORDER);

      Table totalesTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
      totalesTable.setWidth(UnitValue.createPercentValue(100));

      // Diseño moderno para los totales
      totalesTable.addCell(createTotalLabelCell("SUBTOTAL:", normalFont));
      totalesTable.addCell(createTotalValueCell("S/ " + String.format("%.2f", datos.get("subtotal")), normalFont));

      totalesTable.addCell(createTotalLabelCell("IGV (18%):", normalFont));
      totalesTable.addCell(createTotalValueCell("S/ " + String.format("%.2f", datos.get("igv")), normalFont));

      // Destacar el total
      Cell totalLabelCell = createTotalLabelCell("TOTAL:", boldFont);
      totalLabelCell.setBackgroundColor(new DeviceRgb(59, 89, 152));
      totalLabelCell.setPadding(8);
      totalesTable.addCell(totalLabelCell);

      Cell totalValueCell = createTotalValueCell("S/ " + String.format("%.2f", datos.get("total")), boldFont);
      totalValueCell.setBackgroundColor(new DeviceRgb(59, 89, 152));
      totalValueCell.setPadding(8);
      totalesTable.addCell(totalValueCell);

      totalesCell.add(totalesTable);
      totalesOuterTable.addCell(totalesCell);

      document.add(totalesOuterTable);

      // ---------- PIE DE PÁGINA CON INFORMACIÓN ADICIONAL ----------
      document.add(new Paragraph("\n"));

      // Información sobre observaciones o condiciones de pago
      Table pieTables = new Table(1);
      pieTables.setWidth(UnitValue.createPercentValue(100));

      // Condiciones de pago
      Cell condicionesCell = new Cell();
      condicionesCell.setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 1));
      condicionesCell.setPadding(5);

      Table condicionesTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
      condicionesTable.setWidth(UnitValue.createPercentValue(100));

      condicionesTable.addCell(createLabelCell("Condición de pago:", boldFont));
      condicionesTable.addCell(createValueCell(datos.getOrDefault("condicionPago", "Contado").toString(), normalFont));

      condicionesTable.addCell(createLabelCell("Método de pago:", boldFont));
      condicionesTable.addCell(createValueCell(datos.getOrDefault("metodoPago", "Efectivo").toString(), normalFont));

      condicionesCell.add(condicionesTable);
      pieTables.addCell(condicionesCell);

      // Ruta de descarga del PDF
      Cell rutaCell = new Cell();
      rutaCell.setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 1));
      rutaCell.setPadding(5);
      rutaCell.setMarginTop(5);

      Table rutaTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
      rutaTable.setWidth(UnitValue.createPercentValue(100));

      rutaTable.addCell(createLabelCell("Ruta de descarga:", boldFont));
      rutaTable.addCell(createValueCell(rutaDescarga, normalFont));

      rutaCell.add(rutaTable);
      pieTables.addCell(rutaCell);

      document.add(pieTables);

      // Mensaje final
      Paragraph mensajeFinal = new Paragraph("Gracias por su preferencia. Este documento es una representación impresa de un Comprobante Electrónico")
              .setFont(lightFont)
              .setFontSize(8)
              .setTextAlignment(TextAlignment.CENTER)
              .setMarginTop(10);
      document.add(mensajeFinal);

      // Agregar información de validez
      Paragraph validez = new Paragraph("Autorizado mediante Resolución de Superintendencia N° 203-2015/SUNAT")
              .setFont(lightFont)
              .setFontSize(8)
              .setTextAlignment(TextAlignment.CENTER)
              .setFontColor(new DeviceRgb(100, 100, 100));
      document.add(validez);

      document.close();

      // Guardar en la ruta especificada
      guardarPDFEnRuta(baos.toByteArray(), rutaDescarga, comprobante.getSerie(), comprobante.getNumero());

      log.info("PDF generado para comprobante: {}-{} y guardado en: {}",
              comprobante.getSerie(), comprobante.getNumero(), rutaDescarga);
      return baos.toByteArray();
    } catch (IOException e) {
      log.error("Error al generar PDF para comprobante: {}", comprobanteId, e);
      throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
    }
  }


  @Override
  public byte[] generarXmlComprobante(Long comprobanteId) {
    log.debug("Generando XML para comprobante ID: {}", comprobanteId);

    // Obtener el comprobante
    Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
            .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", comprobanteId.toString()));

    // En una implementación real, aquí usaríamos JAXB o alguna biblioteca similar
    // para generar el XML según el formato requerido por la autoridad tributaria

    // Preparar los datos para el XML
    Map<String, Object> datos = prepararDatosComprobante(comprobante);

    // Simulamos la generación del XML
    log.info("XML generado para comprobante: {}-{}", comprobante.getSerie(), comprobante.getNumero());

    // En un caso real, se generaría el XML y se devolvería como array de bytes
    // Por ahora, devolvemos un array de ejemplo
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><comprobante>Ejemplo de XML</comprobante>".getBytes();
  }

  /**
   * Prepara un mapa con los datos necesarios para generar los documentos
   */
  private Map<String, Object> prepararDatosComprobante(Comprobante comprobante) {
    Map<String, Object> datos = new HashMap<>();

    // Información del comprobante
    datos.put("tipoDocumento", comprobante.getTipoDocumento().name());
    datos.put("serie", comprobante.getSerie());
    datos.put("numero", comprobante.getNumero());
    datos.put("fechaEmision", comprobante.getFechaEmision());
    datos.put("codigoHash", comprobante.getCodigoHash());
    datos.put("subtotal", comprobante.getSubtotal());
    datos.put("igv", comprobante.getIgv());
    datos.put("total", comprobante.getTotal());

    // Información del cliente
    Map<String, Object> clienteInfo = new HashMap<>();
    clienteInfo.put("id", comprobante.getCliente().getId());
    clienteInfo.put("nombres", comprobante.getCliente().getNombres());
    clienteInfo.put("apellidos", comprobante.getCliente().getApellidos());
    clienteInfo.put("dni", comprobante.getCliente().getDni());
    clienteInfo.put("ruc", comprobante.getCliente().getRuc());
    clienteInfo.put("direccion", comprobante.getCliente().getDireccion());
    datos.put("cliente", clienteInfo);

    // Información del usuario
    Map<String, Object> usuarioInfo = new HashMap<>();
    usuarioInfo.put("id", comprobante.getUsuario().getId());
    usuarioInfo.put("nombre", comprobante.getUsuario().getNombres());
    usuarioInfo.put("username", comprobante.getUsuario().getUsername());
    datos.put("usuario", usuarioInfo);

    // Detalles
    datos.put("detalles", comprobante.getDetalles().stream()
            .map(this::mapDetalle)
            .collect(Collectors.toList()));

    return datos;
  }

  /**
   * Mapea un detalle a un mapa de datos
   */
  private Map<String, Object> mapDetalle(DetalleComprobante detalle) {
    Map<String, Object> map = new HashMap<>();
    map.put("id", detalle.getId());
    map.put("cantidad", detalle.getCantidad());
    map.put("precioUnitario", detalle.getPrecioUnitario());
    map.put("subtotal", detalle.getSubtotal());
    map.put("igv", detalle.getIgv());
    map.put("total", detalle.getTotal());
    map.put("descripcion", detalle.getDescripcion());
    map.put("unidadMedida", detalle.getUnidadMedida());
    map.put("codigoProducto", detalle.getCodigoProducto());

    // Información del producto
    Map<String, Object> productoInfo = new HashMap<>();
    productoInfo.put("id", detalle.getProducto().getId());
    productoInfo.put("codigo", detalle.getProducto().getCodigo());
    productoInfo.put("nombre", detalle.getProducto().getNombre());
    map.put("producto", productoInfo);

    // Información del color
    Map<String, Object> colorInfo = new HashMap<>();
    colorInfo.put("id", detalle.getColor().getId());
    colorInfo.put("nombre", detalle.getColor().getNombre());
    map.put("color", colorInfo);

    // Información de la talla
    Map<String, Object> tallaInfo = new HashMap<>();
    tallaInfo.put("id", detalle.getTalla().getId());
    tallaInfo.put("numero", detalle.getTalla().getNumero());
    map.put("talla", tallaInfo);

    return map;
  }

  // Métodos de ayuda para crear celdas con estilo consistente
  private Cell createLabelCell(String text, PdfFont font) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(9));
    cell.setBorder(Border.NO_BORDER);
    cell.setPadding(3);
    return cell;
  }

  private Cell createValueCell(String text, PdfFont font) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(9));
    cell.setBorder(Border.NO_BORDER);
    cell.setPadding(3);
    return cell;
  }

  private Cell createHeaderCell(String text, PdfFont font) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(ColorConstants.WHITE));
    cell.setBackgroundColor(new DeviceRgb(59, 89, 152));
    cell.setPadding(5);
    cell.setTextAlignment(TextAlignment.CENTER);
    return cell;
  }

  private Cell createDetailCell(String text, PdfFont font, DeviceRgb bgColor, TextAlignment alignment) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(8));
    cell.setBackgroundColor(bgColor);
    cell.setPadding(5);
    cell.setTextAlignment(alignment);
    return cell;
  }

  private Cell createTotalLabelCell(String text, PdfFont font) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(ColorConstants.WHITE));
    cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
    cell.setPadding(5);
    cell.setTextAlignment(TextAlignment.RIGHT);
    return cell;
  }

  private Cell createTotalValueCell(String text, PdfFont font) {
    Cell cell = new Cell();
    cell.add(new Paragraph(text).setFont(font).setFontSize(10).setFontColor(ColorConstants.WHITE));
    cell.setBackgroundColor(new DeviceRgb(100, 100, 100));
    cell.setPadding(5);
    cell.setTextAlignment(TextAlignment.RIGHT);
    return cell;
  }

  // Método para guardar el PDF en una ruta específica
  private void guardarPDFEnRuta(byte[] pdfBytes, String rutaBase, String serie, String numero) {
    try {
      String nombreArchivo = "Comprobante_" + serie + "-" + numero + ".pdf";
      Path dirPath = Paths.get(rutaBase);

      // Crear directorio si no existe
      if (!Files.exists(dirPath)) {
        Files.createDirectories(dirPath);
      }

      Path filePath = dirPath.resolve(nombreArchivo);
      Files.write(filePath, pdfBytes);

      log.info("PDF guardado correctamente en: {}", filePath);
    } catch (IOException e) {
      log.error("Error al guardar el PDF en el sistema de archivos", e);
      throw new RuntimeException("Error al guardar el PDF: " + e.getMessage(), e);
    }
  }
}