package com.emersondev.api.controller;

import com.emersondev.api.request.ClienteRequest;
import com.emersondev.api.response.ClienteResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

  private final ClienteService clienteService;

  /**
   * Crea un nuevo cliente
   */
  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest clienteRequest) {
    ClienteResponse nuevoCliente = clienteService.crearCliente(clienteRequest);
    return new ResponseEntity<>(nuevoCliente, HttpStatus.CREATED);
  }

  /**
   * Obtiene todos los clientes
   */
  @GetMapping
  public ResponseEntity<List<ClienteResponse>> obtenerTodosLosClientes() {
    List<ClienteResponse> clientes = clienteService.obtenerTodosLosClientes();
    return ResponseEntity.ok(clientes);
  }

  /**
   * Obtiene solo los clientes activos
   */
  @GetMapping("/activos")
  public ResponseEntity<List<ClienteResponse>> obtenerClientesActivos() {
    List<ClienteResponse> clientes = clienteService.obtenerClientesActivos();
    return ResponseEntity.ok(clientes);
  }

  /**
   * Obtiene un cliente por su ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Long id) {
    ClienteResponse cliente = clienteService.obtenerClientePorId(id);
    return ResponseEntity.ok(cliente);
  }

  /**
   * Busca clientes por término (dni, ruc, nombres o apellidos)
   */
  @GetMapping("/buscar")
  public ResponseEntity<List<ClienteResponse>> buscarClientes(@RequestParam String termino) {
    List<ClienteResponse> clientes = clienteService.buscarClientes(termino);
    return ResponseEntity.ok(clientes);
  }

  /**
   * Obtiene un cliente por su DNI
   */
  @GetMapping("/dni/{dni}")
  public ResponseEntity<ClienteResponse> obtenerClientePorDni(@PathVariable String dni) {
    ClienteResponse cliente = clienteService.obtenerClientePorDni(dni);
    return ResponseEntity.ok(cliente);
  }

  /**
   * Obtiene un cliente por su RUC
   */
  @GetMapping("/ruc/{ruc}")
  public ResponseEntity<ClienteResponse> obtenerClientePorRuc(@PathVariable String ruc) {
    ClienteResponse cliente = clienteService.obtenerClientePorRuc(ruc);
    return ResponseEntity.ok(cliente);
  }

  /**
   * Actualiza un cliente existente
   */
  @PutMapping("/actualizar/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<ClienteResponse> actualizarCliente(@PathVariable Long id,
                                                           @Valid @RequestBody ClienteRequest clienteRequest) {
    ClienteResponse clienteActualizado = clienteService.actualizarCliente(id, clienteRequest);
    return ResponseEntity.ok(clienteActualizado);
  }

  /**
   * Desactiva un cliente
   */
  @PutMapping("/{id}/desactivar")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> desactivarCliente(@PathVariable Long id) {
    clienteService.desactivarCliente(id);
    return ResponseEntity.ok(new MensajeResponse("Cliente desactivado correctamente"));
  }

  /**
   * Reactiva un cliente
   */
  @PutMapping("/{id}/reactivar")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> reactivarCliente(@PathVariable Long id) {
    clienteService.reactivarCliente(id);
    return ResponseEntity.ok(new MensajeResponse("Cliente reactivado correctamente"));
  }

  /**
   * Elimina un cliente
   */
  @DeleteMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarCliente(@PathVariable Long id) {
    clienteService.eliminarCliente(id);
    return ResponseEntity.ok(new MensajeResponse("Cliente eliminado correctamente"));
  }

  /**
   * Verifica si existe un cliente con el DNI especificado
   */
  @GetMapping("/verificar/dni")
  public ResponseEntity<Boolean> verificarDni(@RequestParam String dni) {
    boolean existe = clienteService.existePorDni(dni);
    return ResponseEntity.ok(existe);
  }

  /**
   * Verifica si existe un cliente con el RUC especificado
   */
  @GetMapping("/verificar/ruc")
  public ResponseEntity<Boolean> verificarRuc(@RequestParam String ruc) {
    boolean existe = clienteService.existePorRuc(ruc);
    return ResponseEntity.ok(existe);
  }

  /**
   * Verifica si existe un cliente con el email especificado
   */
  @GetMapping("/verificar/email")
  public ResponseEntity<Boolean> verificarEmail(@RequestParam String email) {
    boolean existe = clienteService.existePorEmail(email);
    return ResponseEntity.ok(existe);
  }

  /**
   * Verifica si un cliente tiene ventas asociadas
   */
  @GetMapping("/{id}/tiene-ventas")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Boolean> tieneVentasAsociadas(@PathVariable Long id) {
    boolean tieneVentas = clienteService.tieneVentasAsociadas(id);
    return ResponseEntity.ok(tieneVentas);
  }
}
