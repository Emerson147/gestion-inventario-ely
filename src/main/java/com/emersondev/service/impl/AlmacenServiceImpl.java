package com.emersondev.service.impl;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.domain.entity.Almacen;
import com.emersondev.domain.entity.Inventario;
import com.emersondev.domain.exception.AlmacenNotFoundException;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.repository.AlmacenRepository;
import com.emersondev.domain.repository.InventarioRepository;
import com.emersondev.mapper.AlmacenMapper;
import com.emersondev.mapper.InventarioMapper;
import com.emersondev.service.interfaces.AlmacenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlmacenServiceImpl implements AlmacenService {

  private final AlmacenRepository almacenRepository;
  private final InventarioRepository inventarioRepository;
  private final AlmacenMapper almacenMapper;
  private final InventarioMapper inventarioMapper;

  @Override
  @Transactional
  @CacheEvict(value = "almacenes", allEntries = true)
  public AlmacenResponse crearAlmacen(AlmacenRequest almacenRequest) {
    //Verficcar si el almacen ya existe con el mismo nombre
    if (almacenRepository.findByNombre(almacenRequest.getNombre()).isPresent()) {
      throw new BusinessException("Ya existe un almacen con el nombre: " + almacenRequest.getNombre());
    }

    Almacen almacen = almacenMapper.toEntity(almacenRequest);
    almacenRepository.save(almacen);

    return almacenMapper.toResponse(almacen);
  }

  @Override
  @Transactional
  @Cacheable("almacenes")
  public List<AlmacenResponse> obtenerTodosLosAlmacenes() {
    List<Almacen> almacenes = almacenRepository.findAll();

    return almacenes.stream()
            .map(almacenMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public AlmacenResponse obtenerAlmacenPorId(Long id) {
    Almacen almacen = almacenRepository.findById(id)
            .orElseThrow(() -> new AlmacenNotFoundException(id));

    return almacenMapper.toResponse(almacen);
  }

  @Override
  @Transactional
  public List<InventarioResponse> obtenerInventarioPorAlmacen(Long almacenId) {
    // Verificar si el almacen existe
    if (!almacenRepository.existsById(almacenId)) {
      throw new AlmacenNotFoundException(almacenId);
    }

    List<Inventario> inventarios = inventarioRepository.findByAlmacenId(almacenId);

    return inventarios.stream()
            .map(inventarioMapper::toResponse)
            .collect(Collectors.toList());

  }

  @Override
  @Transactional
  @CacheEvict(value = "almacenes", allEntries = true)
  public AlmacenResponse actualizarAlmacen(Long id, AlmacenRequest almacenRequest) {
    Almacen almacen = almacenRepository.findById(id)
            .orElseThrow(() -> new AlmacenNotFoundException(id));

    // Verificar si el nuevo nombre ya existe en otro almacén
    almacenRepository.findByNombre(almacenRequest.getNombre())
            .ifPresent(existente -> {
              if (!existente.getId().equals(id)) {
                throw new BusinessException("Ya existe un almacén con el nombre: " + almacenRequest.getNombre());
              }
            });

    almacen.setNombre(almacenRequest.getNombre());
    almacen.setUbicacion(almacenRequest.getUbicacion());
    almacen.setDescripcion(almacenRequest.getDescripcion());

    almacen = almacenRepository.save(almacen);

    return almacenMapper.toResponse(almacen);

  }

  @Override
  @Transactional
  @CacheEvict(value = "almacenes", allEntries = true)
  public void eliminarAlmacen(Long id) {
    Almacen almacen = almacenRepository.findById(id)
            .orElseThrow(() -> new AlmacenNotFoundException(id));

    // Verificar que no tenga inventario asociado
    List<Inventario> inventarios = inventarioRepository.findByAlmacenId(id);
    if (!inventarios.isEmpty()) {
      throw new BusinessException(
              "No se puede eliminar el almacén porque tiene " + inventarios.size() +
                      " productos en inventario asociados");
    }

    almacenRepository.delete(almacen);
  }
}
