package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Clientes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Clientes, Long> {

  Optional<Clientes> findByDocumento(String documento);

  Optional<Clientes> findByEmail(String email);

  Page<Clientes> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(
          String nombre, String apellido, Pageable pageable);

  Boolean existsByDocumento(String documento);

  Boolean existsByEmail(String email);
}
