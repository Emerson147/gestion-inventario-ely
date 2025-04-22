package com.emersondev.domain.repository;

import com.emersondev.domain.entity.RefreshToken;
import com.emersondev.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);
  void deleteByUsuario(Usuario usuario);
  void deleteByUsuario_Username(String username);
}
