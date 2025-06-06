package com.emersondev.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);


  @Override
  public void addCorsMappings(CorsRegistry registry) {
    logger.info("Configurando CORS mapping para la aplicación");

    //Auth
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:4200") // Ajusta al origen de tu frontend
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    logger.info("Configuración de CORS completada");
  }

}