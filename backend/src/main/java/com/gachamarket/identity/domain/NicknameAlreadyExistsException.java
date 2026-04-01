package com.gachamarket.identity.domain;

import com.gachamarket.shared.BusinessException;
import com.gachamarket.shared.ErrorCode;

public class NicknameAlreadyExistsException extends BusinessException {

    public NicknameAlreadyExistsException() {
        super(IdentityErrorCode.NICKNAME_ALREADY_EXISTS);
    }
}
