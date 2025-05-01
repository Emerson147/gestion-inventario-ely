package com.emersondev.service.interfaces;

public interface DocumentoGeneratorService {
  /**
   * Genera un archivo PDF para un comprobante
   * @param comprobanteId ID del comprobante
   * @return array de bytes del PDF
   */
  byte[] generarPdfComprobante(Long comprobanteId);

  /**
   * Genera un archivo XML para un comprobante
   * @param comprobanteId ID del comprobante
   * @return array de bytes del XML
   */
  byte[] generarXmlComprobante(Long comprobanteId);
}
