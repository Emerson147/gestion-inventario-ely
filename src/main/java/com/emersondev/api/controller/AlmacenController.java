package com.emersondev.api.controller;

import com.emersondev.api.request.AlmacenRequest;
import com.emersondev.api.response.AlmacenResponse;
import com.emersondev.api.response.InventarioResponse;
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

  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<AlmacenResponse> crearAlmacen(@Valid @RequestBody AlmacenRequest almacenRequest) {
    AlmacenResponse nuevoAlmacen = almacenService.crearAlmacen(almacenRequest);
    return new ResponseEntity<>(nuevoAlmacen, HttpStatus.CREATED);
  }

  @GetMapping
  public ResponseEntity<List<AlmacenResponse>> obtenerTodosLosAlmacenes() {
    List<AlmacenResponse> almacenes = almacenService.obtenerTodosLosAlmacenes();
    return ResponseEntity.ok(almacenes);
  }

  @GetMapping("/{id}")
  public ResponseEntity<AlmacenResponse> obtenerAlmacenPorId(@PathVariable Long id) {
    AlmacenResponse almacen = almacenService.obtenerAlmacenPorId(id);
    return ResponseEntity.ok(almacen);
  }

  @GetMapping("/{id}/inventario")
  public ResponseEntity<List<InventarioResponse>> obtenerInventarioPorAlmacen(@PathVariable Long id) {
    List<InventarioResponse> inventario = almacenService.obtenerInventarioPorAlmacen(id);
    return ResponseEntity.ok(inventario);
  }

  @PutMapping("/actualizar/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<AlmacenResponse> actualizarAlmacen(
          @PathVariable Long id,
          @Valid @RequestBody AlmacenRequest almacenRequest) {
    AlmacenResponse almacenActualizado = almacenService.actualizarAlmacen(id, almacenRequest);
    return ResponseEntity.ok(almacenActualizado);
  }

  @DeleteMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> eliminarAlmacen(@PathVariable Long id) {
    almacenService.eliminarAlmacen(id);
    return ResponseEntity.noContent().build();
  }
}
