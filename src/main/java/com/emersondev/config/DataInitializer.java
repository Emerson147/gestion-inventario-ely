package com.emersondev.config;

import com.emersondev.domain.entity.Rol;
import com.emersondev.domain.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
  @Bean
  public CommandLineRunner initData(RolRepository rolRepository) {
    return args -> {
      // Inicializar roles si no existen
      if (rolRepository.findByNombre(Rol.NombreRol.ROLE_ADMIN).isEmpty()) {
        Rol adminRole = new Rol();
        adminRole.setNombre(Rol.NombreRol.ROLE_ADMIN);
        rolRepository.save(adminRole);
      }

      if (rolRepository.findByNombre(Rol.NombreRol.ROLE_INVENTARIO).isEmpty()) {
        Rol inventarioRole = new Rol();
        inventarioRole.setNombre(Rol.NombreRol.ROLE_INVENTARIO);
        rolRepository.save(inventarioRole);
      }

      if (rolRepository.findByNombre(Rol.NombreRol.ROLE_VENDEDOR).isEmpty()) {
        Rol vendedorRole = new Rol();
        vendedorRole.setNombre(Rol.NombreRol.ROLE_VENDEDOR);
        rolRepository.save(vendedorRole);
      }
    };
  }

}
