package com.emersondev.service.impl;

import com.emersondev.domain.entity.Comprobante;
import com.emersondev.domain.entity.DetalleComprobante;
import com.emersondev.domain.entity.DetalleVenta;
import com.emersondev.domain.entity.Venta;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.ComprobanteRepository;
import com.emersondev.domain.repository.VentaRepository;
import com.emersondev.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"StringBufferReplaceableByString", "unused"})
public class TicketServiceImpl implements TicketService {

    private final ComprobanteRepository comprobanteRepository;
    private final VentaRepository ventaRepository;

    @Value("${app.empresa.nombre:MI EMPRESA}")
    private String nombreEmpresa;
    
    @Value("${app.empresa.ruc:20123456789}")
    private String rucEmpresa;
    
    @Value("${app.empresa.direccion:Av. Principal 123}")
    private String direccionEmpresa;
    
    @Value("${app.empresa.telefono:123-456-789}")
    private String telefonoEmpresa;
    
    @Value("${app.ticketera.ancho-papel:58}")
    private int anchoPapel;

    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FECHA_SIMPLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_SIMPLE = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public String generarFormatoTicket(Long comprobanteId) {
        log.debug("Generando formato de ticket para comprobante ID: {}", comprobanteId);
        
        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante", "id", comprobanteId.toString()));
        
        return generarFormatoTicket(comprobante);
    }

    @Override
    public String generarFormatoTicket(Comprobante comprobante) {
        try {
            StringBuilder ticket = new StringBuilder();

            // Encabezado de la empresa
            ticket.append(generarEncabezado());

            // Información del comprobante
            ticket.append(generarInfoComprobante(comprobante));

            // Información del cliente
            ticket.append(generarInfoCliente(comprobante));

            // Detalles de productos
            ticket.append(generarDetallesProductos(comprobante));

            // Totales (sin IGV porque ya está incluido en el precio)
            ticket.append(generarTotalesSinIGV(comprobante));

            // Información adicional
            ticket.append(generarInfoAdicional(comprobante));

            // Pie del ticket
            ticket.append(generarPie());
            
            return ticket.toString();
            
        } catch (Exception e) {
            log.error("Error al generar formato de ticket: {}", e.getMessage(), e);
            return generarTicketError();
        }
    }

    @Override
    public String generarTicketPrueba() {
        StringBuilder ticket = new StringBuilder();
        
        ticket.append("@CENTER@");
        ticket.append("@BOLD@TICKET DE PRUEBA@/BOLD@\n");
        ticket.append("@CENTER@");
        ticket.append("================================\n");
        ticket.append("@LEFT@");
        ticket.append("Fecha: ").append(java.time.LocalDateTime.now().format(FECHA_FORMATO)).append("\n");
        ticket.append("Usuario: SISTEMA\n");
        ticket.append("--------------------------------\n");
        ticket.append("Este es un ticket de prueba\n");
        ticket.append("para verificar la conexión\n");
        ticket.append("con la ticketera XPrinter XP-V320M\n");
        ticket.append("--------------------------------\n");
        ticket.append("@CENTER@");
        ticket.append("Prueba exitosa!\n");
        ticket.append("@CENTER@");
        ticket.append("================================\n");
        
        return ticket.toString();
    }

