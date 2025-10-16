package kr.santanrudolph.everyvent.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String LOG_FORMAT = """
      \n\t{
          "RequestURI": "{} {}",
          "RequestBody": {},
          "ErrorMessage": "{}"
      \t}
      """;

  @ExceptionHandler(EveryventException.class)
  public ResponseEntity<ErrorResponse> handleEveryventException(
      HttpServletRequest request,
      EveryventException e
  ) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request), e.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(errorCode));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
    log.error(LOG_FORMAT, request.getMethod(), request.getRequestURI(), getRequestBody(request), e.getMessage(), e);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
  }

  private String getRequestBody(HttpServletRequest request) {
    try (BufferedReader reader = request.getReader()) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator() + "\t"));
    } catch (IOException e) {
      log.error("Failed to read request body", e);
      return "";
    }
  }
}
