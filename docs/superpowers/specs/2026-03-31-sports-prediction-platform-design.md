# Gachamarket - 스포츠 예측 플랫폼 MVP 설계서

## 1. 프로젝트 개요

Gachamarket은 폴리마켓 스타일의 스포츠 예측 플랫폼이다. 사용자는 포인트를 배팅하여 스포츠 경기 결과를 예측하고, 맞추면 배당에 따라 포인트를 얻는다. 현금은 사용하지 않으며 순수 포인트 기반이다.

### MVP 범위

- 스포츠 예측 마켓만 지원 (커뮤니티/정치 이벤트는 추후)
- 지원 리그: EPL, La Liga, 분데스리가, 세리에 A, 리그 1, KBO, MLB, NBA
- 배팅 유형: 승무패/승패만
- Fixed Odds 방식 (The Odds API 기반)

### 핵심 특징

| 항목 | 결정 사항 |
|---|---|
| 배팅 방식 | 스포츠 = Fixed Odds, 커뮤니티/정치 = 파리뮤트 (추후) |
| 오즈 API | The Odds API (무료 500 credits/월) |
| 배당 표기 | 소수형 (Decimal) |
| 배당 확정 | 실시간 업데이트, 배팅 시점 배당 확정 |
| 회원가입 | 구글 OAuth2 (email scope만) |
| 닉네임 | 형용사+명사 조합 자동 생성, 수정 가능 |
| 포인트 | 가입 1,000P, 0P 시 하루 1회 100P 무료 충전 |
| 최소 배팅 | 10P |
| 랭킹 | MVP 없음 |
| 언어 | 한국어 + 영어 (i18n) |
| UI | 라이트+미니멀, 동등 반응형 |

---

## 2. 아키텍처

### 백엔드 - 헥사고날 / 포트 & 어댑터

기존 CLAUDE.md의 아키텍처 원칙을 따른다. 모듈별 독립 개발 방식을 채택한다.

### 모듈 구성

