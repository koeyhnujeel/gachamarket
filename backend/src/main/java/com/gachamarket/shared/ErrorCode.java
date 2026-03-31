package com.gachamarket.shared;

public interface ErrorCode {

    String name();
    String message();
    int httpStatus();
}
