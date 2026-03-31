package com.gachamarket.shared;

public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.detail = null;
    }

    protected BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.message());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    protected BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
        this.detail = null;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String detail() {
        return detail;
    }

    public int httpStatus() {
        return errorCode.httpStatus();
    }
}
