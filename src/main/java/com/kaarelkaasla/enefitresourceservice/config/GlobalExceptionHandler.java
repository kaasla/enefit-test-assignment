package com.kaarelkaasla.enefitresourceservice.config;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kaarelkaasla.enefitresourceservice.dtos.ErrorResponse;
import com.kaarelkaasla.enefitresourceservice.dtos.FieldError;
import com.kaarelkaasla.enefitresourceservice.exceptions.OptimisticLockingException;
import com.kaarelkaasla.enefitresourceservice.exceptions.ResourceNotFoundException;
import com.kaarelkaasla.enefitresourceservice.services.TimeProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling that maps errors to HTTP responses.
 * Translates common exceptions (404, 400, 409, 415, 405, 500),
 * aggregates field/global validation errors, and timestamps responses via TimeProvider.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private final TimeProvider timeProvider;

  public GlobalExceptionHandler(TimeProvider timeProvider) {
    this.timeProvider = timeProvider;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {

    log.warn("Resource not found: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler({
    OptimisticLockingException.class,
    org.springframework.dao.OptimisticLockingFailureException.class
  })
  public ResponseEntity<ErrorResponse> handleOptimisticLockingException(
      Exception ex, HttpServletRequest request) {

    log.warn("Optimistic locking conflict: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    log.warn("Validation error: {}", ex.getMessage());

    List<FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldError(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()))
            .toList();

    List<FieldError> globalErrors =
        ex.getBindingResult().getGlobalErrors().stream()
            .map(
                globalError ->
                    new FieldError(
                        "object",
                        "validation constraint violation",
                        globalError.getDefaultMessage()))
            .toList();

    List<FieldError> allErrors = new java.util.ArrayList<>(fieldErrors);
    allErrors.addAll(globalErrors);

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            "Validation failed",
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            allErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    log.warn("Invalid JSON request body: {}", ex.getMessage());

    String message = "Invalid request body";
    String rootCauseMessage = ex.getMostSpecificCause().getMessage();
    if (rootCauseMessage != null
        && rootCauseMessage.contains("not one of the values accepted for Enum")) {
      message = "Invalid enum value in request body";
    }

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    log.error("Data integrity violation: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            "Data integrity constraint violation",
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {

    log.error("Unexpected error occurred", ex);

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      org.springframework.web.HttpRequestMethodNotSupportedException ex,
      HttpServletRequest request) {

    log.warn("Method not allowed: {}", ex.getMessage());

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Method Not Allowed",
            ex.getMessage(),
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

    ErrorResponse error =
        new ErrorResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "Unsupported Media Type",
            ex.getMessage(),
            request.getRequestURI(),
            timeProvider.now().toLocalDateTime(),
            null);
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
  }
}
