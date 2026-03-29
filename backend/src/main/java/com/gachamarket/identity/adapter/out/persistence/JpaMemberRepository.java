package com.gachamarket.identity.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<JpaMemberEntity, UUID> {

    Optional<JpaMemberEntity> findByEmail(String email);
}
