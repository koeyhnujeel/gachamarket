package com.gachamarket.identity.domain;

import com.gachamarket.shared.ErrorCode;

public enum IdentityErrorCode implements ErrorCode {

    NICKNAME_ALREADY_EXISTS("이미 존재하는 닉네임입니다.", 409),
    INSUFFICIENT_POINTS("포인트가 부족합니다.", 400),
    FREE_CHARGE_NOT_ALLOWED("오늘은 이미 무료 충전을 받았습니다.", 400),
    DAILY_CHARGE_LIMIT_EXCEEDED("일일 충전 한도를 초과했습니다.", 400),
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다.", 404),
    ;

    private final String message;
    private final int httpStatus;

    IdentityErrorCode(String message, int httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}
