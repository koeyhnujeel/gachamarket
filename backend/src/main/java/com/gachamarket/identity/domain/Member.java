package com.gachamarket.identity.domain;

import java.util.UUID;

public record Member(UUID id, String email, String nickname, int currentPoint) {
}