```
com.gachamarket/
  shared/                          # 공통 VO, 기본 클래스, 공통 예외
  support/                         # 공통 설정, 웹 예외 핸들러, 유틸리티
  security/                        # 인증/인가
    domain/
      AuthUser (VO)
        - memberId: Long
        - email: String
        - role: Role
    application/
      port/in/
        AuthenticateUseCase
      port/out/
        OAuth2UserRepository
        MemberPort
    adapter/
      in/web/
        OAuth2LoginController
        OAuth2AuthenticationSuccessHandler
      out/security/
        GoogleOAuth2UserService
        JwtTokenProvider
        SecurityConfig
  identity/                        # 회원 프로필 + 포인트 관리
    domain/
      Member (Aggregate Root)
        - id: Long
        - email: String
        - nickname: Nickname (VO)
        - role: Role (USER, ADMIN)
        - points: Points (VO)
        - lastFreeChargeDate: LocalDate
        - createdAt: Instant
        - updatedAt: Instant
      Nickname (VO)
        - value: String
        - 정적 팩토리: generate(Pair<String,String> adjectiveNoun)
        - 규칙: 2~20자, 중복 불가
      Points (VO)
        - value: long
        - canBet(amount): boolean
        - charge(amount): Points
        - deduct(amount): Points
        - isZero(): boolean
      Role (enum) - USER, ADMIN
    application/
      port/in/
        SignUpUseCase
        UpdateNicknameUseCase
        GetMemberProfileUseCase
        ChargeFreePointsUseCase
        GetPointHistoryUseCase
      port/out/
        MemberRepository
        NicknameGeneratorPort (중복 확인)
        PointTransactionRepository
      dto/command/
        SignUpCommand(email)
        UpdateNicknameCommand(memberId, nickname)
        ChargeFreePointsCommand(memberId)
      dto/query/
        GetMemberProfileQuery(memberId)
        GetPointHistoryQuery(memberId, page, size)
      dto/result/
        MemberProfileResult(id, email, nickname, points, role)
        PointTransactionResult(type, amount, referenceId, createdAt)
      service/
        MemberService
        PointService
    adapter/
      in/web/
        MemberController
      out/persistence/
        MemberJpaEntity
        PointTransactionJpaEntity
        MemberJpaRepository
        PointTransactionJpaRepository
        MemberPersistenceAdapter
  market/                          # 마켓, 배팅, 정산
    domain/
      Market (Aggregate Root)
        - id: Long
        - fixture: Fixture (VO)
        - outcomes: List<MarketOutcome>
        - status: MarketStatus
        - bettingType: BettingType
        - result: OutcomeType? (정산 후)
        - openedAt: Instant?
        - closedAt: Instant?
        - settledAt: Instant?
        - createdAt: Instant
        - updatedAt: Instant
      MarketOutcome (VO)
        - type: OutcomeType (HOME, DRAW, AWAY)
        - label: String
        - odds: BigDecimal
      Fixture (VO)
        - sportKey: String
        - homeTeam: String
        - awayTeam: String
        - commenceTime: Instant
      Bet (Entity)
        - id: Long
        - marketId: Long
        - memberId: Long
        - outcomeType: OutcomeType
        - stake: long
        - oddsAtPlacement: BigDecimal
        - status: BetStatus
        - payout: Long?
        - createdAt: Instant
        - updatedAt: Instant
      MarketStatus (enum) - PENDING, OPEN, CLOSED, SETTLED, CANCELLED
      BetStatus (enum) - PENDING, WON, LOST, REFUNDED
      OutcomeType (enum) - HOME, DRAW, AWAY
      BettingType (enum) - FIXED_ODDS, PARIMUTUEL
    application/
      port/in/
        CreateMarketUseCase (관리자)
        OpenMarketUseCase (관리자)
        CloseMarketUseCase
        CancelMarketUseCase (관리자)
        PlaceBetUseCase
        SettleMarketUseCase
        GetMarketDetailUseCase
        ListMarketsUseCase
        GetUserBetsUseCase
        GetAdminMarketsUseCase
        GetBettingStatsUseCase
      port/out/
        MarketRepository
        BetRepository
        OddsApiPort (배당 조회)
        MemberPort (포인트 차감/지급)
      dto/command/
        CreateMarketCommand(sportKey, homeTeam, awayTeam, commenceTime, homeOdds, drawOdds?, awayOdds)
        OpenMarketCommand(marketId)
        PlaceBetCommand(marketId, memberId, outcomeType, stake)
        SettleMarketCommand(marketId, result)
      dto/query/
        ListMarketsQuery(sportKey?, status?, page, size)
        GetUserBetsQuery(memberId, status?, page, size)
      dto/result/
        MarketSummaryResult(id, sportKey, homeTeam, awayTeam, commenceTime, homeOdds, drawOdds?, awayOdds, status)
        MarketDetailResult(... + totalBets, totalVolume)
        BetResult(id, marketId, outcomeType, stake, oddsAtPlacement, status, payout?)
      service/
        MarketService
        BetService
        SettlementService
    adapter/
      in/web/
        MarketController
        BetController
        AdminMarketController
      out/persistence/
        MarketJpaEntity
        BetJpaEntity
        MarketJpaRepository
        BetJpaRepository
        MarketPersistenceAdapter
  comment/                         # 댓글 시스템
    domain/
      Comment (Aggregate Root)
        - id: Long
        - marketId: Long
        - memberId: Long
        - parentId: Long?
        - depth: int (0 or 1)
        - content: String
        - likeCount: int
        - dislikeCount: int
        - deleted: boolean
        - createdAt: Instant
        - updatedAt: Instant
      CommentReaction (Entity)
        - id: Long
        - commentId: Long
        - memberId: Long
        - type: ReactionType
      ReactionType (enum) - LIKE, DISLIKE
    application/
      port/in/
        CreateCommentUseCase
        CreateReplyUseCase
        DeleteCommentUseCase
        ToggleReactionUseCase
        ListCommentsUseCase
      port/out/
        CommentRepository
        ReactionRepository
      dto/command/
        CreateCommentCommand(marketId, memberId, content)
        CreateReplyCommand(marketId, parentId, memberId, content)
        ToggleReactionCommand(commentId, memberId, type)
      dto/query/
        ListCommentsQuery(marketId, sort, page, size)
      dto/result/
        CommentResult(id, nickname, content, likeCount, dislikeCount, createdAt, replies[], myReaction?)
      service/
        CommentService
    adapter/
      in/web/
        CommentController
      out/persistence/
        CommentJpaEntity
        ReactionJpaEntity
        CommentJpaRepository
        ReactionJpaRepository
        CommentPersistenceAdapter
  odds/                            # The Odds API 연동
    adapter/
      out/external/
        TheOddsApiClient (WebClient)
        OddsApiScheduler
        ScoreApiScheduler
        FixtureSyncScheduler
```

