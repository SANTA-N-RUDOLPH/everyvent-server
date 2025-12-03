package kr.santanrudolph.everyvent.global.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EveryventException.class)
  public ResponseEntity<ErrorResponse> handleEveryventException(EveryventException e
  ) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn(e.getMessage(), e.getDetail());

    ErrorResponse response = e.getDetail() != null
        ? ErrorResponse.of(errorCode, e.getDetail())
        : ErrorResponse.of(errorCode);

    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      HttpServletRequest request,
      MethodArgumentNotValidException e
  ) {
    String detail = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    log.warn(request.getMethod(), request.getRequestURI(),
        getRequestBody(request), detail);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, detail));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
    log.debug("No static resource found: {}", e.getMessage());
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception e) {
    log.error(request.getMethod(), request.getRequestURI(), getRequestBody(request),
        e.getMessage(), e);

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
