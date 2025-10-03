package com.emersondev.api.controller;

import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.service.interfaces.TicketeraService;
import com.emersondev.service.interfaces.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
@Slf4j
public class TicketeraController {

    private final TicketeraService ticketeraService;
    private final TicketService ticketService;

    /**
     * Imprime un ticket en la ticketera
     */
    @PostMapping("/{id}/imprimir-ticket")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> imprimirTicket(@PathVariable Long id) {
        log.info("Solicitada impresión de ticket para comprobante ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar comprobante antes de imprimir
            if (!ticketService.validarComprobanteParaImpresion(id)) {
                response.put("success", false);
                response.put("message", "El comprobante no es válido para impresión");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar conexión con ticketera
            if (!ticketeraService.verificarConexion()) {
                response.put("success", false);
                response.put("message", "No se pudo conectar con la ticketera. Verifique la conexión.");
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Imprimir ticket
            boolean resultado = ticketeraService.imprimirTicket(id);
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Ticket enviado a impresión exitosamente");
                response.put("comprobanteId", id);
                log.info("Ticket impreso exitosamente para comprobante ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Error al enviar el ticket a impresión");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error al imprimir ticket para comprobante {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error interno al procesar la impresión: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene la configuración de impresión disponible
     */
    @GetMapping("/configuracion-impresion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> obtenerConfiguracionImpresion() {
        log.debug("Solicitada configuración de impresión");
        
        try {
            Map<String, Object> configuracion = ticketeraService.obtenerConfiguracion();
            
            // Agregar información adicional
            configuracion.put("success", true);
            configuracion.put("message", "Configuración obtenida exitosamente");
            
            return ResponseEntity.ok(configuracion);
            
        } catch (Exception e) {
            log.error("Error al obtener configuración de impresión: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener configuración: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Configura el puerto de la ticketera
     */
    @PostMapping("/configurar-puerto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> configurarPuerto(@RequestBody Map<String, String> request) {
        String puerto = request.get("puerto");
        log.info("Solicitada configuración de puerto: {}", puerto);
        
        Map<String, Object> response = new HashMap<>();
        
        if (puerto == null || puerto.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "El puerto es requerido");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean resultado = ticketeraService.configurarPuerto(puerto.trim());
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Puerto configurado exitosamente");
                response.put("puerto", puerto);
                response.put("configuracion", ticketeraService.obtenerConfiguracion());
            } else {
                response.put("success", false);
                response.put("message", "No se pudo configurar el puerto especificado");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al configurar puerto {}: {}", puerto, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al configurar puerto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene los puertos disponibles para la ticketera
     */
    @GetMapping("/puertos-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerPuertosDisponibles() {
        log.debug("Solicitados puertos disponibles");
        
        try {
            List<String> puertos = ticketeraService.obtenerPuertosDisponibles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Puertos obtenidos exitosamente");
            response.put("puertos", puertos);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener puertos disponibles: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al obtener puertos: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Verifica la conexión con la ticketera
     */
    @GetMapping("/verificar-conexion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> verificarConexion() {
        log.debug("Solicitada verificación de conexión con ticketera");
        
        try {
            boolean conectada = ticketeraService.verificarConexion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conectada", conectada);
            response.put("message", conectada ? "Ticketera conectada" : "Ticketera desconectada");
            
            if (conectada) {
                response.put("configuracion", ticketeraService.obtenerConfiguracion());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al verificar conexión: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("conectada", false);
            errorResponse.put("message", "Error al verificar conexión: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Imprime un ticket de prueba
     */
    @PostMapping("/imprimir-prueba")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> imprimirTicketPrueba() {
        log.info("Solicitada impresión de ticket de prueba");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar conexión primero
            if (!ticketeraService.verificarConexion()) {
                response.put("success", false);
                response.put("message", "No se pudo conectar con la ticketera");
                return ResponseEntity.internalServerError().body(response);
            }
            
            // Generar y enviar ticket de prueba
            String ticketPrueba = ticketService.generarTicketPrueba();
            boolean resultado = ticketeraService.imprimirTexto(ticketPrueba);
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Ticket de prueba enviado exitosamente");
                log.info("Ticket de prueba impreso exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al imprimir ticket de prueba");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al imprimir ticket de prueba: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error interno: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba la conexión con la ticketera
     */
    @PostMapping("/probar-conexion")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> probarConexion() {
        log.info("Probando conexión con ticketera");

        Map<String, Object> response = new HashMap<>();

        try {
            boolean conectada = ticketeraService.verificarConexion();

            if (conectada) {
                // Imprimir ticket de prueba
                String ticketPrueba = ticketService.generarTicketPrueba();
                boolean impreso = ticketeraService.imprimirTexto(ticketPrueba);

                if (impreso) {
                    response.put("success", true);
                    response.put("message", "Conexión exitosa - Ticket de prueba enviado");
                    response.put("conectada", true);
                } else {
                    response.put("success", false);
                    response.put("message", "Conexión establecida pero falló la impresión");
                    response.put("conectada", true);
                }
            } else {
                response.put("success", false);
                response.put("message", "No se pudo conectar con la ticketera");
                response.put("conectada", false);
            }

            response.put("configuracion", ticketeraService.obtenerConfiguracion());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al probar conexión: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al probar conexión: " + e.getMessage());
            response.put("conectada", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Corta el papel de la ticketera
     */
    @PostMapping("/cortar-papel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> cortarPapel() {
        log.info("Solicitado corte de papel");

        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean resultado = ticketeraService.cortarPapel();
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Papel cortado exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al cortar el papel");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al cortar papel: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al cortar papel: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Abre el cajón de dinero
     */
    @PostMapping("/abrir-cajon")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> abrirCajon() {
        log.info("Solicitada apertura de cajón");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean resultado = ticketeraService.abrirCajon();
            
            if (resultado) {
                response.put("success", true);
                response.put("message", "Cajón abierto exitosamente");
            } else {
                response.put("success", false);
                response.put("message", "Error al abrir el cajón");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al abrir cajón: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al abrir cajón: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtiene una vista previa del ticket (solo texto, sin imprimir)
     */
    @GetMapping("/{id}/vista-previa-ticket")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<Map<String, Object>> obtenerVistaPreviaTicket(@PathVariable Long id) {
        log.debug("Solicitada vista previa de ticket para comprobante ID: {}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!ticketService.validarComprobanteParaImpresion(id)) {
                response.put("success", false);
                response.put("message", "El comprobante no es válido para generar vista previa");
                return ResponseEntity.badRequest().body(response);
            }
            
            String contenidoTicket = ticketService.generarFormatoTicket(id);
            
            response.put("success", true);
            response.put("contenido", contenidoTicket);
            response.put("comprobanteId", id);
            response.put("message", "Vista previa generada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al generar vista previa para comprobante {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al generar vista previa: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

  /**
   * Imprime un ticket directamente desde una venta (sin necesidad de comprobante)
   */
  @PostMapping("/venta/{ventaId}/imprimir-ticket")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<Map<String, Object>> imprimirTicketDesdeVenta(@PathVariable Long ventaId) {
    log.info("Solicitada impresión de ticket para venta ID: {}", ventaId);

    Map<String, Object> response = new HashMap<>();

    try {
      // Verificar conexión con ticketera
      if (!ticketeraService.verificarConexion()) {
        response.put("success", false);
        response.put("message", "No se pudo conectar con la ticketera. Verifique la conexión.");
        return ResponseEntity.internalServerError().body(response);
      }

      // Generar formato del ticket desde la venta
      String contenidoTicket = ticketService.generarFormatoTicketDesdeVenta(ventaId);

      // Imprimir ticket
      boolean resultado = ticketeraService.imprimirTexto(contenidoTicket);

      if (resultado) {
        response.put("success", true);
        response.put("message", "Ticket enviado a impresión exitosamente");
        response.put("ventaId", ventaId);
        log.info("Ticket impreso exitosamente para venta ID: {}", ventaId);
        return ResponseEntity.ok(response);
      } else {
        response.put("success", false);
        response.put("message", "Error al enviar el ticket a impresión");
        return ResponseEntity.internalServerError().body(response);
      }

    } catch (ResourceNotFoundException e) {
      log.error("Venta no encontrada: {}", ventaId);
      response.put("success", false);
      response.put("message", "Venta no encontrada con ID: " + ventaId);
      return ResponseEntity.status(404).body(response);
    } catch (Exception e) {
      log.error("Error al imprimir ticket para venta {}: {}", ventaId, e.getMessage(), e);
      response.put("success", false);
      response.put("message", "Error interno al procesar la impresión: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }
}
