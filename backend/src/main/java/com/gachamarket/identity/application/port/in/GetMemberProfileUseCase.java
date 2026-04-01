package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.result.MemberProfileResult;

public interface GetMemberProfileUseCase {

    MemberProfileResult getProfile(Long memberId);
}