    @Override
    public boolean validarComprobanteParaImpresion(Long comprobanteId) {
        try {
            Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                    .orElse(null);
            
            if (comprobante == null) {
                log.error("Comprobante {} no encontrado", comprobanteId);
                return false;
            }
            
            if (comprobante.getEstado() == Comprobante.EstadoComprobante.ANULADO) {
                log.error("No se puede imprimir un comprobante anulado: {}", comprobanteId);
                return false;
            }
            
            if (comprobante.getDetalles() == null || comprobante.getDetalles().isEmpty()) {
                log.error("El comprobante {} no tiene detalles", comprobanteId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error al validar comprobante {}: {}", comprobanteId, e.getMessage());
            return false;
        }
    }

  @Override
  public String generarFormatoTicketDesdeVenta(Long ventaId) {
    log.debug("Generando formato de ticket desde venta ID: {}", ventaId);

    // Aquí necesitas inyectar VentaRepository
    Venta venta = ventaRepository.findById(ventaId)
            .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", ventaId.toString()));

    return generarTicketDesdeVenta(venta);
  }

  /**
   * Genera un ticket formateado desde una venta (sin comprobante)
   */
  private String generarTicketDesdeVenta(Venta venta) {
    StringBuilder ticket = new StringBuilder();

    // Encabezado de la empresa
    ticket.append("@CENTER@");
    ticket.append("@BOLD@").append(nombreEmpresa).append("@/BOLD@\n");
    ticket.append("@CENTER@");
    ticket.append("RUC: ").append(rucEmpresa).append("\n");
    ticket.append("@CENTER@");
    ticket.append(direccionEmpresa).append("\n");
    ticket.append("@CENTER@");
    ticket.append("Tel: ").append(telefonoEmpresa).append("\n");
    ticket.append("@CENTER@");
    ticket.append(generarLinea("=")).append("\n");

    // Información de venta
    ticket.append("@CENTER@");
    ticket.append("@BOLD@TICKET DE VENTA@/BOLD@\n");
    ticket.append("@LEFT@");
    ticket.append("Nro: ").append(venta.getNumeroVenta()).append("\n");
    ticket.append("Fecha: ").append(venta.getFechaCreacion().format(FECHA_SIMPLE)).append("\n");
    ticket.append("Hora: ").append(venta.getFechaCreacion().format(HORA_SIMPLE)).append("\n");
    ticket.append("Vendedor: ").append(venta.getUsuario().getUsername()).append("\n");
    ticket.append(generarLinea("-")).append("\n");

    // Información del cliente
    if (venta.getCliente() != null) {
      ticket.append("CLIENTE:\n");
      ticket.append(venta.getCliente().getNombres()).append(" ");
      ticket.append(venta.getCliente().getApellidos()).append("\n");

      if (venta.getCliente().getDni() != null) {
        ticket.append("DNI: ").append(venta.getCliente().getDni()).append("\n");
      }

      if (venta.getCliente().getRuc() != null && !venta.getCliente().getRuc().isEmpty()) {
        ticket.append("RUC: ").append(venta.getCliente().getRuc()).append("\n");
      }

      ticket.append(generarLinea("-")).append("\n");
    }

    // Detalles de productos
    ticket.append("PRODUCTOS:\n");
    ticket.append("Cant  Descripcion       Precio\n");
    ticket.append(generarLinea("-")).append("\n");

    for (DetalleVenta detalle : venta.getDetalles()) {
      String cantidad = String.format("%4d", detalle.getCantidad());
      String precio = String.format("S/ %8.2f", detalle.getSubtotal());
      String descripcion = truncarTexto(detalle.getProducto().getNombre());

      ticket.append(cantidad).append("  ");
      ticket.append(String.format("%-20s", descripcion));
      ticket.append(precio).append("\n");

      // Información adicional (color y talla si existen)
      if (detalle.getColor() != null || detalle.getTalla() != null) {
        StringBuilder adicional = new StringBuilder("      ");
        if (detalle.getColor() != null) {
          adicional.append("Color: ").append(detalle.getColor().getNombre());
        }
        if (detalle.getTalla() != null) {
          if (detalle.getColor() != null) adicional.append(" - ");
          adicional.append("Talla: ").append(detalle.getTalla().getNumero());
        }
        ticket.append(adicional).append("\n");
      }

      // Precio unitario si cantidad > 1
      if (detalle.getCantidad() > 1) {
        ticket.append(String.format("      S/ %.2f c/u", detalle.getPrecioUnitario())).append("\n");
      }
    }

    ticket.append(generarLinea("-")).append("\n");

    // Totales con desglose de IGV (igual que en comprobantes)
    ticket.append(generarLinea("-")).append("\n");

    // Mostrar operación gravada (subtotal sin IGV)
    ticket.append(String.format("%-20s S/ %8.2f", "Op. Gravada:", venta.getSubtotal()));
    ticket.append("\n");

    // Mostrar IGV (18%)
    ticket.append(String.format("%-20s S/ %8.2f", "IGV (18%):", venta.getIgv()));
    ticket.append("\n");

    ticket.append(generarLinea("-")).append("\n");

    // Mostrar total con IGV incluido
    ticket.append("@BOLD@");
    ticket.append(String.format("%-20s S/ %8.2f", "TOTAL A PAGAR:", venta.getTotal()));
    ticket.append("@/BOLD@").append("\n");

    ticket.append(generarLinea("=")).append("\n");

    // Observaciones si las hay
    if (venta.getObservaciones() != null && !venta.getObservaciones().isEmpty()) {
      ticket.append("Observaciones:\n");
      ticket.append(venta.getObservaciones()).append("\n");
      ticket.append(generarLinea("-")).append("\n");
    }

    // Pie del ticket
    ticket.append("@CENTER@");
    ticket.append("Gracias por su compra\n");
    ticket.append("@CENTER@");
    ticket.append("Vuelva pronto!\n");
    ticket.append("@CENTER@");
    ticket.append(generarLinea("=")).append("\n");
    ticket.append("@CENTER@");
    ticket.append("Sistema de Inventario\n");
    ticket.append("@CENTER@");
    ticket.append("www.miempresa.com\n");

    return ticket.toString();
  }

    /**
     * Genera el encabezado del ticket con información de la empresa
     */
      private String generarEncabezado() {
          StringBuilder encabezado = new StringBuilder();

          encabezado.append("@CENTER@");
          encabezado.append("@BOLD@").append(nombreEmpresa).append("@/BOLD@\n");
          encabezado.append("@CENTER@");
          encabezado.append("RUC: ").append(rucEmpresa).append("\n");
          encabezado.append("@CENTER@");
          encabezado.append(direccionEmpresa).append("\n");
          encabezado.append("@CENTER@");
          encabezado.append("Tel: ").append(telefonoEmpresa).append("\n");
          encabezado.append("@CENTER@");
          encabezado.append(generarLinea("=")).append("\n");

          return encabezado.toString();
      }

    /**
     * Genera la información del comprobante
     */
    private String generarInfoComprobante(Comprobante comprobante) {
        StringBuilder info = new StringBuilder();
        
        info.append("@CENTER@");
        info.append("@BOLD@").append(comprobante.getTipoDocumento().name()).append("@/BOLD@\n");
        info.append("@CENTER@");
        info.append(comprobante.getSerie()).append("-").append(comprobante.getNumero()).append("\n");
        info.append("@LEFT@");
        info.append("Fecha: ").append(comprobante.getFechaEmision().format(FECHA_SIMPLE)).append("\n");
        info.append("Hora: ").append(comprobante.getFechaEmision().format(HORA_SIMPLE)).append("\n");
        info.append("Usuario: ").append(comprobante.getUsuario().getUsername()).append("\n");
        info.append(generarLinea("-")).append("\n");
        
        return info.toString();
    }

    /**
     * Genera la información del cliente
     */
    private String generarInfoCliente(Comprobante comprobante) {
        StringBuilder cliente = new StringBuilder();
        
        cliente.append("CLIENTE:\n");
        cliente.append(comprobante.getCliente().getNombres()).append(" ");
        cliente.append(comprobante.getCliente().getApellidos()).append("\n");
        
        if (comprobante.getCliente().getDni() != null) {
            cliente.append("DNI: ").append(comprobante.getCliente().getDni()).append("\n");
        }
        
        if (comprobante.getCliente().getRuc() != null && !comprobante.getCliente().getRuc().isEmpty()) {
            cliente.append("RUC: ").append(comprobante.getCliente().getRuc()).append("\n");
        }
        
        cliente.append(generarLinea("-")).append("\n");
        
        return cliente.toString();
    }

    /**
     * Genera los detalles de productos
     */
    private String generarDetallesProductos(Comprobante comprobante) {
        StringBuilder detalles = new StringBuilder();
        
        detalles.append("PRODUCTOS:\n");
        detalles.append("Cant  Descripcion       Precio\n");
        detalles.append(generarLinea("-")).append("\n");
        
        for (DetalleComprobante detalle : comprobante.getDetalles()) {
            // Línea principal del producto
            String cantidad = String.format("%4d", detalle.getCantidad());
            String precio = String.format("S/ %8.2f", detalle.getSubtotal());
            
            // Descripción del producto (máximo caracteres según ancho)
            String descripcion = truncarTexto(detalle.getProducto().getNombre());

            detalles.append(cantidad).append("  ");
            detalles.append(String.format("%-20s", descripcion));
            detalles.append(precio).append("\n");
            
            // Información adicional del producto (color y talla)
            if (detalle.getColor() != null || detalle.getTalla() != null) {
                StringBuilder adicional = new StringBuilder("      ");
                if (detalle.getColor() != null) {
                    adicional.append("Color: ").append(detalle.getColor().getNombre());
                }
                if (detalle.getTalla() != null) {
                    if (detalle.getColor() != null) adicional.append(" - ");
                    adicional.append("Talla: ").append(detalle.getTalla().getNumero());
                }
                detalles.append(adicional).append("\n");
            }
            
            // Precio unitario si la cantidad es mayor a 1
            if (detalle.getCantidad() > 1) {
                detalles.append(String.format("      S/ %.2f c/u", detalle.getPrecioUnitario())).append("\n");
            }
        }
        
        detalles.append(generarLinea("-")).append("\n");
        
        return detalles.toString();
    }

    /**
     * Genera los totales del comprobante sin IGV separado
     */
    private String generarTotalesSinIGV(Comprobante comprobante) {
        StringBuilder totales = new StringBuilder();
        
        totales.append(generarLinea("-")).append("\n");

        // Mostrar operación gravada (subtotal sin IGV)
        totales.append(String.format("%-20s S/ %8.2f", "Op. Gravada:", comprobante.getSubtotal()));
        totales.append("\n");

        // Mostrar IGV (18%)
        totales.append(String.format("%-20s S/ %8.2f", "IGV (18%):", comprobante.getIgv()));
        totales.append("\n");

        totales.append(generarLinea("-")).append("\n");

        // Mostrar total con IGV incluido
        totales.append("@BOLD@");
        totales.append(String.format("%-20s S/ %8.2f", "TOTAL A PAGAR:", comprobante.getTotal()));
        totales.append("@/BOLD@").append("\n");
        
        totales.append(generarLinea("=")).append("\n");
        
        return totales.toString();
    }

    /**
     * Genera los totales del comprobante (método original mantenido para compatibilidad)
     */
    @SuppressWarnings("unused")
    private String generarTotales(Comprobante comprobante) {
        return generarTotalesSinIGV(comprobante);
    }

    /**
     * Genera información adicional del comprobante
     */
    private String generarInfoAdicional(Comprobante comprobante) {
        StringBuilder adicional = new StringBuilder();
        
        // Número de venta referenciado
        if (comprobante.getVenta() != null) {
            adicional.append("Venta #: ").append(comprobante.getVenta().getNumeroVenta()).append("\n");
        }
        
        // Observaciones si las hay
        if (comprobante.getObservaciones() != null && !comprobante.getObservaciones().isEmpty()) {
            adicional.append("Observaciones:\n");
            adicional.append(comprobante.getObservaciones()).append("\n");
        }
        
        adicional.append(generarLinea("-")).append("\n");
        
        return adicional.toString();
    }

    /**
     * Genera el pie del ticket
     */
    private String generarPie() {
        StringBuilder pie = new StringBuilder();
        
        pie.append("@CENTER@");
        pie.append("Gracias por su compra\n");
        pie.append("@CENTER@");
        pie.append("Vuelva pronto!\n");
        pie.append("@CENTER@");
        pie.append(generarLinea("=")).append("\n");
        pie.append("@CENTER@");
        pie.append("Sistema de Inventario\n");
        pie.append("@CENTER@");
        pie.append("www.miempresa.com\n");
        
        return pie.toString();
    }

    /**
     * Genera un ticket de error
     */
    private String generarTicketError() {
        StringBuilder error = new StringBuilder();
        
        error.append("@CENTER@");
        error.append("@BOLD@ERROR EN TICKET@/BOLD@\n");
        error.append("@CENTER@");
        error.append("================================\n");
        error.append("@LEFT@");
        error.append("No se pudo generar el ticket\n");
        error.append("correctamente.\n\n");
        error.append("Por favor, contacte al\n");
        error.append("administrador del sistema.\n");
        error.append("@CENTER@");
        error.append("================================\n");
        
        return error.toString();
    }

    /**
     * Genera una línea con el caracter especificado
     */
    private String generarLinea(String caracter) {
        int longitud = Math.min(anchoPapel / 2, 32); // Máximo 32 caracteres para papel de 58mm
        return caracter.repeat(longitud);
    }

    /**
     * Trunca un texto al tamaño especificado
     */
    @SuppressWarnings("SameParameterValue")
    private String truncarTexto(String texto) {
        return truncarTexto(texto, 20);
    }

    /**
     * Trunca un texto al tamaño especificado
     */
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }
}
