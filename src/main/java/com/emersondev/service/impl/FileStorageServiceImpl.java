package com.emersondev.service.impl;

import com.emersondev.config.FileStorageProperties;
import com.emersondev.domain.exception.BusinessException;
import com.emersondev.domain.exception.FileNotFoundException;
import com.emersondev.service.interfaces.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

  private final Path fileStorageLocation;

  @Autowired
  public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
    this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
            .toAbsolutePath().normalize();

    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (Exception ex) {
      throw new BusinessException("No se pudo crear el directorio para almacenar los archivos", ex);
    }
  }

  @Override
  public String storeFile(MultipartFile file) {
    log.info("Almacenando archivo: {}", file.getOriginalFilename());

    // Validar archivo
    if (file.isEmpty()) {
      log.error("No se puede almacenar un archivo vacío");
      throw new BusinessException("No se puede almacenar un archivo vacío");
    }

    //Normalizar el nombre del arvhivo
    String originalFilename = file.getOriginalFilename() != null ? StringUtils.cleanPath(file.getOriginalFilename()) : "unknown";

    //Verificar si el nombre del archivo contiene caracteres invalidos
    if (originalFilename.contains("..")) {
      log.error("El nombre del archivo contiene una secuenca de path inválidos: {}", originalFilename);
      throw new BusinessException("El nombre del archivo contiene caracteres inválidos: " + originalFilename);
    }

    //Generar un nombre unico para el arvhico
    String filename = generateUniqueFileName(originalFilename);

    try {
      //Copiar el archivo al directorio de almacenamiento
      Path targetLocation = this.fileStorageLocation.resolve(filename);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING );

      log.info("Archivo almacenado exitosamente: {}", filename );
      return filename;
    } catch (IOException e) {
      log.error("El nombre del archivo contiene una secuencia de path inválidos: {}", originalFilename);
      throw new BusinessException("No se puede almacenar el archivo " + originalFilename, e);
    }
  }


  /**
   * Genera un nombre único para el archivo combinando timestamp, UUID y la extensión original
   * @param originalFilename Nombre original del archivo
   * @return Nombre único para el archivo
   */
  private String generateUniqueFileName(String originalFilename) {
    // Extraer la extensión del archivo correctamente
    String fileExtension = "";
    int lastDotIndex = originalFilename.lastIndexOf(".");
    if (lastDotIndex > 0) {  // Verificar que existe un punto y no está al inicio
      fileExtension = originalFilename.substring(lastDotIndex + 1);  // Tomar solo la parte después del punto
    }

    // Generar un formato con fecha/hora actual y UUID para garantizar unicidad
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uuid = UUID.randomUUID().toString().substring(0, 8);

    return "file_" + timestamp + "_" + uuid + (fileExtension.isEmpty() ? "" : "." + fileExtension);
  }

  @Override
  public Resource loadFileAsResource(String fileName) {
    log.info("Cargando archivo: {}", fileName);

    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists()) {
        return resource;
      } else {
        log.error("Archivo no encontrado: {}", fileName);
        throw new FileNotFoundException("Archivo no encontrado: " + fileName);
      }
    } catch (MalformedURLException ex) {
      log.error("Error al cargar archivo: {}", fileName, ex);
      throw new FileNotFoundException("Error al cargar archivo: " + fileName, ex);
    }
  }

  @Override
  public boolean deleteFile(String fileName) {
    log.info("Eliminando archivo: {}", fileName);

    try {
      Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
      return Files.deleteIfExists(filePath);
    } catch (IOException ex) {
      log.error("Error al eliminar archivo: {}", fileName, ex);
      return false;
    }
  }

  @Override
  public String getFileExtension(MultipartFile file) {
    if (file == null || file.getOriginalFilename() == null) {
      return "";
    }
    String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
    int lastDotIndex = originalFileName.lastIndexOf(".");
    if (lastDotIndex == -1) {
      return ""; // No hay extensión
    }
    return originalFileName.substring(lastDotIndex);
  }

  @Override
  public void validateImageFile(MultipartFile file) throws BusinessException {
    log.info("Validando archivo de imagen: {}", file.getOriginalFilename());

    // Verificar si el archivo está vacío
    if (file == null || file.isEmpty()) {
      log.error("No se puede procesar un archivo vacío");
      throw new BusinessException("No se puede procesar un archivo vacío");
    }

    // Verificar si es una imagen basándose en el tipo de contenido
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      log.error("El archivo proporcionado no es una imagen válida: {}", contentType);
      throw new BusinessException("El archivo proporcionado no es una imagen válida");
    }

    // Verificar extensión del archivo
    String extension = getFileExtension(file).toLowerCase();
    if (!isValidImageExtension(extension)) {
      log.error("La extensión del archivo no es válida para una imagen: {}", extension);
      throw new BusinessException("El tipo de archivo no es válido para una imagen");
    }

    // Verificar tamaño del archivo (por ejemplo, máximo 5MB)
    long maxSize = 5 * 1024 * 1024; // 5MB
    if (file.getSize() > maxSize) {
      log.error("El tamaño del archivo excede el límite permitido: {} bytes", file.getSize());
      throw new BusinessException("El tamaño del archivo excede el límite permitido (5MB)");
    }
  }

  /**
   * Verifica si la extensión del archivo corresponde a un formato de imagen aceptado
   */
  private boolean isValidImageExtension(String extension) {
    if (extension == null || extension.isEmpty()) {
      return false;
    }

    // Lista de extensiones de imagen válidas
    String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};

    for (String validExtension : validExtensions) {
      if (extension.equalsIgnoreCase(validExtension)) {
        return true;
      }
    }

    return false;
  }
}
