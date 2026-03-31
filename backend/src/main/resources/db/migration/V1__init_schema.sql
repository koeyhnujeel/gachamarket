-- 회원
CREATE TABLE member (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    points BIGINT NOT NULL DEFAULT 0,
    last_free_charge_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 마켓
CREATE TABLE market (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sport_key VARCHAR(50) NOT NULL,
    home_team VARCHAR(100) NOT NULL,
    away_team VARCHAR(100) NOT NULL,
    commence_time TIMESTAMP NOT NULL,
    home_odds DECIMAL(6,3) NOT NULL,
    draw_odds DECIMAL(6,3),
    away_odds DECIMAL(6,3) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    result VARCHAR(10),
    betting_type VARCHAR(15) NOT NULL DEFAULT 'FIXED_ODDS',
    opened_at TIMESTAMP,
    closed_at TIMESTAMP,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 배팅
CREATE TABLE bet (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    market_id BIGINT NOT NULL REFERENCES market(id),
    member_id BIGINT NOT NULL REFERENCES member(id),
    outcome_type VARCHAR(10) NOT NULL,
    stake BIGINT NOT NULL,
    odds_at_placement DECIMAL(6,3) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    payout BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 댓글
CREATE TABLE comment (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    market_id BIGINT NOT NULL REFERENCES market(id),
    member_id BIGINT NOT NULL REFERENCES member(id),
    parent_id BIGINT REFERENCES comment(id),
    content VARCHAR(500) NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    dislike_count INT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 댓글 반응
CREATE TABLE comment_reaction (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comment(id),
    member_id BIGINT NOT NULL REFERENCES member(id),
    reaction_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_comment_reaction UNIQUE (comment_id, member_id)
);

-- 포인트 이력
CREATE TABLE point_transaction (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    reference_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Spring Modulith 이벤트 발행 로그
CREATE TABLE event_publication (
    id UUID NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    completion_attempts INTEGER NOT NULL,
    event_type VARCHAR(255),
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    listener_id VARCHAR(255),
    publication_date TIMESTAMP WITH TIME ZONE,
    serialized_event VARCHAR(255),
    status VARCHAR(255),
    CONSTRAINT event_publication_pkey PRIMARY KEY (id),
    CONSTRAINT event_publication_status_check CHECK (status IN ('PUBLISHED', 'PROCESSING', 'COMPLETED', 'FAILED', 'RESUBMITTED'))
);

-- 인덱스
CREATE INDEX idx_market_status ON market(status);
CREATE INDEX idx_market_commence ON market(commence_time);
CREATE INDEX idx_market_sport_status ON market(sport_key, status);
CREATE INDEX idx_bet_member ON bet(member_id);
CREATE INDEX idx_bet_market ON bet(market_id);
CREATE INDEX idx_bet_market_status ON bet(market_id, status);
CREATE INDEX idx_comment_market_created ON comment(market_id, created_at);
CREATE INDEX idx_comment_parent ON comment(parent_id);
CREATE INDEX idx_point_tx_member_created ON point_transaction(member_id, created_at);
