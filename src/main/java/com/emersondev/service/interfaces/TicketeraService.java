package com.emersondev.service.interfaces;

import java.util.List;
import java.util.Map;

public interface TicketeraService {
    
    /**
     * Imprime un ticket en la ticketera
     * @param comprobanteId ID del comprobante a imprimir
     * @return true si la impresión fue exitosa
     */
    boolean imprimirTicket(Long comprobanteId);
    
    /**
     * Imprime texto directo en la ticketera
     * @param texto Texto a imprimir
     * @return true si la impresión fue exitosa
     */
    boolean imprimirTexto(String texto);
    
    /**
     * Obtiene los puertos disponibles para la impresora
     * @return Lista de puertos disponibles
     */
    List<String> obtenerPuertosDisponibles();
    
    /**
     * Verifica si la ticketera está conectada y disponible
     * @return true si está disponible
     */
    boolean verificarConexion();
    
    /**
     * Configura el puerto de la ticketera
     * @param puerto Puerto a configurar (ej: COM1, /dev/ttyUSB0)
     * @return true si la configuración fue exitosa
     */
    boolean configurarPuerto(String puerto);
    
    /**
     * Obtiene la configuración actual de la ticketera
     * @return Mapa con la configuración actual
     */
    Map<String, Object> obtenerConfiguracion();
    
    /**
     * Corta el papel de la ticketera
     * @return true si el corte fue exitoso
     */
    boolean cortarPapel();
    
    /**
     * Abre el cajón de dinero conectado a la ticketera
     * @return true si la apertura fue exitosa
     */
    boolean abrirCajon();
}
