package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;

public interface ChargeFreePointsUseCase {

    MemberProfileResult chargeFreePoints(Long memberId);
}
