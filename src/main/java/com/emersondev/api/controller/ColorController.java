package com.emersondev.api.controller;

import com.emersondev.api.request.ColorRequest;
import com.emersondev.api.response.ColorResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.api.response.PagedResponse;
import com.emersondev.service.interfaces.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/color")
@RequiredArgsConstructor
public class ColorController {

  private final ColorService colorService;

  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ColorResponse> crearColor(@RequestParam Long productoId, @Valid @RequestBody ColorRequest colorRequest) {
    ColorResponse nuevoColor = colorService.crearColor(productoId, colorRequest);
    return new ResponseEntity<>(nuevoColor, HttpStatus.CREATED);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<PagedResponse<ColorResponse>> obtenerColores(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "nombre") String sortBy,
          @RequestParam(defaultValue = "asc") String sortDir) {
    PagedResponse<ColorResponse> colores = colorService.obtenerColores(page, size, sortBy, sortDir);
    return ResponseEntity.ok(colores);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<ColorResponse> obtenerColorPorId(@PathVariable Long id) {
    ColorResponse color = colorService.obtenerColorPorId(id);
    return new ResponseEntity<>(color, HttpStatus.OK);
  }

  @GetMapping("/producto/{productoId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<List<ColorResponse>> obtenerColoresPorProducto(@PathVariable Long productoId) {
    List<ColorResponse> colores = colorService.obtenerColoresPorProducto(productoId);
    return new ResponseEntity<>(colores, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ColorResponse> actualizarColor(@PathVariable Long id, @Valid @RequestBody ColorRequest colorRequest) {
    ColorResponse colorActualizado = colorService.actualizarColor(id, colorRequest);
    return new ResponseEntity<>(colorActualizado, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarColor(@PathVariable Long id) {
    colorService.eliminarColor(id);
    return ResponseEntity.ok(new MensajeResponse("Color eliminado correctamente"));
  }


}
