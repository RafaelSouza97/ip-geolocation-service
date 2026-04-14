package com.example.geolocation.infrastructure.adapter.in.web;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.geolocation.application.domain.exception.ErrorCode;
import com.example.geolocation.application.domain.exception.GeolocationException;
import com.example.geolocation.application.domain.exception.InvalidIpAddressException;
import com.example.geolocation.application.domain.exception.InvalidPlatformException;
import com.example.geolocation.application.domain.exception.MissingPlatformHeaderException;
import com.example.geolocation.application.domain.exception.PrivateIpAddressException;
import com.example.geolocation.infrastructure.adapter.in.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler global de exceções para a API REST.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidIpAddressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidIp(InvalidIpAddressException ex) {
        log.warn("Invalid IP address: {}", ex.getIp());
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(PrivateIpAddressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePrivateIp(PrivateIpAddressException ex) {
        log.warn("Private IP address: {}", ex.getIp());
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MissingPlatformHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingPlatformHeader(MissingPlatformHeaderException ex) {
        log.warn("Missing platform header");
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidPlatformException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidPlatform(InvalidPlatformException ex) {
        log.warn("Invalid platform: {}", ex.getPlatform());
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(GeolocationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleGeolocationException(GeolocationException ex) {
        log.warn("Geolocation error: {}", ex.getMessage());
        return new ErrorResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return new ErrorResponse(ErrorCode.MISSING_PARAMETER.getCode(),
                ErrorCode.MISSING_PARAMETER.format(ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return new ErrorResponse(ErrorCode.VALIDATION_ERROR.getCode(), errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
