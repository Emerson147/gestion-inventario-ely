package com.emersondev.api.controller;

import com.emersondev.api.response.FileResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
  private final FileStorageService fileStorageService;

  /**
   * Sube un archivo al servidor
   * @param file archivo a subir
   * @return información del archivo subido
   */
  @PostMapping("/upload")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
    String fileName = fileStorageService.storeFile(file);
    String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/files/")
            .path(fileName)
            .toUriString();

    FileResponse response = new FileResponse(
            fileName,
            fileDownloadUri,
            file.getContentType(),
            file.getSize()
    );

    return ResponseEntity.ok(response);
  }

  /**
   * Sube múltiples archivos al servidor
   * @param files archivos a subir
   * @return información de los archivos subidos
   */
  @PostMapping("/upload-multiple")
  @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTARIO')")
  public ResponseEntity<List<FileResponse>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
    List<FileResponse> responses = Arrays.stream(files)
            .map(file -> {
              String fileName = fileStorageService.storeFile(file);
              String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                      .path("/api/files/")
                      .path(fileName)
                      .toUriString();

              return new FileResponse(
                      fileName,
                      fileDownloadUri,
                      file.getContentType(),
                      file.getSize()
              );
            })
            .collect(Collectors.toList());

    return ResponseEntity.ok(responses);
  }

  /**
   * Descarga un archivo del servidor
   * @param fileName nombre del archivo
   * @return el archivo como recurso
   */
  @GetMapping("/{fileName:.+}")
  public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
    Resource resource = fileStorageService.loadFileAsResource(fileName);

    String contentType = "application/octet-stream";

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
  }

  /**
   * Elimina un archivo del servidor
   * @param fileName nombre del archivo a eliminar
   * @return mensaje de confirmación
   */
  @DeleteMapping("/{fileName:.+}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MensajeResponse> deleteFile(@PathVariable String fileName) {
    boolean deleted = fileStorageService.deleteFile(fileName);

    if (deleted) {
      return ResponseEntity.ok(new MensajeResponse("Archivo eliminado correctamente"));
    } else {
      return ResponseEntity.badRequest().body(new MensajeResponse("No se pudo eliminar el archivo"));
    }
  }

  @PostMapping("/validate-image")
  public ResponseEntity<String> validateImageFile(@RequestParam("file") MultipartFile file) {
    fileStorageService.validateImageFile(file);
    return ResponseEntity.ok("La imagen es válida");
  }
}
