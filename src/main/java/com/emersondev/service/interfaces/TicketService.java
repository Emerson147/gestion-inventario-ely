package com.emersondev.service.interfaces;

import com.emersondev.domain.entity.Comprobante;

public interface TicketService {
    
    /**
     * Genera el formato de ticket para impresión
     * @param comprobanteId ID del comprobante
     * @return Texto formateado para la ticketera
     */
    String generarFormatoTicket(Long comprobanteId);
    
    /**
     * Genera el formato de ticket desde el objeto comprobante
     * @param comprobante Objeto comprobante
     * @return Texto formateado para la ticketera
     */
    String generarFormatoTicket(Comprobante comprobante);
    
    /**
     * Genera un ticket de prueba
     * @return Texto formateado de prueba
     */
    String generarTicketPrueba();
    
    /**
     * Valida que el comprobante sea válido para imprimir
     * @param comprobanteId ID del comprobante
     * @return true si es válido para imprimir
     */
    boolean validarComprobanteParaImpresion(Long comprobanteId);

    /**
     * Genera el formato de ticket directamente desde una venta
     */
    String generarFormatoTicketDesdeVenta(Long ventaId);
}
