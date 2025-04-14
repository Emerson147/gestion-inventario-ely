package com.emersondev.security;

import com.emersondev.domain.entity.Usuario;
import com.emersondev.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UsuarioRepository usuarioRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("No se encontró usuario con el nombre: " + username));

    // Verifica si el usuario está activo
    if (!usuario.isActivo()) {
      throw new UsernameNotFoundException("El usuario está desactivado: " + username);
    }

    // Convierte los roles a authorities para Spring Security
    List<GrantedAuthority> authorities = usuario.getRoles().stream()
            .map(rol -> new SimpleGrantedAuthority(rol.getNombre().name()))
            .collect(Collectors.toList());

    // Retorna un objeto UserDetails con username, password y authorities
    return User.builder()
            .username(usuario.getUsername())
            .password(usuario.getPassword())
            .authorities(authorities)
            .build();
  }
}
