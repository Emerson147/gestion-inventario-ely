package com.emersondev.api.controller;

import com.emersondev.api.request.InventarioRequest;
import com.emersondev.api.request.TransferenciaInventarioRequest;
import com.emersondev.api.response.InventarioResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventarios")
@RequiredArgsConstructor
public class InventarioController {

  private final InventarioService inventarioService;

  /**
   * Agregar un nuevo registro de inventario
   * @param inventarioRequest datos del nuevo inventario
   * @return el inventario creado
   */
  @PostMapping("/crear")
  @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
  public ResponseEntity<InventarioResponse> agregarInventario(@Valid @RequestBody InventarioRequest inventarioRequest) {
    InventarioResponse nuevoInventario = inventarioService.agregarInventario(inventarioRequest);
    return new ResponseEntity<>(nuevoInventario, HttpStatus.CREATED);
  }

  /**
   * Obtener todo el inventario
   * @return lista de inventarios
   */
  @GetMapping
  public ResponseEntity<List<InventarioResponse>> obtenerTodoElInventario() {
    List<InventarioResponse> inventario = inventarioService.obtenerTodoElInventario();
    return ResponseEntity.ok(inventario);
  }

  /**
   * Obtener inventario por ID
   * @param id ID del inventario
   * @return el inventario encontrado
   */
  @GetMapping("/{id}")
  public ResponseEntity<InventarioResponse> obtenerInventarioPorId(@PathVariable Long id) {
    InventarioResponse inventario = inventarioService.obtenerInventarioPorId(id);
    return ResponseEntity.ok(inventario);
  }

  /**
   * Obtener inventario por número de serie
   * @param serie número de serie
   * @return el inventario encontrado
   */
  @GetMapping("/serie/{serie}")
  public ResponseEntity<InventarioResponse> obtenerInventarioPorSerie(@PathVariable String serie) {
    InventarioResponse inventario = inventarioService.obtenerInventarioPorSerie(serie);
    return ResponseEntity.ok(inventario);
  }

  /**
   * Obtener inventario por producto
   * @param productoId ID del producto
   * @return lista de inventarios del producto
   */
  @GetMapping("/producto/{productoId}")
  public ResponseEntity<List<InventarioResponse>> obtenerInventarioPorProducto(@PathVariable Long productoId) {
    List<InventarioResponse> inventario = inventarioService.obtenerInventarioPorProducto(productoId);
    return ResponseEntity.ok(inventario);
  }

  /**
   * Transferir inventario entre almacenes
   * @param transferenciaRequest datos de la transferencia
   * @return mensaje de confirmación
   */
  @PostMapping("/transferir")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<MensajeResponse> transferirInventario(
          @Valid @RequestBody TransferenciaInventarioRequest transferenciaRequest) {
    inventarioService.transferirInventario(
            transferenciaRequest.getInventarioId(),
            transferenciaRequest.getAlmacenDestinoId(),
            transferenciaRequest.getCantidad()
    );
    return ResponseEntity.ok(new MensajeResponse("Transferencia de inventario realizada con éxito"));
  }

  /**
   * Actualizar un registro de inventario
   * @param id ID del inventario
   * @param inventarioRequest datos actualizados
   * @return el inventario actualizado
   */
  @PutMapping("/actualizar/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<InventarioResponse> actualizarInventario(
          @PathVariable Long id,
          @Valid @RequestBody InventarioRequest inventarioRequest) {
    InventarioResponse inventarioActualizado = inventarioService.actualizarInventario(id, inventarioRequest);
    return ResponseEntity.ok(inventarioActualizado);
  }

  /**
   * Eliminar un registro de inventario
   * @param id ID del inventario
   * @return respuesta vacía con código de estado
   */
  @DeleteMapping("/eliminar/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> eliminarInventario(@PathVariable Long id) {
    inventarioService.eliminarInventario(id);
    return ResponseEntity.ok(new MensajeResponse("Inventario eliminado con éxito"));
  }

  /**
   * Obtener inventario con stock bajo el umbral especificado
   * @param umbral cantidad máxima para considerar como stock bajo
   * @return lista de inventarios con stock bajo
   */
  @GetMapping("/stock-bajo")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<List<InventarioResponse>> obtenerInventarioBajoStock(
          @RequestParam(defaultValue = "5") Integer umbral) {
    List<InventarioResponse> inventario = inventarioService.obtenerInventarioConStockBajo(umbral);
    return ResponseEntity.ok(inventario);
  }

  /**
   * Obtener el stock total disponible de un producto en todos los almacenes
   * @param productoId ID del producto
   * @return cantidad total disponible
   */
  @GetMapping("/producto/{productoId}/stock-total")
  public ResponseEntity<Integer> obtenerStockTotalProducto(@PathVariable Long productoId) {
    Integer stockTotal = inventarioService.obtenerStockTotalProducto(productoId);
    return ResponseEntity.ok(stockTotal);
  }

  /**
   * Obtener el stock disponible para una variante específica (producto+color+talla)
   * @param productoId ID del producto
   * @param colorId ID del color
   * @param tallaId ID de la talla
   * @return cantidad disponible
   */
  @GetMapping("/stock")
  public ResponseEntity<Integer> obtenerStockPorVariante(
          @RequestParam Long productoId,
          @RequestParam Long colorId,
          @RequestParam Long tallaId) {
    Integer stock = inventarioService.obtenerStockPorVariante(productoId, colorId, tallaId);
    return ResponseEntity.ok(stock);
  }
}
