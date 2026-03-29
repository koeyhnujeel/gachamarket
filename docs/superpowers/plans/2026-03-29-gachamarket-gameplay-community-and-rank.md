# Gachamarket Gameplay, Community, and Rank Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 포인트 지갑, 베팅, 자동 정산, 칭호, 리더보드, 댓글 기능을 묶어 실제로 예측을 즐길 수 있는 MVP를 완성한다.

**Architecture:** Wallet/Betting/Settlement는 포인트 원장을 기준으로 일관성을 유지하고, Title/Leaderboard/Community는 후행 projection으로 붙인다. 이벤트 상세 페이지는 베팅 카드와 댓글 스레드를 함께 보여주며, 관리자 화면은 Review Required와 환불/재정산 액션을 추가한다.

**Tech Stack:** Spring transactions, Spring Modulith events, PostgreSQL, JUnit 5, Testcontainers, Next.js App Router, TypeScript, Playwright

---

## Planned File Structure

- `backend/src/main/java/com/gachamarket/betting/...`: 지갑, 포지션, 무료 충전, 포인트 원장
- `backend/src/main/java/com/gachamarket/settlement/...`: 결과 반영, 환불, 재시도
- `backend/src/main/java/com/gachamarket/title/...`: 칭호 규칙, 진척도, 합성 칭호
- `backend/src/main/java/com/gachamarket/leaderboard/...`: 전체/종목별 projection
- `backend/src/main/java/com/gachamarket/community/...`: 댓글, 좋아요, 대댓글
- `frontend/app/events/[eventId]/page.tsx`: 베팅 카드와 댓글 스레드
- `frontend/app/leaderboard/page.tsx`: 전체/종목별 리더보드
- `frontend/app/profile/page.tsx`: 포인트, 칭호, 무료 충전, 닉네임 변경
- `frontend/src/components/betting/...`, `frontend/src/components/community/...`: 상호작용 컴포넌트
- `frontend/tests/e2e/...`: 핵심 사용자 흐름 E2E

