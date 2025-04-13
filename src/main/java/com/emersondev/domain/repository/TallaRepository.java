package com.emersondev.domain.repository;

import com.emersondev.domain.entity.Talla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TallaRepository extends JpaRepository<Talla, Long> {
  List<Talla> findByColorId(Long colorId);

  Optional<Talla> findByNumeroAndColorId(String numero, Long colorId);
}
