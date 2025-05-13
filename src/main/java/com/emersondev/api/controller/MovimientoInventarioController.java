package com.emersondev.api.controller;

import com.emersondev.api.request.MovimientoInventarioRequest;
import com.emersondev.api.response.MovimientoInventarioResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.domain.entity.MovimientoInventario;
import com.emersondev.service.interfaces.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/inventario/movimientos")
@RequiredArgsConstructor
public class MovimientoInventarioController {

  private final MovimientoInventarioService movimientoService;

  @PostMapping("/registrar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<MovimientoInventarioResponse> registrarMovimiento(
          @Valid @RequestBody MovimientoInventarioRequest movimientoRequest) {
    MovimientoInventarioResponse movimiento = movimientoService.registrarMovimiento(movimientoRequest);
    return new ResponseEntity<>(movimiento, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<MovimientoInventarioResponse> obtenerMovimientoPorId(@PathVariable Long id) {
    MovimientoInventarioResponse movimiento = movimientoService.obtenerMovimientoPorId(id);
    return ResponseEntity.ok(movimiento);
  }

  @GetMapping("/inventario/{inventarioId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<PagedResponse<MovimientoInventarioResponse>> obtenerMovimientosPorInventario(
          @PathVariable Long inventarioId,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size) {
    PagedResponse<MovimientoInventarioResponse> movimientos =
            movimientoService.obtenerMovimientosPorInventario(inventarioId, page, size);
    return ResponseEntity.ok(movimientos);
  }

  @GetMapping("/buscar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENTAS')")
  public ResponseEntity<PagedResponse<MovimientoInventarioResponse>> buscarMovimientos(
          @RequestParam(required = false) Long inventarioId,
          @RequestParam(required = false) Long productoId,
          @RequestParam(required = false) Long colorId,
          @RequestParam(required = false) Long tallaId,
          @RequestParam(required = false) MovimientoInventario.TipoMovimiento tipo,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "fechaMovimiento") String sortBy,
          @RequestParam(defaultValue = "desc") String sortDir) {

    PagedResponse<MovimientoInventarioResponse> movimientos = movimientoService.buscarMovimientos(
            inventarioId, productoId, colorId, tallaId,
            tipo, fechaInicio, fechaFin, page, size, sortBy, sortDir);

    return ResponseEntity.ok(movimientos);
  }

  // Endpoint adicional para ver tipos de movimiento disponibles (útil para el frontend)
  @GetMapping("/tipos")
  public ResponseEntity<MovimientoInventario.TipoMovimiento[]> obtenerTiposMovimiento() {
    return ResponseEntity.ok(MovimientoInventario.TipoMovimiento.values());
  }
}