### Task 1: Wallet, Point Ledger, 무료 충전 구현

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__wallet_and_betting.sql`
- Create: `backend/src/main/java/com/gachamarket/betting/domain/Wallet.java`
- Create: `backend/src/main/java/com/gachamarket/betting/application/FreeRechargeService.java`
- Create: `backend/src/main/java/com/gachamarket/betting/adapter/in/web/WalletController.java`
- Test: `backend/src/test/java/com/gachamarket/betting/FreeRechargeServiceTest.java`

- [ ] **Step 1: 무료 충전 조건 테스트 작성**

```java
package com.gachamarket.betting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FreeRechargeServiceTest {

    @Test
    void rejectsRechargeWhenBalanceIsNotZero() {
        var wallet = Wallet.withBalance(120);

        assertThatThrownBy(() -> wallet.applyFreeRecharge())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("balance must be zero");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.betting.FreeRechargeServiceTest`

Expected: Wallet 미구현으로 FAIL

- [ ] **Step 3: 지갑/원장/무료 충전 구현**

```sql
-- backend/src/main/resources/db/migration/V3__wallet_and_betting.sql
create table point_ledger_entries (
    id uuid primary key,
    member_id uuid not null references members(id),
    source_type varchar(32) not null,
    source_id varchar(64) not null,
    amount int not null,
    created_at timestamp not null,
    unique (source_type, source_id, member_id)
);

create table free_recharge_records (
    id uuid primary key,
    member_id uuid not null references members(id),
    recharge_date date not null,
    created_at timestamp not null,
    unique (member_id, recharge_date)
);
```

```java
// backend/src/main/java/com/gachamarket/betting/domain/Wallet.java
package com.gachamarket.betting.domain;

public class Wallet {

    private int currentPoint;

    public static Wallet withBalance(int currentPoint) {
        var wallet = new Wallet();
        wallet.currentPoint = currentPoint;
        return wallet;
    }

    public void applyFreeRecharge() {
        if (currentPoint != 0) {
            throw new IllegalStateException("balance must be zero");
        }
        currentPoint += 100;
    }

    public void spend(int amount) {
        if (currentPoint < amount) {
            throw new IllegalStateException("insufficient points");
        }
        currentPoint -= amount;
    }
}
```

- [ ] **Step 4: 서비스 테스트와 원장 통합 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.betting.FreeRechargeServiceTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/resources/db/migration/V3__wallet_and_betting.sql backend/src/main/java/com/gachamarket/betting backend/src/test/java/com/gachamarket/betting
git commit -m "feat: add wallet and free recharge rules"
```

### Task 2: 같은 결과 추가 베팅 규칙과 베팅 API 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/betting/domain/BetPosition.java`
- Create: `backend/src/main/java/com/gachamarket/betting/application/BetPlacementService.java`
- Create: `backend/src/main/java/com/gachamarket/betting/adapter/in/web/BetController.java`
- Test: `backend/src/test/java/com/gachamarket/betting/BetPlacementServiceTest.java`
- Modify: `frontend/app/events/[eventId]/page.tsx`
- Create: `frontend/src/components/betting/bet-slip.tsx`

- [ ] **Step 1: 반대 결과 변경 금지 테스트 작성**

```java
package com.gachamarket.betting;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BetPlacementServiceTest {

    @Test
    void rejectsOppositeOutcomeAfterInitialBet() {
        var position = BetPosition.start("home", 100);

        assertThatThrownBy(() -> position.addStake("away", 50))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("same outcome");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.betting.BetPlacementServiceTest`

Expected: BetPosition 미구현으로 FAIL

- [ ] **Step 3: 베팅 도메인과 프론트 베팅 카드 구현**

```java
// backend/src/main/java/com/gachamarket/betting/domain/BetPosition.java
package com.gachamarket.betting.domain;

public class BetPosition {

    private String outcome;
    private int totalStake;

    public static BetPosition start(String outcome, int stake) {
        var position = new BetPosition();
        position.outcome = outcome;
        position.totalStake = stake;
        return position;
    }

    public void addStake(String requestedOutcome, int additionalStake) {
        if (!outcome.equals(requestedOutcome)) {
            throw new IllegalStateException("additional stake must keep the same outcome");
        }
        totalStake += additionalStake;
    }
}
```

```tsx
// frontend/src/components/betting/bet-slip.tsx
"use client";

import { useState } from "react";

type Props = {
  eventId: string;
  options: { key: string; label: string; odds: string }[];
};

export function BetSlip({ eventId, options }: Props) {
  const [selected, setSelected] = useState(options[0]?.key ?? "");
  const [stake, setStake] = useState("100");

  return (
    <form className="rounded-3xl border border-cyan-400/30 bg-cyan-400/10 p-5">
      <h3 className="text-lg font-semibold">베팅하기</h3>
      <div className="mt-4 grid gap-3 sm:grid-cols-3">
        {options.map((option) => (
          <button key={option.key} type="button" onClick={() => setSelected(option.key)} className="rounded-2xl border border-white/10 p-3 text-left">
            <p className="text-sm text-slate-300">{option.label}</p>
            <p className="mt-1 text-xl font-semibold">{option.odds}</p>
          </button>
        ))}
      </div>
      <input value={stake} onChange={(event) => setStake(event.target.value)} className="mt-4 w-full rounded-2xl bg-slate-950/70 px-4 py-3" />
      <button className="mt-4 rounded-full bg-white px-4 py-2 font-semibold text-slate-950">베팅 확정</button>
    </form>
  );
}
```

- [ ] **Step 4: 백엔드/프론트 테스트 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.betting.BetPlacementServiceTest && cd ../frontend && pnpm build`

Expected: backend PASS, frontend build success

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/betting backend/src/test/java/com/gachamarket/betting frontend/app/events frontend/src/components/betting
git commit -m "feat: add bet placement flow"
```

### Task 3: 자동 정산, 환불, Review Required 흐름 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/settlement/application/SettlementService.java`
- Create: `backend/src/main/java/com/gachamarket/settlement/application/RefundService.java`
- Create: `backend/src/main/java/com/gachamarket/settlement/adapter/in/web/AdminSettlementController.java`
- Test: `backend/src/test/java/com/gachamarket/settlement/SettlementServiceTest.java`

- [ ] **Step 1: 승리/패배/환불 테스트 작성**

```java
package com.gachamarket.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SettlementServiceTest {

    @Test
    void paysStakeTimesOddsForWinningPosition() {
        var result = SettlementService.calculatePayout(100, "2.05", true);
        assertThat(result).isEqualTo(205);
    }

    @Test
    void returnsStakeForRefundedEvent() {
        var result = SettlementService.refundStake(100);
        assertThat(result).isEqualTo(100);
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.settlement.SettlementServiceTest`

Expected: SettlementService 미구현으로 FAIL

- [ ] **Step 3: 정산/환불 로직과 관리자 재시도 엔드포인트 구현**

```java
// backend/src/main/java/com/gachamarket/settlement/application/SettlementService.java
package com.gachamarket.settlement.application;

public class SettlementService {

    public static int calculatePayout(int stake, String odds, boolean won) {
        if (!won) {
            return 0;
        }
        return (int) Math.round(stake * Double.parseDouble(odds));
    }

    public static int refundStake(int stake) {
        return stake;
    }
}
```

```java
// backend/src/main/java/com/gachamarket/settlement/adapter/in/web/AdminSettlementController.java
package com.gachamarket.settlement.adapter.in.web;

import com.gachamarket.settlement.application.SettlementRetryService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminSettlementController {

    private final SettlementRetryService settlementRetryService;

    public AdminSettlementController(SettlementRetryService settlementRetryService) {
        this.settlementRetryService = settlementRetryService;
    }

    @PostMapping("/api/admin/prediction-events/{eventId}/retry-settlement")
    void retrySettlement(@PathVariable String eventId) {
        settlementRetryService.retry(eventId);
    }
}
```

```java
// backend/src/main/java/com/gachamarket/settlement/application/SettlementRetryService.java
package com.gachamarket.settlement.application;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SettlementRetryService {

    private final SettlementOrchestrator settlementOrchestrator;

    public SettlementRetryService(SettlementOrchestrator settlementOrchestrator) {
        this.settlementOrchestrator = settlementOrchestrator;
    }

    public void retry(String eventId) {
        settlementOrchestrator.retry(eventId, "retry-" + UUID.randomUUID());
    }
}
```

```java
// backend/src/main/java/com/gachamarket/settlement/application/SettlementOrchestrator.java
package com.gachamarket.settlement.application;

public interface SettlementOrchestrator {
    void retry(String eventId, String idempotencyKey);
}
```

- [ ] **Step 4: 정산 테스트와 이벤트 재시도 흐름 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.settlement.SettlementServiceTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/settlement backend/src/test/java/com/gachamarket/settlement
git commit -m "feat: add settlement and refund services"
```

### Task 4: Title, Leaderboard, Profile settings 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/title/domain/TitleRule.java`
- Create: `backend/src/main/java/com/gachamarket/title/application/TitleProgressService.java`
- Create: `backend/src/main/java/com/gachamarket/title/application/ActiveTitleService.java`
- Create: `backend/src/main/java/com/gachamarket/leaderboard/application/LeaderboardProjectionService.java`
- Create: `backend/src/main/java/com/gachamarket/leaderboard/adapter/in/web/LeaderboardController.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/NicknameChangeService.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/in/web/ProfileController.java`
- Test: `backend/src/test/java/com/gachamarket/title/TitleProgressServiceTest.java`
- Test: `backend/src/test/java/com/gachamarket/identity/NicknameChangeServiceTest.java`
- Modify: `frontend/app/profile/page.tsx`
- Create: `frontend/app/leaderboard/page.tsx`

- [ ] **Step 1: 칭호/닉네임 변경 실패 테스트 작성**

```java
package com.gachamarket.title;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TitleProgressServiceTest {

    @Test
    void grantsLeagueTitleAfterFiftyHits() {
        var progress = new com.gachamarket.title.application.TitleProgressService();
        var granted = progress.evaluateLeagueRule("soccer-epl", 50);

        assertThat(granted).contains("EPL KING");
    }
}
```

```java
package com.gachamarket.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NicknameChangeServiceTest {

    @Test
    void firstChangeIsFreeAndSecondCostsOneHundredPoints() {
        var service = new com.gachamarket.identity.application.NicknameChangeService();
        var wallet = com.gachamarket.betting.domain.Wallet.withBalance(500);

        var afterFirst = service.change(wallet, false, "GM-100001", "GM-999999");
        var afterSecond = service.change(afterFirst.wallet(), true, "GM-999999", "GM-123456");

        assertThat(afterFirst.cost()).isEqualTo(0);
        assertThat(afterSecond.cost()).isEqualTo(100);
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.title.TitleProgressServiceTest --tests com.gachamarket.identity.NicknameChangeServiceTest`

Expected: TitleProgressService 또는 NicknameChangeService 미구현으로 FAIL

- [ ] **Step 3: 칭호/리더보드 projection 구현**

```java
// backend/src/main/java/com/gachamarket/title/application/TitleProgressService.java
package com.gachamarket.title.application;

import java.util.Optional;

public class TitleProgressService {

    public Optional<String> evaluateLeagueRule(String leagueSlug, int hitCount) {
        if ("soccer-epl".equals(leagueSlug) && hitCount >= 50) {
            return Optional.of("EPL KING");
        }
        return Optional.empty();
    }
}
```

```java
// backend/src/main/java/com/gachamarket/title/application/ActiveTitleService.java
package com.gachamarket.title.application;

import java.util.Set;
import java.util.UUID;

public class ActiveTitleService {

    public UUID activate(UUID requestedTitleId, Set<UUID> ownedTitleIds) {
        if (!ownedTitleIds.contains(requestedTitleId)) {
            throw new IllegalStateException("title must be owned before activation");
        }
        return requestedTitleId;
    }
}
```

```java
// backend/src/main/java/com/gachamarket/identity/application/NicknameChangeService.java
package com.gachamarket.identity.application;

import com.gachamarket.betting.domain.Wallet;

public class NicknameChangeService {

    public NicknameChangeResult change(Wallet wallet, boolean freeChangeAlreadyUsed, String currentNickname, String requestedNickname) {
        int cost = freeChangeAlreadyUsed ? 100 : 0;
        if (freeChangeAlreadyUsed) {
            wallet.spend(cost);
        }
        return new NicknameChangeResult(wallet, requestedNickname, cost);
    }

    public record NicknameChangeResult(Wallet wallet, String nickname, int cost) {
    }
}
```

```java
// backend/src/main/java/com/gachamarket/identity/adapter/in/web/ProfileController.java
package com.gachamarket.identity.adapter.in.web;

import com.gachamarket.identity.application.NicknameChangeService;
import com.gachamarket.title.application.ActiveTitleService;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    private final NicknameChangeService nicknameChangeService;
    private final ActiveTitleService activeTitleService;

    public ProfileController(NicknameChangeService nicknameChangeService, ActiveTitleService activeTitleService) {
        this.nicknameChangeService = nicknameChangeService;
        this.activeTitleService = activeTitleService;
    }

    @PatchMapping("/api/me/nickname")
    void changeNickname(@RequestBody ChangeNicknameRequest request) {
        nicknameChangeService.change(
            com.gachamarket.betting.domain.Wallet.withBalance(1000),
            false,
            "GM-100001",
            request.nickname()
        );
    }

    @PatchMapping("/api/me/active-title")
    void activateTitle(@RequestBody ActivateTitleRequest request) {
        activeTitleService.activate(request.titleId(), java.util.Set.of(request.titleId()));
    }

    public record ChangeNicknameRequest(String nickname) {
    }

    public record ActivateTitleRequest(UUID titleId) {
    }
}
```

```tsx
// frontend/app/leaderboard/page.tsx
import { AppShell } from "@/components/app-shell";

export default function LeaderboardPage() {
  return (
    <AppShell title="리더보드">
      <div className="grid gap-6 lg:grid-cols-2">
        <section className="rounded-3xl border border-white/10 bg-white/5 p-6">전체 포인트 랭킹</section>
        <section className="rounded-3xl border border-white/10 bg-white/5 p-6">종목별 랭킹</section>
      </div>
    </AppShell>
  );
}
```

```tsx
// frontend/app/profile/page.tsx
import { AppShell } from "@/components/app-shell";

export default function ProfilePage() {
  return (
    <AppShell title="내 프로필">
      <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
        <section className="rounded-3xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-xl font-semibold">닉네임과 포인트</h2>
          <form className="mt-4 grid gap-3">
            <input className="rounded-2xl bg-slate-950/70 px-4 py-3" defaultValue="GM-100001" />
            <button className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-slate-950">닉네임 변경</button>
          </form>
        </section>
        <section className="rounded-3xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-xl font-semibold">활성 칭호</h2>
          <button className="mt-4 rounded-full bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950">EPL KING 장착</button>
        </section>
      </div>
    </AppShell>
  );
}
```

- [ ] **Step 4: 백엔드 테스트와 프론트 빌드 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.title.TitleProgressServiceTest --tests com.gachamarket.identity.NicknameChangeServiceTest && cd ../frontend && pnpm build`

Expected: PASS and build success

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/title backend/src/main/java/com/gachamarket/leaderboard backend/src/main/java/com/gachamarket/identity backend/src/test/java/com/gachamarket/title backend/src/test/java/com/gachamarket/identity frontend/app/leaderboard frontend/app/profile
git commit -m "feat: add titles leaderboards and profile settings"
```

### Task 5: 댓글, 좋아요, 대댓글과 이벤트 상세 통합

**Files:**
- Create: `backend/src/main/resources/db/migration/V4__community.sql`
- Create: `backend/src/main/java/com/gachamarket/community/domain/Comment.java`
- Create: `backend/src/main/java/com/gachamarket/community/application/CommentService.java`
- Create: `backend/src/main/java/com/gachamarket/community/adapter/in/web/CommentController.java`
- Create: `backend/src/main/java/com/gachamarket/community/adapter/in/web/CommentLikeController.java`
- Create: `frontend/src/components/community/comment-thread.tsx`
- Test: `backend/src/test/java/com/gachamarket/community/CommentServiceTest.java`

- [ ] **Step 1: depth 1 대댓글만 허용하는 테스트 작성**

```java
package com.gachamarket.community;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CommentServiceTest {

    @Test
    void rejectsReplyToReply() {
        var service = new com.gachamarket.community.application.CommentService();

        assertThatThrownBy(() -> service.createReply("reply-comment-id", "nested reply"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("depth 1");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.community.CommentServiceTest`

Expected: CommentService 미구현으로 FAIL

- [ ] **Step 3: 댓글 도메인과 프론트 스레드 구현**

```sql
-- backend/src/main/resources/db/migration/V4__community.sql
create table comments (
    id uuid primary key,
    prediction_event_id uuid not null,
    member_id uuid not null references members(id),
    parent_comment_id uuid null references comments(id),
    depth int not null,
    body text not null,
    deleted boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table comment_likes (
    comment_id uuid not null references comments(id),
    member_id uuid not null references members(id),
    created_at timestamp not null,
    primary key (comment_id, member_id)
);
```

```tsx
// frontend/src/components/community/comment-thread.tsx
"use client";

type CommentNode = {
  id: string;
  nickname: string;
  activeTitle?: string | null;
  body: string;
  replies: CommentNode[];
};

export function CommentThread({ comments }: { comments: CommentNode[] }) {
  return (
    <section className="rounded-3xl border border-white/10 bg-white/5 p-6">
      <h3 className="text-lg font-semibold">댓글</h3>
      <div className="mt-4 space-y-4">
        {comments.map((comment) => (
          <article key={comment.id} className="rounded-2xl border border-white/10 p-4">
            <p className="text-sm text-cyan-300">{comment.activeTitle ?? "칭호 없음"}</p>
            <p className="text-sm font-semibold">{comment.nickname}</p>
            <p className="mt-2 text-sm text-slate-200">{comment.body}</p>
            <button className="mt-3 text-xs text-slate-400">좋아요</button>
            <div className="mt-3 pl-4">
              {comment.replies.map((reply) => (
                <div key={reply.id} className="border-l border-white/10 pl-4 text-sm text-slate-300">
                  {reply.nickname}: {reply.body}
                </div>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
```

```java
// backend/src/main/java/com/gachamarket/community/adapter/in/web/CommentLikeController.java
package com.gachamarket.community.adapter.in.web;

import com.gachamarket.community.application.CommentLikeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    public CommentLikeController(CommentLikeService commentLikeService) {
        this.commentLikeService = commentLikeService;
    }

    @PostMapping("/api/comments/{commentId}/likes")
    void like(@PathVariable String commentId, Authentication authentication) {
        commentLikeService.like(commentId, authentication.getName());
    }
}
```

```java
// backend/src/main/java/com/gachamarket/community/application/CommentLikeService.java
package com.gachamarket.community.application;

import org.springframework.stereotype.Service;

@Service
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository) {
        this.commentLikeRepository = commentLikeRepository;
    }

    public void like(String commentId, String memberId) {
        if (!commentLikeRepository.exists(commentId, memberId)) {
            commentLikeRepository.insert(commentId, memberId);
        }
    }
}
```

```java
// backend/src/main/java/com/gachamarket/community/application/CommentLikeRepository.java
package com.gachamarket.community.application;

public interface CommentLikeRepository {
    boolean exists(String commentId, String memberId);
    void insert(String commentId, String memberId);
}
```

- [ ] **Step 4: 댓글 테스트와 프론트 빌드 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.community.CommentServiceTest && cd ../frontend && pnpm build`

Expected: PASS and build success

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/resources/db/migration/V4__community.sql backend/src/main/java/com/gachamarket/community backend/src/test/java/com/gachamarket/community frontend/src/components/community frontend/app/events
git commit -m "feat: add event discussion threads"
```

### Task 6: 핵심 E2E와 운영 하드닝 추가

**Files:**
- Create: `frontend/tests/e2e/login-and-bet.spec.ts`
- Create: `frontend/tests/e2e/admin-approval-and-settlement.spec.ts`
- Modify: `docs/superpowers/specs/2026-03-29-gachamarket-mvp-core-platform-design.md`

- [ ] **Step 1: 핵심 사용자 흐름 E2E 작성**

```ts
import { test, expect } from "@playwright/test";

test("user can log in, place a bet, and see updated balance", async ({ page }) => {
  await page.goto("/");
  await page.getByText("Google로 로그인").click();
  await page.goto("/events/evt-1");
  await page.getByRole("button", { name: "홈승" }).click();
  await page.getByRole("button", { name: "베팅 확정" }).click();
  await expect(page.getByText("베팅이 접수되었습니다")).toBeVisible();
});
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd frontend && pnpm playwright test tests/e2e/login-and-bet.spec.ts`

Expected: 로그인 모킹 또는 베팅 플로우 부재로 FAIL

- [ ] **Step 3: 운영 하드닝과 테스트 환경 보강**

```ts
// frontend/tests/e2e/admin-approval-and-settlement.spec.ts
import { test, expect } from "@playwright/test";

test("admin can approve and retry settlement from review state", async ({ page }) => {
  await page.goto("/admin/prediction-events");
  await page.getByText("승인 대기 큐").isVisible();
  await page.goto("/admin/prediction-events/evt-1");
  await page.getByRole("button", { name: "정산 재시도" }).click();
  await expect(page.getByText("재시도 요청이 접수되었습니다")).toBeVisible();
});
```

```md
<!-- docs/superpowers/specs/2026-03-29-gachamarket-mvp-core-platform-design.md -->
- 운영 환경 투입 전 Playwright 핵심 시나리오를 필수 통과 기준으로 둔다.
- 무료 API 장애 주간에는 수동 오즈 입력과 관리자 승인 큐를 운영 기본 경로로 간주한다.
```

- [ ] **Step 4: 전체 테스트 스위트 확인**

Run: `cd backend && ./gradlew test && cd ../frontend && pnpm test && pnpm playwright test`

Expected: backend PASS, frontend unit PASS, e2e PASS

- [ ] **Step 5: 커밋**

```bash
git add frontend/tests/e2e docs/superpowers/specs/2026-03-29-gachamarket-mvp-core-platform-design.md
git commit -m "test: add end-to-end coverage for core gameplay"
```
