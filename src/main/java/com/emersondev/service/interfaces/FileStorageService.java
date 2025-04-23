package com.emersondev.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  /**
   * Almacena un archivo en el sistema y devuelve el nombre del archivo guardado
   * @param file Archivo a almacenar
   * @return Nombre del archivo almacenado
   */
  String storeFile(MultipartFile file);

  /**
   * Carga un archivo como recurso
   * @param fileName Nombre del archivo a cargar
   * @return El archivo como recurso
   */
  Resource loadFileAsResource(String fileName);

  /**
   * Elimina un archivo del almacenamiento
   * @param fileName Nombre del archivo a eliminar
   * @return true si se eliminó correctamente, false en caso contrario
   */
  boolean deleteFile(String fileName);

  /**
   * Obtiene la extensión de un archivo
   * @param file Archivo
   * @return Extensión del archivo
   */
  String getFileExtension(MultipartFile file);

}
