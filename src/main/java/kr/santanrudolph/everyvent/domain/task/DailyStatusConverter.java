package kr.santanrudolph.everyvent.domain.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Converter
public class DailyStatusConverter implements AttributeConverter<Map<String, Boolean>, String> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Boolean> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return "{}";
    }
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new EveryventException(ErrorCode.INTERNAL_SERVER_ERROR, "Map을 JSON으로 변환하지 못했습니다.");
    }
  }

  @Override
  public Map<String, Boolean> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new HashMap<>();
    }
    try {
      return objectMapper.readValue(dbData, HashMap.class);
    } catch (IOException e) {
      throw new EveryventException(ErrorCode.INTERNAL_SERVER_ERROR, "JSON을 Map으로 변환하지 못했습니다.");
    }
  }
}
