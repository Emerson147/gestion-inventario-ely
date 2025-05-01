package com.emersondev.domain.repository;

import com.emersondev.domain.entity.DetalleComprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleComprobanteRepository extends JpaRepository<DetalleComprobante, Long> {

  /**
   * Lista detalles por comprobante
   */
  List<DetalleComprobante> findByComprobanteId(Long comprobanteId);

  /**
   * Lista detalles por producto
   */
  List<DetalleComprobante> findByProductoId(Long productoId);

  /**
   * Elimina detalles por comprobante
   */
  void deleteByComprobanteId(Long comprobanteId);
}