### 모듈 간 의존성

```
security → identity (회원 조회/생성 via port)
market → identity (포인트 차감/지급 via port)
market → odds (배당 조회 via port)
comment → market (댓글이 속한 마켓 확인)
comment → identity (작성자 닉네임 조회)
```

의존성 방향: `adapter → application → domain`. 도메인은 의존성이 없다.

---

## 3. 데이터베이스 스키마

PostgreSQL 17, Flyway 마이그레이션으로 관리. JPA ddl-auto = validate.

### 테이블

```sql
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
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_comment_depth CHECK (
        parent_id IS NULL
        OR EXISTS (SELECT 1 FROM comment p WHERE p.id = comment.parent_id AND p.parent_id IS NULL)
    )
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
```

### 인덱스

```sql
CREATE INDEX idx_market_status ON market(status);
CREATE INDEX idx_market_commence ON market(commence_time);
CREATE INDEX idx_market_sport_status ON market(sport_key, status);
CREATE INDEX idx_bet_member ON bet(member_id);
CREATE INDEX idx_bet_market ON bet(market_id);
CREATE INDEX idx_bet_market_status ON bet(market_id, status);
CREATE INDEX idx_comment_market_created ON comment(market_id, created_at);
CREATE INDEX idx_comment_parent ON comment(parent_id);
CREATE INDEX idx_point_tx_member_created ON point_transaction(member_id, created_at);
```

---

## 4. 핵심 유스케이스 플로우

### 4.1 회원가입/로그인

```
사용자 → 프론트엔드 "Google 로그인" 버튼
→ 백엔드 GET /api/auth/google 리다이렉트
→ Google OAuth2 인증
→ 백엔드 GET /api/auth/google/callback
→ security 모듈에서:
    기존 회원 → JWT 발급
    신규 회원 → identity 모듈:
        1. 이메일 중복 확인
        2. NicknameGenerator로 형용사+명사 닉네임 생성 (중복 시 재생성)
        3. Member 생성 (role=USER, points=1000)
        4. PointTransaction 기록 (SIGNUP_BONUS, +1000)
    → JWT 발급
→ 프론트엔드에 HttpOnly 쿠키로 JWT 전달
```

### 4.2 마켓 생성 및 배당 관리

```
[FixtureSyncScheduler] 1일 1회
→ odds 모듈이 향후 7일 경기 목록 동기화
→ 새 경기 → Market 자동 생성 (status=PENDING)

[OddsApiScheduler] 2회/일
→ PENDING/OPEN 마켓의 배당 업데이트
→ 기존 배팅의 oddsAtPlacement는 변경되지 않음

[관리자] 대시보드에서 PENDING 마켓 확인
→ OpenMarketUseCase로 OPEN 상태로 변경
→ 사용자에게 마켓 노출
```

### 4.3 배팅

