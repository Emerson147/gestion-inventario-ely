package com.emersondev.api.controller;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {


  private final AlmacenService almacenService;

  /**
   * Obtiene todos los almacenes
   */
  @GetMapping
  public ResponseEntity<List<AlmacenResponse>> obtenerTodosLosAlmacenes() {
    List<AlmacenResponse> almacenes = almacenService.obtenerTodosLosAlmacenes();
    return ResponseEntity.ok(almacenes);
  }

  /**
   * Obtiene un almacén por su ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<AlmacenResponse> obtenerAlmacenPorId(@PathVariable Long id) {
    AlmacenResponse almacen = almacenService.obtenerAlmacenPorId(id);
    return ResponseEntity.ok(almacen);
  }

  /**
   * Obtiene un almacén por su nombre
   */
  @GetMapping("/nombre/{nombre}")
  public ResponseEntity<AlmacenResponse> obtenerAlmacenPorNombre(@PathVariable String nombre) {
    AlmacenResponse almacen = almacenService.obtenerAlmacenPorNombre(nombre);
    return ResponseEntity.ok(almacen);
  }

  /**
   * Obtiene el inventario de un almacén
   */
  @GetMapping("/{id}/inventario")
  public ResponseEntity<List<InventarioResponse>> obtenerInventarioPorAlmacen(@PathVariable Long id) {
    List<InventarioResponse> inventario = almacenService.obtenerInventarioPorAlmacen(id);
    return ResponseEntity.ok(inventario);
  }

  /**
   * Crea un nuevo almacén
   */
  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<AlmacenResponse> crearAlmacen(@Valid @RequestBody AlmacenRequest almacenRequest) {
    AlmacenResponse nuevoAlmacen = almacenService.crearAlmacen(almacenRequest);
    return new ResponseEntity<>(nuevoAlmacen, HttpStatus.CREATED);
  }

  /**
   * Actualiza un almacén existente
   */
  @PutMapping("/actualizar/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<AlmacenResponse> actualizarAlmacen(
          @PathVariable Long id,
          @Valid @RequestBody AlmacenRequest almacenRequest) {
    AlmacenResponse almacenActualizado = almacenService.actualizarAlmacen(id, almacenRequest);
    return ResponseEntity.ok(almacenActualizado);
  }

  /**
   * Elimina un almacén
   */
  @DeleteMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarAlmacen(@PathVariable Long id) {
    almacenService.eliminarAlmacen(id);
    return ResponseEntity.ok(new MensajeResponse("Almacén eliminado correctamente"));
  }

  /**
   * Verifica si existe un almacén con el nombre especificado
   */
  @GetMapping("/validar-nombre")
  public ResponseEntity<Boolean> validarNombre(@RequestParam String nombre) {
    boolean existe = almacenService.existePorNombre(nombre);
    return ResponseEntity.ok(existe);
  }

  /**
   * Verifica si un almacén tiene inventario asociado
   */
  @GetMapping("/{id}/tiene-inventario")
  public ResponseEntity<Boolean> tieneInventarioAsociado(@PathVariable Long id) {
    boolean tieneInventario = almacenService.tieneInventarioAsociado(id);
    return ResponseEntity.ok(tieneInventario);
  }
}
