package com.gachamarket.identity.application.port.out;

import com.gachamarket.identity.domain.PointTransaction;
import com.gachamarket.support.PageResponse;

import java.util.List;

public interface PointTransactionRepository {

    PointTransaction save(PointTransaction transaction);

    PageResponse<PointTransaction> findByMemberIdOrderByCreatedAtDesc(Long memberId, int page, int size);
}
