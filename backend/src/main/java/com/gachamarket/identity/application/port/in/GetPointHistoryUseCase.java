package com.gachamarket.identity.application.port.in;

import com.gachamarket.identity.application.dto.result.PointTransactionResult;
import com.gachamarket.support.PageResponse;

public interface GetPointHistoryUseCase {

    PageResponse<PointTransactionResult> getHistory(Long memberId, int page, int size);
}
