package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

  Optional<Rol> findByNombre(Rol.NombreRol nombreRol);
}
