package com.emersondev.api.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReporteVentasResponse {
  private LocalDateTime fechaInicio;
  private LocalDateTime fechaFin;
  private BigDecimal totalVentas;
  private BigDecimal totalIgv;
  private Integer cantidadVentas;
  private List<ClienteVentas> topClientes = new ArrayList<>();
  private List<ProductoVendido> topProductos = new ArrayList<>();

  @Data
  public static class ClienteVentas {
    private Long clienteId;
    private String nombreCliente;
    private BigDecimal total;
  }

  @Data
  public static class ProductoVendido {
    private Long productoId;
    private String nombreProducto;
    private Integer cantidadVendida;
  }
}
