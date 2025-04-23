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
    //Extraer la extension del archivo
    String fileExtension = "";
    if (originalFilename.contains("..")) {
      fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    //Generar un formato con fecha/hora actual y UUID para garantizar unicidad
    String timestap = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uuid = UUID.randomUUID().toString().substring(0, 8);

    return "file_" + timestap + "_" + uuid + "." + fileExtension;
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
}
