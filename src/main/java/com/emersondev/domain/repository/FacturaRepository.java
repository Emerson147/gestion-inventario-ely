package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
  Optional<Factura> findByNumeroFactura(String numeroFactura);

  Optional<Factura> findByVentaId(Long ventaId);

}
