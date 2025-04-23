package com.emersondev.service.impl;

import com.emersondev.api.request.TallaRequest;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.domain.entity.Color;
import com.emersondev.domain.entity.Talla;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ResourceNotFoundException;
import com.emersondev.domain.repository.ColorRepository;
import com.emersondev.domain.repository.InventarioRepository;
import com.emersondev.domain.repository.TallaRepository;
import com.emersondev.mapper.TallaMapper;
import com.emersondev.service.interfaces.TallaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TallaServiceImpl implements TallaService {

  private final TallaRepository tallaRepository;
  private final ColorRepository colorRepository;
  private final InventarioRepository inventarioRepository;
  private final TallaMapper tallaMapper;

  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public TallaResponse crearTalla(Long colorId, TallaRequest tallaRequest) {
    log.info("Creando nueva talla {} para el color ID {}", tallaRequest, colorId);

    //Verificar si el color existe
    Color color = colorRepository.findById(colorId)
            .orElseThrow(() -> {
              log.error("Color no encontrado con ID: {}", colorId);
              return new ResourceNotFoundException("Color", "id", colorId);
            });

    // Verificar si ya existe una talla con el mismo número para este color
    if (tallaRepository.existsByNumeroAndColorId(tallaRequest.getNumero(), colorId)) {
      log.error("Ya existe una talla con el número {} para el color ID {}", tallaRequest.getNumero(), colorId);
      throw new BusinessException("Ya existe una talla con el número " + tallaRequest.getNumero() + " para el color ID " + colorId);
    }

    //Crear y guardar la nueva talla
    Talla talla = tallaMapper.toEntity(tallaRequest, color);

    //Asegurarse de que se asigna la cantidad correctamente
    talla.setCantidad(tallaRequest.getCantidad() != null ? tallaRequest.getCantidad() : "0");

    // Guardar la talla en la base de datos
    talla = tallaRepository.save(talla);

    log.info("Talla creada exitosamente con ID: {}", talla.getId());
    return tallaMapper.toResponse(talla);
  }


  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'talla-color-' + #colorId + '-' + #tallaId")
  public TallaResponse obtenerTallaPorId(Long colorId, Long tallaId) {
    log.debug("Obteniendo talla por ID: {} para el color ID: {}", tallaId, colorId);

    //Verficar si el color existe
    if (!colorRepository.existsById(colorId)) {
      log.error("Color no encontrado con ID: {}", colorId);
      throw new ResourceNotFoundException("Color", "id", colorId);
    }

    // Verificar si la talla existe
    Talla talla = tallaRepository.findByIdAndColorId(tallaId, colorId)
            .orElseThrow(() -> {
              log.error("Talla con el ID: {} no encontrada para el color ID: {}", tallaId, colorId);
              return new ResourceNotFoundException("Talla", "id", tallaId);
            });

    return tallaMapper.toResponse(talla);
  }

  @Override
  @Transactional
  @Cacheable(value = "productos", key = "'tallas-color-' + #colorId")
  public List<TallaResponse> obtenerTallasPorColor(Long colorId) {
    log.debug("Obteniendo tallas por color ID: {}", colorId);

    // Verificar si el color existe
    colorRepository.findById(colorId)
            .orElseThrow(() -> {
              log.error("Color no encontrado con ID: {}", colorId);
              return new ResourceNotFoundException("Color", "id", colorId);
            });

    // Obtener las tallas asociadas al color
    List<Talla> tallas = tallaRepository.findByColorId(colorId);

    //Mapear a respuestas y devolver
    return tallas.stream()
            .map(tallaMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public TallaResponse actualizarTalla(Long id, TallaRequest tallaRequest) {
    log.info("Actualizando talla con ID: {}", id);

    //Verificar si la talla existe
    Talla talla = tallaRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Talla no encontrada con ID: {}", id);
              return new ResourceNotFoundException("Talla", "id", id);
            });

    //Verificar si ya existe otra talla con el mismo número para este color
    if (!talla.getNumero().equals(tallaRequest.getNumero()) && tallaRepository.existsByNumeroAndColorId(tallaRequest.getNumero(), talla.getColor().getId())) {
      log.error("Ya existe una talla con el número {} para el color ID {}", tallaRequest.getNumero(), talla.getColor().getId());
      throw new BusinessException("Ya existe una talla con el número " + tallaRequest.getNumero() + " para el color ID " + talla.getColor().getId());
    }

    //Actualizar datos de la talla
    talla.setNumero(tallaRequest.getNumero());

    //Agregar actualizacion de cantidad
    talla.setCantidad(tallaRequest.getCantidad() != null ? tallaRequest.getCantidad() : "0");

    talla = tallaRepository.save(talla);
    log.info("Talla actualizada exitosamente con ID: {}", talla.getId());

    return tallaMapper.toResponse(talla);
  }

  @Override
  @Transactional
  @CacheEvict(value = "productos", allEntries = true)
  public void eliminarTalla(Long id) {
    log.info("Eliminando talla con ID: {}", id);

    //Verificar si la talla existe
    if (!tallaRepository.existsById(id)) {
      log.error("Talla no encontrada con ID: {}", id);
      throw new ResourceNotFoundException("Talla", "id", id);
    }

    //Verificar si la talla está asociada a un inventario
    if (inventarioRepository.existsByTallaId(id)) {
      log.error("No se puede eliminar la talla con ID {} porque está asociada a un inventario", id);
      throw new BusinessException("No se puede eliminar la talla porque está asociada a un inventario");
    }

    //Eliminar la talla
    tallaRepository.deleteById(id);
    log.info("Talla eliminada exitosamente con ID: {}", id);
  }
}
