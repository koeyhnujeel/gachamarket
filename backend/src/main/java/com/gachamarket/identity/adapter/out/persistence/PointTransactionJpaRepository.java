package com.gachamarket.identity.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface PointTransactionJpaRepository extends JpaRepository<PointTransactionJpaEntity, Long> {

    Page<PointTransactionJpaEntity> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
