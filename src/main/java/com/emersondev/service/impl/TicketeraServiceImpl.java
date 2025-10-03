package com.emersondev.service.impl;

import com.emersondev.service.interfaces.TicketeraService;
import com.emersondev.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketeraServiceImpl implements TicketeraService {

    private final TicketService ticketService;

    @Value("${app.ticketera.puerto:USB}")
    private String puertoConfiguracion;

    @Value("${app.ticketera.modelo:XPrinter XP-V320M}")
    private String modeloTicketera;

    @Value("${app.ticketera.ancho-papel:58}")
    private int anchoPapel;

    @Value("${app.ticketera.timeout:5000}")
    private int timeoutConexion;

    // Comandos ESC/POS específicos para XPrinter XP-V320M
    private static final byte[] INIT = {0x1B, 0x40}; // Inicializar impresora
    private static final byte[] CUT_PAPER = {0x1D, 0x56, 0x42, 0x00}; // Cortar papel
    private static final byte[] OPEN_DRAWER = {0x1B, 0x70, 0x00, 0x19, (byte)0xFA}; // Abrir cajón
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01}; // Centrar texto
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00}; // Alinear izquierda
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01}; // Activar negrita
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00}; // Desactivar negrita
    private static final byte[] DOUBLE_HEIGHT = {0x1B, 0x21, 0x10}; // Doble altura
    private static final byte[] NORMAL_SIZE = {0x1B, 0x21, 0x00}; // Tamaño normal
    private static final byte[] LINE_FEED = {0x0A}; // Salto de línea

    private PrintService impresora;

    @Override
    public boolean imprimirTicket(Long comprobanteId) {
        try {
            log.info("Iniciando impresión de ticket para comprobante ID: {}", comprobanteId);

            // Validar comprobante
            if (!ticketService.validarComprobanteParaImpresion(comprobanteId)) {
                log.error("El comprobante {} no es válido para impresión", comprobanteId);
                return false;
            }

            // Generar formato del ticket
            String contenidoTicket = ticketService.generarFormatoTicket(comprobanteId);

            // Imprimir
            return imprimirTexto(contenidoTicket);

        } catch (Exception e) {
            log.error("Error al imprimir ticket para comprobante {}: {}", comprobanteId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean imprimirTexto(String texto) {
        try {
            log.debug("Imprimiendo texto en ticketera");

            PrintService impresora = obtenerImpresora();
            if (impresora == null) {
                log.error("No se pudo encontrar la impresora");
                return false;
            }

            // Crear documento de impresión
            DocPrintJob printJob = impresora.createPrintJob();

            // Preparar contenido con comandos ESC/POS
            byte[] contenido = prepararContenidoESCPOS(texto);

            // Crear documento
            Doc documento = new SimpleDoc(
                new ByteArrayInputStream(contenido),
                DocFlavor.INPUT_STREAM.AUTOSENSE,
                null
            );

            // Configurar atributos de impresión
            PrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();
            atributos.add(new Copies(1));

            // Imprimir
            printJob.print(documento, atributos);
            log.info("Ticket enviado a impresión exitosamente");

            return true;

        } catch (Exception e) {
            log.error("Error durante la impresión: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> obtenerPuertosDisponibles() {
        List<String> puertos = new ArrayList<>();

        try {
            // Obtener servicios de impresión disponibles
            PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);

            for (PrintService servicio : servicios) {
                String nombre = servicio.getName();
                // Filtrar por impresoras que podrían ser ticketeras
                if (nombre.toLowerCase().contains("xprinter") ||
                    nombre.toLowerCase().contains("pos") ||
                    nombre.toLowerCase().contains("thermal") ||
                    nombre.toLowerCase().contains("ticket")) {
                    puertos.add(nombre);
                }
            }

            // Agregar puertos serie comunes
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                for (int i = 1; i <= 10; i++) {
                    puertos.add("COM" + i);
                }
            } else {
                puertos.add("/dev/ttyUSB0");
                puertos.add("/dev/ttyUSB1");
                puertos.add("/dev/ttyS0");
                puertos.add("/dev/ttyS1");
            }

            puertos.add("USB");
            puertos.add("LPT1");

        } catch (Exception e) {
            log.error("Error al obtener puertos disponibles: {}", e.getMessage());
        }

        return puertos;
    }

    @Override
    public boolean verificarConexion() {
        try {
            PrintService impresora = obtenerImpresora();
            if (impresora == null) {
                return false;
            }

            // Intentar imprimir un comando simple de inicialización
            byte[] testCommand = INIT;

            DocPrintJob printJob = impresora.createPrintJob();
            Doc documento = new SimpleDoc(
                new ByteArrayInputStream(testCommand),
                DocFlavor.INPUT_STREAM.AUTOSENSE,
                null
            );

            printJob.print(documento, null);

            log.info("Conexión con ticketera verificada exitosamente");
            return true;

        } catch (Exception e) {
            log.error("Error al verificar conexión con ticketera: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean configurarPuerto(String puerto) {
        try {
            log.info("Configurando puerto de ticketera: {}", puerto);
            this.puertoConfiguracion = puerto;
            this.impresora = null; // Resetear impresora para buscar nuevamente

            // Verificar la nueva configuración
            return verificarConexion();

        } catch (Exception e) {
            log.error("Error al configurar puerto {}: {}", puerto, e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> obtenerConfiguracion() {
        Map<String, Object> configuracion = new HashMap<>();

        configuracion.put("puerto", puertoConfiguracion);
        configuracion.put("modelo", modeloTicketera);
        configuracion.put("anchoPapel", anchoPapel);
        configuracion.put("timeout", timeoutConexion);
        configuracion.put("conectada", verificarConexion());
        configuracion.put("puertosDisponibles", obtenerPuertosDisponibles());

        // Información de la impresora actual
        PrintService impresora = obtenerImpresora();
        if (impresora != null) {
            configuracion.put("nombreImpresora", impresora.getName());
            configuracion.put("estadoImpresora", "Disponible");
        } else {
            configuracion.put("nombreImpresora", "No encontrada");
            configuracion.put("estadoImpresora", "Desconectada");
        }

        return configuracion;
    }

    @Override
    public boolean cortarPapel() {
        try {
            log.debug("Enviando comando de corte de papel");

            PrintService impresora = obtenerImpresora();
            if (impresora == null) {
                return false;
            }

            DocPrintJob printJob = impresora.createPrintJob();
            Doc documento = new SimpleDoc(
                new ByteArrayInputStream(CUT_PAPER),
                DocFlavor.INPUT_STREAM.AUTOSENSE,
                null
            );

            printJob.print(documento, null);
            log.debug("Comando de corte enviado exitosamente");

            return true;

        } catch (Exception e) {
            log.error("Error al cortar papel: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean abrirCajon() {
        try {
            log.debug("Enviando comando para abrir cajón");

            PrintService impresora = obtenerImpresora();
            if (impresora == null) {
                return false;
            }

            DocPrintJob printJob = impresora.createPrintJob();
            Doc documento = new SimpleDoc(
                new ByteArrayInputStream(OPEN_DRAWER),
                DocFlavor.INPUT_STREAM.AUTOSENSE,
                null
            );

            printJob.print(documento, null);
            log.info("Comando para abrir cajón enviado exitosamente");

            return true;

        } catch (Exception e) {
            log.error("Error al abrir cajón: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene la impresora configurada
     */
    private PrintService obtenerImpresora() {
        if (this.impresora != null) {
            return this.impresora;
        }

        try {
            PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);

            // Buscar por nombre específico primero
            for (PrintService servicio : servicios) {
                String nombre = servicio.getName().toLowerCase();
                if (nombre.contains("xprinter") || nombre.contains(puertoConfiguracion.toLowerCase())) {
                    this.impresora = servicio;
                    log.info("Impresora encontrada: {}", servicio.getName());
                    return this.impresora;
                }
            }

            // Si no se encuentra por nombre, buscar impresoras tipo POS
            for (PrintService servicio : servicios) {
                String nombre = servicio.getName().toLowerCase();
                if (nombre.contains("pos") || nombre.contains("thermal") || nombre.contains("ticket")) {
                    this.impresora = servicio;
                    log.info("Impresora POS encontrada: {}", servicio.getName());
                    return this.impresora;
                }
            }

            // Como último recurso, usar la impresora predeterminada
            this.impresora = PrintServiceLookup.lookupDefaultPrintService();
            if (this.impresora != null) {
                log.warn("Usando impresora predeterminada: {}", this.impresora.getName());
            }

        } catch (Exception e) {
            log.error("Error al buscar impresora: {}", e.getMessage());
        }

        return this.impresora;
    }

    /**
     * Prepara el contenido con comandos ESC/POS específicos para XPrinter
     */
    private byte[] prepararContenidoESCPOS(String texto) {
        try {
            List<Byte> contenido = new ArrayList<>();

            // Inicializar impresora
            for (byte b : INIT) contenido.add(b);

            // Procesar el texto línea por línea
            String[] lineas = texto.split("\n");

            for (String linea : lineas) {
                // Detectar comandos especiales en el texto
                if (linea.startsWith("@CENTER@")) {
                    for (byte b : ALIGN_CENTER) contenido.add(b);
                    linea = linea.substring(8);
                } else if (linea.startsWith("@LEFT@")) {
                    for (byte b : ALIGN_LEFT) contenido.add(b);
                    linea = linea.substring(6);
                } else if (linea.startsWith("@BOLD@")) {
                    for (byte b : BOLD_ON) contenido.add(b);
                    linea = linea.substring(6);
                } else if (linea.startsWith("@/BOLD@")) {
                    for (byte b : BOLD_OFF) contenido.add(b);
                    linea = linea.substring(7);
                } else if (linea.startsWith("@BIG@")) {
                    for (byte b : DOUBLE_HEIGHT) contenido.add(b);
                    linea = linea.substring(5);
                } else if (linea.startsWith("@/BIG@")) {
                    for (byte b : NORMAL_SIZE) contenido.add(b);
                    linea = linea.substring(6);
                }

                // Agregar contenido de la línea
                if (!linea.isEmpty()) {
                    byte[] lineaBytes = linea.getBytes(StandardCharsets.UTF_8);
                    for (byte b : lineaBytes) contenido.add(b);
                }

                // Agregar salto de línea
                for (byte b : LINE_FEED) contenido.add(b);
            }

            // Agregar comandos finales
            for (byte b : LINE_FEED) contenido.add(b);
            for (byte b : LINE_FEED) contenido.add(b);
            for (byte b : CUT_PAPER) contenido.add(b);

            // Convertir a array
            byte[] resultado = new byte[contenido.size()];
            for (int i = 0; i < contenido.size(); i++) {
                resultado[i] = contenido.get(i);
            }

            return resultado;

        } catch (Exception e) {
            log.error("Error al preparar contenido ESC/POS: {}", e.getMessage());
            return texto.getBytes(StandardCharsets.UTF_8);
        }
    }
}
