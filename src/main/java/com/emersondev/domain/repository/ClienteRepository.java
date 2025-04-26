package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Clientes, Long> {

  /**
   * Encuentra clientes por estado
   */
  List<Clientes> findByEstadoTrue();

  /**
   * Encuentra un cliente por DNI
   */
  Optional<Clientes> findByDni(String dni);

  /**
   * Encuentra un cliente por RUC
   */
  Optional<Clientes> findByRuc(String ruc);

  /**
   * Encuentra un cliente por email
   */
  Optional<Clientes> findByEmail(String email);

  /**
   * Verifica si existe un cliente con el DNI especificado
   */
  boolean existsByDni(String dni);

  /**
   * Verifica si existe un cliente con el RUC especificado
   */
  boolean existsByRuc(String ruc);

  /**
   * Verifica si existe un cliente con el email especificado
   */
  boolean existsByEmail(String email);

  /**
   * Busca clientes por DNI, RUC, nombres o apellidos
   */
  @Query("SELECT c FROM Clientes c WHERE c.estado = true AND " +
          "(LOWER(c.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
          "LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
          "c.dni LIKE CONCAT('%', :termino, '%') OR " +
          "c.ruc LIKE CONCAT('%', :termino, '%'))")
  List<Clientes> buscarPorTermino(@Param("termino") String termino);
}
