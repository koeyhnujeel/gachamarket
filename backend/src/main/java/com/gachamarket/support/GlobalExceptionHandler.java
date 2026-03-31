package com.gachamarket.support;

import com.gachamarket.shared.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.errorCode().name(),
                ex.errorCode().message(),
                ex.detail()
        );
        return ResponseEntity
                .status(ex.httpStatus())
                .body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다.",
                null
        );
        return ResponseEntity
                .status(500)
                .body(ApiResponse.error(error));
    }
}
