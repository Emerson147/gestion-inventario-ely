package com.emersondev.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {
  private String uploadDir;

  // Por defecto, si no se especifica en application.properties/yml
  public FileStorageProperties() {
    this.uploadDir = "uploads";
  }
}
