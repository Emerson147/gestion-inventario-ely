package com.emersondev.domain.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class ListStringConverter implements AttributeConverter<List<?>, String> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<?> list) {
    if (list == null || list.isEmpty()) {
      return "[]";
    }

    try {
      return objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      // Log error si es necesario
      return "[]";
    }
  }

  @Override
  public List<?> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new ArrayList<>();
    }

    try {
      return objectMapper.readValue(dbData, new TypeReference<List<?>>() {});
    } catch (IOException e) {
      // Log error si es necesario
      return new ArrayList<>();
    }
  }
}