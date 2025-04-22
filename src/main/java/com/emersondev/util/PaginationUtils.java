package com.emersondev.util;


import com.emersondev.api.response.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilidades para manejar paginación en consultas y respuestas
 */
public class PaginationUtils {

  private PaginationUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Crea un objeto PageRequest con los parámetros de paginación y ordenamiento
   * @param page número de página (0-based)
   * @param size tamaño de página
   * @param sortBy campo para ordenar
   * @param sortDir dirección de ordenamiento (asc/desc)
   * @return objeto Pageable configurado
   */
  public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

    return PageRequest.of(page, size, sort);
  }

  /**
   * Convierte un Page de Spring Data a nuestro objeto PagedResponse personalizado
   * @param page objeto Page de Spring Data
   * @param mapper función para mapear entidades a DTOs
   * @return objeto PagedResponse con el contenido mapeado
   */
  public static <T, R> PagedResponse<R> createPagedResponse(Page<T> page, Function<T, R> mapper) {
    List<R> content = page.getContent().stream()
            .map(mapper)
            .collect(Collectors.toList());

    return new PagedResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
    );
  }

  /**
   * Crea una respuesta paginada vacía
   * @param page número de página
   * @param size tamaño de página
   * @return objeto PagedResponse vacío
   */
  public static <T> PagedResponse<T> emptyPagedResponse(int page, int size) {
    return new PagedResponse<>(
            Collections.emptyList(),
            page,
            size,
            0,
            0,
            true
    );
  }

  /**
   * Valida y normaliza los parámetros de paginación
   * @param page número de página
   * @param size tamaño de página
   * @return array con los valores normalizados [page, size]
   */
  public static int[] validatePaginationParams(int page, int size) {
    // Validar página y tamaño
    if (page < 0) {
      page = 0;
    }

    if (size <= 0) {
      size = 10; // Tamaño por defecto
    }

    if (size > 100) {
      size = 100; // Tamaño máximo
    }

    return new int[]{page, size};
  }

}
