package com.gachamarket.identity.domain;

import com.gachamarket.shared.BusinessException;
import com.gachamarket.shared.ErrorCode;

public class FreeChargeNotAllowedException extends BusinessException {

    public FreeChargeNotAllowedException() {
        super(IdentityErrorCode.FREE_CHARGE_NOT_ALLOWED);
    }
}
