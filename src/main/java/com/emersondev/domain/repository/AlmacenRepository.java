package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

  /**
   * @param nombre
   * @return Almacen
   */
  Optional<Almacen> findByNombre(String nombre);

}
