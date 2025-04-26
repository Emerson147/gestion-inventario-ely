package com.emersondev.service.impl;

import com.emersondev.api.request.ClienteRequest;
import com.emersondev.api.response.ClienteResponse;
import com.emersondev.domain.entity.Clientes;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.ClienteNotFoundException;
import com.emersondev.domain.exception.DuplicateResourceException;
import com.emersondev.domain.repository.ClienteRepository;
import com.emersondev.domain.repository.VentaRepository;
import com.emersondev.mapper.ClienteMapper;
import com.emersondev.service.interfaces.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

  private final ClienteRepository clienteRepository;
  private final VentaRepository ventaRepository;
  private final ClienteMapper clienteMapper;

  @Override
  @Transactional
  @CacheEvict(value = {"clientes", "clientes-activos"}, allEntries = true)
  public ClienteResponse crearCliente(ClienteRequest clienteRequest) {
    log.info("Creando nuevo cliente: {} {}", clienteRequest.getNombres(), clienteRequest.getApellidos());

    // Validar DNI único si está presente
    if (StringUtils.hasText(clienteRequest.getDni()) && clienteRepository.existsByDni(clienteRequest.getDni())) {
      log.error("Ya existe un cliente con DNI: {}", clienteRequest.getDni());
      throw new DuplicateResourceException("cliente", "DNI", clienteRequest.getDni());
    }

    // Validar RUC único si está presente
    if (StringUtils.hasText(clienteRequest.getRuc()) && clienteRepository.existsByRuc(clienteRequest.getRuc())) {
      log.error("Ya existe un cliente con RUC: {}", clienteRequest.getRuc());
      throw new DuplicateResourceException("cliente", "RUC", clienteRequest.getRuc());
    }

    // Validar Email único si está presente
    if (StringUtils.hasText(clienteRequest.getEmail()) && clienteRepository.existsByEmail(clienteRequest.getEmail())) {
      log.error("Ya existe un cliente con Email: {}", clienteRequest.getEmail());
      throw new DuplicateResourceException("cliente", "Email", clienteRequest.getEmail());
    }

    // Convertir request a entidad
    Clientes cliente = clienteMapper.toEntity(clienteRequest);
    cliente.setEstado(true);

    // Guardar cliente
    cliente = clienteRepository.save(cliente);
    log.info("Cliente creado exitosamente con ID: {}", cliente.getId());

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes")
  public List<ClienteResponse> obtenerTodosLosClientes() {
    log.info("Obteniendo todos los clientes");

    List<Clientes> clientes = clienteRepository.findAll();

    return clientes.stream()
            .map(clienteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes-activos")
  public List<ClienteResponse> obtenerClientesActivos() {
    log.info("Obteniendo clientes activos");

    List<Clientes> clientes = clienteRepository.findByEstadoTrue();

    return clientes.stream()
            .map(clienteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes", key = "'id-' + #id")
  public ClienteResponse obtenerClientePorId(Long id) {
    log.debug("Obteniendo cliente con ID: {}", id);

    Clientes cliente = clienteRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con ID: {}", id);
              return new ClienteNotFoundException(id);
            });

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  public List<ClienteResponse> buscarClientes(String termino) {
    log.debug("Buscando clientes con término: {}", termino);

    if (!StringUtils.hasText(termino)) {
      return obtenerClientesActivos();
    }

    List<Clientes> clientes = clienteRepository.buscarPorTermino(termino);

    return clientes.stream()
            .map(clienteMapper::toResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes", key = "'dni-' + #dni")
  public ClienteResponse obtenerClientePorDni(String dni) {
    log.debug("Obteniendo cliente con DNI: {}", dni);

    Clientes cliente = clienteRepository.findByDni(dni)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con DNI: {}", dni);
              return new ClienteNotFoundException("DNI", dni);
            });

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes", key = "'ruc-' + #ruc")
  public ClienteResponse obtenerClientePorRuc(String ruc) {
    log.debug("Obteniendo cliente con RUC: {}", ruc);

    Clientes cliente = clienteRepository.findByRuc(ruc)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con RUC: {}", ruc);
              return new ClienteNotFoundException("RUC", ruc);
            });

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  @Cacheable(value = "clientes", key = "'email-' + #email")
  public ClienteResponse obtenerClientePorEmail(String email) {
    log.debug("Obteniendo cliente con Email: {}", email);

    Clientes cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con Email: {}", email);
              return new ClienteNotFoundException("Email", email);
            });

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"clientes", "clientes-activos"}, allEntries = true)
  public ClienteResponse actualizarCliente(Long id, ClienteRequest clienteRequest) {
    log.info("Actualizando cliente con ID: {}", id);

    // Verificar que el cliente exista
    Clientes cliente = clienteRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con ID: {}", id);
              return new ClienteNotFoundException(id);
            });

    // Validar DNI único
    if (StringUtils.hasText(clienteRequest.getDni()) &&
            !clienteRequest.getDni().equals(cliente.getDni()) &&
            clienteRepository.existsByDni(clienteRequest.getDni())) {
      log.error("Ya existe un cliente con DNI: {}", clienteRequest.getDni());
      throw new DuplicateResourceException("cliente", "DNI", clienteRequest.getDni());
    }

    // Validar RUC único
    if (StringUtils.hasText(clienteRequest.getRuc()) &&
            !clienteRequest.getRuc().equals(cliente.getRuc()) &&
            clienteRepository.existsByRuc(clienteRequest.getRuc())) {
      log.error("Ya existe un cliente con RUC: {}", clienteRequest.getRuc());
      throw new DuplicateResourceException("cliente", "RUC", clienteRequest.getRuc());
    }

    // Validar Email único
    if (StringUtils.hasText(clienteRequest.getEmail()) &&
            !clienteRequest.getEmail().equals(cliente.getEmail()) &&
            clienteRepository.existsByEmail(clienteRequest.getEmail())) {
      log.error("Ya existe un cliente con Email: {}", clienteRequest.getEmail());
      throw new DuplicateResourceException("cliente", "Email", clienteRequest.getEmail());
    }

    // Actualizar datos del cliente
    cliente.setNombres(clienteRequest.getNombres());
    cliente.setApellidos(clienteRequest.getApellidos());
    cliente.setDni(clienteRequest.getDni());
    cliente.setRuc(clienteRequest.getRuc());
    cliente.setTelefono(clienteRequest.getTelefono());
    cliente.setDireccion(clienteRequest.getDireccion());
    cliente.setEmail(clienteRequest.getEmail());

    // Guardar cambios
    cliente = clienteRepository.save(cliente);
    log.info("Cliente actualizado exitosamente");

    return clienteMapper.toResponse(cliente);
  }

  @Override
  @Transactional
  @CacheEvict(value = {"clientes", "clientes-activos"}, allEntries = true)
  public void desactivarCliente(Long id) {
    log.info("Desactivando cliente con ID: {}", id);

    // Verificar que el cliente exista
    Clientes cliente = clienteRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con ID: {}", id);
              return new ClienteNotFoundException(id);
            });

    // Desactivar cliente
    cliente.setEstado(false);
    clienteRepository.save(cliente);
    log.info("Cliente desactivado exitosamente");

  }

  @Override
  @Transactional
  @CacheEvict(value = {"clientes", "clientes-activos"}, allEntries = true)
  public void reactivarCliente(Long id) {
    log.info("Reactivando cliente con ID: {}", id);

    // Verificar que el cliente exista
    Clientes cliente = clienteRepository.findById(id)
            .orElseThrow(() -> {
              log.error("Cliente no encontrado con ID: {}", id);
              return new ClienteNotFoundException(id);
            });

    // Reactivar cliente
    cliente.setEstado(true);
    clienteRepository.save(cliente);
    log.info("Cliente reactivado exitosamente");
  }

  @Override
  @Transactional
  @CacheEvict(value = {"clientes", "clientes-activos"}, allEntries = true)
  public void eliminarCliente(Long id) {
    log.info("Eliminando cliente con ID: {}", id);

    // Verificar que el cliente exista
    if (!clienteRepository.existsById(id)) {
      log.error("Cliente no encontrado con ID: {}", id);
      throw new ClienteNotFoundException(id);
    }

    // Verificar si tiene ventas asociadas
    if (ventaRepository.existsByClienteId(id)) {
      log.error("No se puede eliminar el cliente con ID: {} porque tiene ventas asociadas", id);
      throw new BusinessException("No se puede eliminar el cliente porque tiene ventas asociadas. Considere desactivarlo en su lugar.");
    }

    // Eliminar cliente
    clienteRepository.deleteById(id);
    log.info("Cliente eliminado exitosamente");
  }

  @Override
  @Transactional
  public boolean existePorDni(String dni) {
    return clienteRepository.existsByDni(dni);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existePorRuc(String ruc) {
    return clienteRepository.existsByRuc(ruc);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existePorEmail(String email) {
    return clienteRepository.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean tieneVentasAsociadas(Long id) {
    return ventaRepository.existsByClienteId(id);
  }

}