```
사용자 → 마켓 상세 페이지
→ 홈/무/원 중 선택
→ 배팅 포인트 입력 (최소 10P)
→ POST /api/markets/{id}/bets
→ market 모듈:
    1. 마켓 status=OPEN 확인
    2. stake >= 10 확인
    3. identity 모듈에서 포인트 차감 (via port)
    4. 현재 배당 스냅샷 (oddsAtPlacement)
    5. Bet 생성 (status=PENDING)
    6. PointTransaction 기록 (BET_PLACED, -stake)
```

### 4.4 정산

```
[ScoreApiScheduler] 경기 시간대 10분마다
→ odds 모듈이 완료된 경기 결과 수집
→ market 모듈의 SettleMarketUseCase:
    1. 마켓 result 설정 (HOME/DRAW/AWAY)
    2. 해당 마켓의 모든 PENDING bet 조회
    3. 각 bet 판정:
       - 정답 → WON, payout = floor(stake × oddsAtPlacement)
       - 오답 → LOST
    4. WON 베팅 포인트 적립 (identity.port.out)
    5. PointTransaction 기록 (BET_WON, +payout)
    6. 마켓 status → SETTLED
```

### 4.5 무료 포인트 충전

```
사용자 → "무료 충전" 버튼
→ POST /api/members/me/free-charge
→ identity 모듈:
    1. 현재 포인트 == 0 확인 (아니면 FreeChargeNotAllowedException)
    2. lastFreeChargeDate != today 확인 (이미 충전했으면 DailyChargeLimitExceededException)
    3. 포인트 +100
    4. lastFreeChargeDate = today
    5. PointTransaction 기록 (FREE_CHARGE, +100)
```

---

## 5. API 설계

### 5.1 인증 (security 모듈)

```
GET  /api/auth/google              → Google OAuth2 리다이렉트
GET  /api/auth/google/callback     → OAuth2 콜백 (JWT 발급, HttpOnly 쿠키)
POST /api/auth/logout              → 로그아웃 (쿠키 삭제)
GET  /api/auth/me                  → 현재 인증된 사용자 정보
```

### 5.2 회원 (identity 모듈)

```
GET    /api/members/me               → 내 프로필 조회
PATCH  /api/members/me/nickname      → 닉네임 수정
POST   /api/members/me/free-charge   → 무료 포인트 충전
GET    /api/members/me/points        → 포인트 이력 조회 (페이지네이션)
```

### 5.3 마켓 (market 모듈)

```
GET   /api/markets                    → 마켓 목록 (?sport=soccer_epl&status=OPEN&page=0&size=20)
GET   /api/markets/{id}               → 마켓 상세
GET   /api/markets/{id}/bets          → 마켓 배팅 목록 (공개)
POST  /api/markets/{id}/bets          → 배팅 (인증 필요)
GET   /api/members/me/bets            → 내 배팅 목록 (?status=PENDING&page=0&size=20)
```

### 5.4 댓글 (comment 모듈)

```
GET    /api/markets/{id}/comments              → 댓글 목록 (?sort=latest|popular&page=0&size=20)
POST   /api/markets/{id}/comments              → 댓글 작성
POST   /api/markets/{id}/comments/{parentId}/replies → 대댓글 작성 (depth 1)
DELETE /api/comments/{id}                       → 삭제 (작성자 or 관리자)
POST   /api/comments/{id}/reactions             → 반응 토글 (LIKE/DISLIKE)
```

### 5.5 관리자 (admin → market)

```
GET    /api/admin/markets                 → 전체 마켓 관리 목록
PATCH  /api/admin/markets/{id}/status     → 마켓 상태 변경
GET    /api/admin/stats                   → 통계 대시보드 요약
GET    /api/admin/stats/betting           → 배팅 통계
GET    /api/admin/stats/members           → 회원 통계
```

---

## 6. 프론트엔드 구조

### 기술 스택

- **Next.js App Router** (TypeScript)
- **Tailwind CSS** (라이트+미니멀 테마)
- **React Query** (서버 상태)
- **Zustand** (클라이언트 상태)
- **next-intl** (i18n: 한국어 + 영어)
- **인증**: HttpOnly 쿠키 JWT, 미들웨어 인증 체크

