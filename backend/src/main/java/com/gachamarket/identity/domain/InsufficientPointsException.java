package com.gachamarket.identity.domain;

import com.gachamarket.shared.BusinessException;
import com.gachamarket.shared.ErrorCode;

public class InsufficientPointsException extends BusinessException {

    public InsufficientPointsException() {
        super(IdentityErrorCode.INSUFFICIENT_POINTS);
    }
}
