package com.gachamarket.identity.domain;

import com.gachamarket.shared.BusinessException;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException() {
        super(IdentityErrorCode.MEMBER_NOT_FOUND);
    }
}