### 페이지 구성

```
app/
  [locale]/                         # i18n 라우팅
    (public)/
      page.tsx                      # 홈: 활성 마켓 목록
      markets/[id]/page.tsx         # 마켓 상세: 배팅 + 댓글
      login/page.tsx                # 로그인
    (auth)/
      profile/page.tsx              # 내 프로필
      bets/page.tsx                 # 내 배팅 목록
    (admin)/
      dashboard/page.tsx            # 관리자 대시보드
      markets/page.tsx              # 마켓 관리
      stats/page.tsx                # 통계
```

### 주요 컴포넌트

```
components/
  market/
    MarketCard.tsx                  # 마켓 요약 카드
    MarketList.tsx                  # 마켓 목록 + 스포츠 필터
    BetForm.tsx                     # 배팅 폼
    BetHistory.tsx                  # 배팅 이력
  comment/
    CommentList.tsx                 # 댓글 목록 (정렬 탭)
    CommentItem.tsx                 # 댓글 + 대댓글
    CommentForm.tsx                 # 댓글/대댓글 작성
  auth/
    LoginButton.tsx                 # Google 로그인 버튼
    UserMenu.tsx                    # 사용자 메뉴
  common/
    PointsDisplay.tsx               # 포인트 표시
    FreeChargeButton.tsx            # 무료 충전 버튼
    Pagination.tsx
    SportFilter.tsx                 # 스포츠 필터
```

---

## 7. 스케줄러 & 외부 연동

### The Odds API 연동 (WebClient)

| 스케줄러 | 빈도 | 월 사용량 | 설명 |
|---|---|---|---|
| FixtureSyncScheduler | 1회/일 | ~30 | 향후 7일 경기 목록 동기화 |
| OddsApiScheduler | 2회/일 | ~60 | PENDING/OPEN 마켓 배당 업데이트 |
| ScoreApiScheduler | 경기 시간대 10분 | ~200 | 경기 결과 수집 → 자동 정산 |
| 여유분 | - | ~210 | |

총 500 credits/월 내에서 운영.

### The Odds API 스포츠 키 (MVP 대상)

- 축구: `soccer_epl`, `soccer_spain_la_liga`, `soccer_germany_bundesliga`, `soccer_italy_serie_a`, `soccer_france_ligue_one`
- 야구: `baseball_mlb`, `baseball_kbo`
- 농구: `basketball_nba`

---

## 8. 에러 처리

### 도메인 예외

| 모듈 | 예외 | HTTP 상태 |
|---|---|---|
| identity | NicknameAlreadyExistsException | 409 |
| identity | InsufficientPointsException | 400 |
| identity | FreeChargeNotAllowedException | 400 |
| identity | DailyChargeLimitExceededException | 400 |
| market | MarketNotOpenException | 400 |
| market | MarketAlreadySettledException | 400 |
| market | BetBelowMinimumException | 400 |
| comment | CommentDepthExceededException | 400 |
| comment | CommentNotFoundException | 404 |
| comment | CannotDeleteCommentException | 403 |

### 에러 응답 형식

```json
{
  "code": "INSUFFICIENT_POINTS",
  "message": "포인트가 부족합니다.",
  "detail": "현재 포인트: 5, 배팅 금액: 100"
}
```

---

## 9. 테스트 전략

| 계층 | 전략 | 세부 사항 |
|---|---|---|
| domain | 순수 단위 테스트 | Spring 없이 비즈니스 규칙 (Points 연산, Market 상태 전이, Comment depth 제약) |
| application | Mockito 단위 테스트 | 포트 목킹, 유스케이스 오케스트레이션 검증 |
| adapter/in/web | @SpringBootTest + MockMvc | HTTP + DB 통합 테스트 |
| adapter/out/persistence | 영속성 매핑 테스트 | JPA ↔ 도메인 변환 검증 |
| architecture | ModulithArchitectureTests | 모듈 경계/패키지 구조 검증 |
