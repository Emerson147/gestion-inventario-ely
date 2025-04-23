package com.emersondev.api.controller;

import com.emersondev.api.request.TallaRequest;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.api.response.TallaResponse;
import com.emersondev.service.interfaces.TallaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colores/{colorId}/tallas")
@RequiredArgsConstructor
public class TallaController {

  private final TallaService tallaService;

  /**
   * Crea una nueva talla
   *
   * @param tallaRequest Objeto que contiene los datos de la talla a crear
   * @return ResponseEntity<TallaResponse> Objeto que contiene la respuesta de la creación de la talla
   */
  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN')")
  ResponseEntity<TallaResponse> crearTalla(@PathVariable Long colorId, @Valid @RequestBody TallaRequest tallaRequest) {
    return ResponseEntity.status(HttpStatus.CREATED).body(tallaService.crearTalla(colorId, tallaRequest));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  ResponseEntity<List<TallaResponse>> obtenerTallasPorColor(@PathVariable Long colorId) {
    return ResponseEntity.ok(tallaService.obtenerTallasPorColor(colorId));
  }

  @GetMapping("/{tallaId}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  ResponseEntity<TallaResponse> obtenerTallaPorId(@PathVariable Long colorId, @PathVariable Long tallaId) {
    return ResponseEntity.ok(tallaService.obtenerTallaPorId(colorId, tallaId));
  }

  @PutMapping("/{tallaId}/actualizar")
  @PreAuthorize("hasRole('ADMIN')")
  ResponseEntity<TallaResponse> actualizarTalla(@PathVariable Long tallaId, @Valid @RequestBody TallaRequest tallaRequest) {
    return ResponseEntity.ok(tallaService.actualizarTalla(tallaId, tallaRequest));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarTalla(@PathVariable Long id) {
    tallaService.eliminarTalla(id);
    return ResponseEntity.ok(new MensajeResponse("Talla eliminada correctamente"));
  }

}
