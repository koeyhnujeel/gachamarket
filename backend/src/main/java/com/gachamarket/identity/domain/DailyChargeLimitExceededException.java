package com.gachamarket.identity.domain;

import com.gachamarket.shared.BusinessException;
import com.gachamarket.shared.ErrorCode;

public class DailyChargeLimitExceededException extends BusinessException {

    public DailyChargeLimitExceededException() {
        super(IdentityErrorCode.DAILY_CHARGE_LIMIT_EXCEEDED);
    }
}
