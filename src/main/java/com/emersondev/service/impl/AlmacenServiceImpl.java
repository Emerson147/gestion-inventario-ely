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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    log.info("Creando nuevo almacén: {}", almacenRequest.getNombre());

    // Verificar si ya existe un almacén con el mismo nombre
    if (almacenRepository.existsByNombreIgnoreCase(almacenRequest.getNombre())) {
      log.error("Ya existe un almacén con el nombre: {}", almacenRequest.getNombre());
      throw new BusinessException("Ya existe un almacén con ese nombre");
    }

    // Mapear el objeto request a una entidad
    Almacen almacen = almacenMapper.toEntity(almacenRequest);

    // Guardar el almacén
    almacen = almacenRepository.save(almacen);
    log.info("Almacén creado exitosamente con ID: {}", almacen.getId());

    return almacenMapper.toResponse(almacen);
  }

  @Override
  @Transactional
  @Cacheable("almacenes")
  public List<AlmacenResponse> obtenerTodosLosAlmacenes() {
    log.debug("Obteniendo todos los almacenes");

    List<Almacen> almacenes = almacenRepository.findAll();

    return almacenes.stream()
            .map(almacenMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "almacenes", key = "'id-' + #id")
  public AlmacenResponse obtenerAlmacenPorId(Long id) {
    log.debug("Obteniendo almacen por ID: {}", id);

    Almacen almacen = almacenRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Almacén no encontrado con ID: {}", id);
              return new AlmacenNotFoundException(id);
            });

    return almacenMapper.toResponse(almacen);
  }

  @Override
  @Transactional
  @Cacheable(value = "almacenes", key = "'nombre-' + #nombre")
  public AlmacenResponse obtenerAlmacenPorNombre(String nombre) {
    log.debug("Obteniendo almacén con nombre: {}", nombre);

    Almacen almacen = almacenRepository.findByNombreIgnoreCase(nombre)
            .orElseThrow(() -> {
              log.error("Almacén no encontrado con nombre: {}", nombre);
              return new AlmacenNotFoundException(nombre);
            });

    return almacenMapper.toResponse(almacen);
  }

  @Override
  @Transactional
  @Cacheable(value = "inventario", key = "'almacen-' + #id")
  public List<InventarioResponse> obtenerInventarioPorAlmacen(Long id) {
    log.debug("Obteniendo inventario para almacén con ID: {}", id);

    // Verificar si el almacén existe
    if (!almacenRepository.existsById(id)) {
      log.error("Almacén no encontrado con ID: {}", id);
      throw new AlmacenNotFoundException(id);
    }

    // Obtener el inventario asociado al almacén
    List<Inventario> inventarios = inventarioRepository.findByAlmacenId(id);

    return inventarios.stream()
            .map(inventarioMapper::toResponse)
            .collect(Collectors.toList());

  }

  @Override
  @Transactional
  @CacheEvict(value = {"almacenes", "inventario"}, allEntries = true)
  public AlmacenResponse actualizarAlmacen(Long id, AlmacenRequest almacenRequest) {
    log.info("Actualizando almacén con ID: {}", id);

    // Verificar si el almacén existe
    Almacen almacen = almacenRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Almacén no encontrado con ID: {}", id);
              return new AlmacenNotFoundException(id);
            });

    // Verificar si el nuevo nombre ya está siendo usado por otro almacén
    if (!almacen.getNombre().equalsIgnoreCase(almacenRequest.getNombre()) &&
            almacenRepository.existsByNombreIgnoreCase(almacenRequest.getNombre())) {
      log.error("Ya existe un almacén con el nombre: {}", almacenRequest.getNombre());
      throw new BusinessException("Ya existe un almacén con ese nombre");
    }

    // Actualizar los datos del almacén
    almacen.setNombre(almacenRequest.getNombre());
    almacen.setUbicacion(almacenRequest.getUbicacion());
    almacen.setDescripcion(almacenRequest.getDescripcion());

    // Guardar los cambios
    almacen = almacenRepository.save(almacen);
    log.info("Almacén actualizado exitosamente con ID: {}", almacen.getId());

    return almacenMapper.toResponse(almacen);

  }

  @Override
  @Transactional
  @CacheEvict(value = {"almacenes", "inventario"}, allEntries = true)
  public void eliminarAlmacen(Long id) {
    log.info("Eliminando almacén con ID: {}", id);

    // Verificar si el almacén existe
    if (!almacenRepository.existsById(id)) {
      log.error("Almacén no encontrado con ID: {}", id);
      throw new AlmacenNotFoundException(id);
    }

    // Verificar si el almacén tiene inventario asociado
    if (inventarioRepository.existsByAlmacenId(id)) {
      log.error("No se puede eliminar el almacén con ID: {} porque tiene inventario asociado", id);
      throw new BusinessException("No se puede eliminar el almacén porque tiene inventario asociado");
    }

    // Eliminar el almacén
    almacenRepository.deleteById(id);
    log.info("Almacén eliminado exitosamente con ID: {}", id);
  }

  @Override
  @Transactional
  public boolean existePorNombre(String nombre) {
    return almacenRepository.existsByNombreIgnoreCase(nombre);
  }

  @Override
  @Transactional
  public boolean tieneInventarioAsociado(Long id) {
    return inventarioRepository.existsByAlmacenId(id);
  }
}
