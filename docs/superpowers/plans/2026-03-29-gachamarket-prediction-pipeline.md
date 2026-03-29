# Gachamarket Prediction Pipeline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 스포츠 경기 수집, 오즈 계산, 운영자 승인, 공개 이벤트 조회까지 이어지는 예측 이벤트 파이프라인을 구현한다.

**Architecture:** Source Intake는 공급자별 어댑터로 외부 데이터를 수집하고, Odds 모듈은 외부 스냅샷을 내부 확률 모델로 정규화한다. Prediction Event는 Source Item과 분리된 공개 객체로 두고, Admin 모듈이 승인/보류/수동 오즈 입력을 담당한다. 프론트는 공개 목록/상세 화면과 관리자 승인 화면을 같은 Next 앱 안에서 분리한다.

**Tech Stack:** Spring Scheduling, Spring Modulith events, JdbcTemplate or JPA for persistence, PostgreSQL, Flyway, JUnit 5, Testcontainers, Next.js App Router, TypeScript

---

## Planned File Structure

- `backend/src/main/java/com/gachamarket/sourceintake/...`: 소스 공급자, 동기화 서비스, Source Item 저장소
- `backend/src/main/java/com/gachamarket/odds/...`: 스냅샷 수집, 컨센서스 계산, 수동 오즈 입력
- `backend/src/main/java/com/gachamarket/prediction/...`: 공개 이벤트 도메인, 상태 전이, 공개 API
- `backend/src/main/java/com/gachamarket/admin/...`: 승인 큐, 수동 오즈 입력, 감사 로그
- `backend/src/main/resources/db/migration/V2__prediction_pipeline.sql`: 파이프라인 테이블
- `frontend/app/events/...`: 공개 이벤트 목록/상세
- `frontend/app/admin/...`: 관리자 승인 큐와 이벤트 상세
- `frontend/src/components/event-card.tsx`: 공개 이벤트 카드
- `frontend/src/components/admin/...`: 관리자 전용 컴포넌트

### Task 1: Source Intake 기본 모델과 동기화 서비스 구현

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__prediction_pipeline.sql`
- Create: `backend/src/main/java/com/gachamarket/sourceintake/domain/SourceItem.java`
- Create: `backend/src/main/java/com/gachamarket/sourceintake/application/SourceSyncService.java`
- Create: `backend/src/main/java/com/gachamarket/sourceintake/adapter/out/provider/SourceProvider.java`
- Test: `backend/src/test/java/com/gachamarket/sourceintake/SourceSyncServiceTest.java`

- [ ] **Step 1: 동기화가 신규 경기만 저장하는 테스트 작성**

```java
package com.gachamarket.sourceintake;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SourceSyncServiceTest {

    static class InMemorySourceItemRepository implements com.gachamarket.sourceintake.domain.SourceItemRepository {
        private final java.util.Map<String, SourceFixture> items = new java.util.HashMap<>();

        @Override
        public boolean exists(String provider, String externalId) {
            return items.containsKey(provider + ":" + externalId);
        }

        @Override
        public void save(SourceFixture fixture) {
            items.put(fixture.provider() + ":" + fixture.externalId(), fixture);
        }

        int count() {
            return items.size();
        }
    }

    @Test
    void savesOnlyNewFixtures() {
        var provider = () -> List.of(
            new SourceFixture("provider-a", "fixture-1", "soccer-epl", Instant.parse("2026-08-10T11:30:00Z")),
            new SourceFixture("provider-a", "fixture-2", "baseball-kbo", Instant.parse("2026-08-10T12:00:00Z"))
        );

        var repository = new InMemorySourceItemRepository();
        var service = new com.gachamarket.sourceintake.application.SourceSyncService(provider, repository);

        service.sync();
        service.sync();

        assertThat(repository.count()).isEqualTo(2);
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.sourceintake.SourceSyncServiceTest`

Expected: `SourceSyncService`, `SourceProvider`, `InMemorySourceItemRepository` 미구현으로 FAIL

- [ ] **Step 3: 최소 Source Intake 구현**

```sql
-- backend/src/main/resources/db/migration/V2__prediction_pipeline.sql
create table source_items (
    id uuid primary key,
    provider varchar(64) not null,
    external_id varchar(128) not null,
    category_slug varchar(64) not null,
    start_at timestamp not null,
    payload jsonb not null,
    result_status varchar(32) not null default 'PENDING',
    created_at timestamp not null,
    updated_at timestamp not null,
    unique (provider, external_id)
);
```

```java
// backend/src/main/java/com/gachamarket/sourceintake/adapter/out/provider/SourceProvider.java
package com.gachamarket.sourceintake.adapter.out.provider;

import java.util.List;

public interface SourceProvider {
    List<SourceFixture> fetchUpcomingFixtures();
}
```

```java
// backend/src/main/java/com/gachamarket/sourceintake/adapter/out/provider/SourceFixture.java
package com.gachamarket.sourceintake.adapter.out.provider;

import java.time.Instant;

public record SourceFixture(String provider, String externalId, String categorySlug, Instant startAt) {
}
```

```java
// backend/src/main/java/com/gachamarket/sourceintake/domain/SourceItemRepository.java
package com.gachamarket.sourceintake.domain;

import com.gachamarket.sourceintake.adapter.out.provider.SourceFixture;

public interface SourceItemRepository {
    boolean exists(String provider, String externalId);
    void save(SourceFixture fixture);
}
```

```java
// backend/src/main/java/com/gachamarket/sourceintake/application/SourceSyncService.java
package com.gachamarket.sourceintake.application;

import com.gachamarket.sourceintake.adapter.out.provider.SourceProvider;
import com.gachamarket.sourceintake.adapter.out.provider.SourceFixture;
import com.gachamarket.sourceintake.domain.SourceItemRepository;
import org.springframework.stereotype.Service;

@Service
public class SourceSyncService {

    private final SourceProvider sourceProvider;
    private final SourceItemRepository sourceItemRepository;

    public SourceSyncService(SourceProvider sourceProvider, SourceItemRepository sourceItemRepository) {
        this.sourceProvider = sourceProvider;
        this.sourceItemRepository = sourceItemRepository;
    }

    public void sync() {
        for (SourceFixture fixture : sourceProvider.fetchUpcomingFixtures()) {
            if (!sourceItemRepository.exists(fixture.provider(), fixture.externalId())) {
                sourceItemRepository.save(fixture);
            }
        }
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.sourceintake.SourceSyncServiceTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/resources/db/migration/V2__prediction_pipeline.sql backend/src/main/java/com/gachamarket/sourceintake backend/src/test/java/com/gachamarket/sourceintake
git commit -m "feat: add source intake sync baseline"
```

### Task 2: Odds 스냅샷과 컨센서스 계산 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/odds/domain/OddsSnapshot.java`
- Create: `backend/src/main/java/com/gachamarket/odds/application/ConsensusOddsCalculator.java`
- Create: `backend/src/main/java/com/gachamarket/odds/application/OddsCollectionService.java`
- Test: `backend/src/test/java/com/gachamarket/odds/ConsensusOddsCalculatorTest.java`

- [ ] **Step 1: 오즈 정규화 계산 테스트 작성**

```java
package com.gachamarket.odds;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConsensusOddsCalculatorTest {

    @Test
    void buildsMedianConsensusOdds() {
        var calculator = new com.gachamarket.odds.application.ConsensusOddsCalculator();
        var consensus = calculator.calculate(List.of(
            new com.gachamarket.odds.domain.OddsSnapshot(Map.of("home", new BigDecimal("2.10"), "draw", new BigDecimal("3.30"), "away", new BigDecimal("3.80"))),
            new com.gachamarket.odds.domain.OddsSnapshot(Map.of("home", new BigDecimal("2.00"), "draw", new BigDecimal("3.40"), "away", new BigDecimal("3.90"))),
            new com.gachamarket.odds.domain.OddsSnapshot(Map.of("home", new BigDecimal("2.05"), "draw", new BigDecimal("3.35"), "away", new BigDecimal("3.85")))
        ));

        assertThat(consensus.oddsFor("home")).isEqualByComparingTo(new BigDecimal("2.05"));
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.odds.ConsensusOddsCalculatorTest`

Expected: 계산기 미구현으로 FAIL

- [ ] **Step 3: 컨센서스 계산 구현**

```java
// backend/src/main/java/com/gachamarket/odds/application/ConsensusOddsCalculator.java
package com.gachamarket.odds.application;

import com.gachamarket.odds.domain.ConsensusOdds;
import com.gachamarket.odds.domain.OddsSnapshot;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsensusOddsCalculator {

    public ConsensusOdds calculate(List<OddsSnapshot> snapshots) {
        Map<String, BigDecimal> medianOdds = snapshots.stream()
            .flatMap(snapshot -> snapshot.outcomes().entrySet().stream())
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.collectingAndThen(Collectors.mapping(Map.Entry::getValue, Collectors.toList()), values -> {
                    values.sort(Comparator.naturalOrder());
                    return values.get(values.size() / 2).setScale(2, RoundingMode.HALF_UP);
                })
            ));

        return new ConsensusOdds(medianOdds);
    }
}
```

```java
// backend/src/main/java/com/gachamarket/odds/domain/OddsSnapshot.java
package com.gachamarket.odds.domain;

import java.math.BigDecimal;
import java.util.Map;

public record OddsSnapshot(Map<String, BigDecimal> outcomes) {
}
```

```java
// backend/src/main/java/com/gachamarket/odds/domain/ConsensusOdds.java
package com.gachamarket.odds.domain;

import java.math.BigDecimal;
import java.util.Map;

public record ConsensusOdds(Map<String, BigDecimal> outcomes) {
    public BigDecimal oddsFor(String outcome) {
        return outcomes.get(outcome);
    }
}
```

- [ ] **Step 4: 계산 테스트와 서비스 테스트 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.odds.ConsensusOddsCalculatorTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/odds backend/src/test/java/com/gachamarket/odds
git commit -m "feat: add odds consensus calculation"
```

### Task 3: Prediction Event 상태 전이와 승인 API 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/prediction/domain/PredictionEvent.java`
- Create: `backend/src/main/java/com/gachamarket/prediction/application/PredictionEventApprovalService.java`
- Create: `backend/src/main/java/com/gachamarket/admin/adapter/in/web/AdminPredictionEventController.java`
- Create: `backend/src/main/java/com/gachamarket/admin/application/AdminAuditService.java`
- Test: `backend/src/test/java/com/gachamarket/prediction/PredictionEventApprovalIntegrationTest.java`

- [ ] **Step 1: 승인 시점에 이벤트가 Published가 되는 테스트 작성**

```java
package com.gachamarket.prediction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PredictionEventApprovalIntegrationTest {

    @Test
    void approvesPendingEventWithFixedOdds() {
        var event = PredictionEvent.pending("fixture-1", "soccer-epl");

        event.approveWithConsensusOdds("2.05", "3.30", "3.80");

        assertThat(event.status()).isEqualTo(PredictionEventStatus.PUBLISHED);
        assertThat(event.homeOdds()).isEqualTo("2.05");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.prediction.PredictionEventApprovalIntegrationTest`

Expected: PredictionEvent 미구현으로 FAIL

- [ ] **Step 3: 상태 전이와 관리자 승인 구현**

```java
// backend/src/main/java/com/gachamarket/prediction/domain/PredictionEvent.java
package com.gachamarket.prediction.domain;

public class PredictionEvent {

    private PredictionEventStatus status;
    private String homeOdds;
    private String drawOdds;
    private String awayOdds;

    public static PredictionEvent pending(String sourceItemId, String categorySlug) {
        return new PredictionEvent(PredictionEventStatus.PENDING_APPROVAL);
    }

    private PredictionEvent(PredictionEventStatus status) {
        this.status = status;
    }

    public void approveWithConsensusOdds(String homeOdds, String drawOdds, String awayOdds) {
        if (status != PredictionEventStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("only pending events can be approved");
        }
        this.homeOdds = homeOdds;
        this.drawOdds = drawOdds;
        this.awayOdds = awayOdds;
        this.status = PredictionEventStatus.PUBLISHED;
    }
}
```

```java
// backend/src/main/java/com/gachamarket/prediction/domain/PredictionEventStatus.java
package com.gachamarket.prediction.domain;

public enum PredictionEventStatus {
    PENDING_APPROVAL,
    PUBLISHED,
    LOCKED,
    REVIEW_REQUIRED,
    SUSPENDED,
    SETTLED
}
```

```java
// backend/src/main/java/com/gachamarket/admin/adapter/in/web/AdminPredictionEventController.java
package com.gachamarket.admin.adapter.in.web;

import com.gachamarket.prediction.application.PredictionEventApprovalService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminPredictionEventController {

    private final PredictionEventApprovalService approvalService;

    public AdminPredictionEventController(PredictionEventApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/api/admin/prediction-events/{eventId}/approve")
    void approve(@PathVariable String eventId, @RequestBody ApprovalRequest request) {
        approvalService.approve(eventId, request.homeOdds(), request.drawOdds(), request.awayOdds(), request.memo());
    }
}
```

```java
// backend/src/main/java/com/gachamarket/admin/adapter/in/web/ApprovalRequest.java
package com.gachamarket.admin.adapter.in.web;

public record ApprovalRequest(String homeOdds, String drawOdds, String awayOdds, String memo) {
}
```

- [ ] **Step 4: 승인 테스트와 컨트롤러 슬라이스 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.prediction.PredictionEventApprovalIntegrationTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/prediction backend/src/main/java/com/gachamarket/admin backend/src/test/java/com/gachamarket/prediction
git commit -m "feat: add prediction event approval workflow"
```

### Task 4: 공개 이벤트 목록/상세와 관리자 승인 화면 구현

**Files:**
- Create: `frontend/app/events/page.tsx`
- Create: `frontend/app/events/[eventId]/page.tsx`
- Create: `frontend/app/admin/prediction-events/page.tsx`
- Create: `frontend/src/components/event-card.tsx`
- Create: `frontend/src/components/admin/approval-queue.tsx`
- Test: `frontend/src/components/__tests__/event-card.test.tsx`

- [ ] **Step 1: 이벤트 카드 테스트 작성**

```tsx
import { render, screen } from "@testing-library/react";
import { EventCard } from "@/components/event-card";

describe("EventCard", () => {
  it("renders odds, status, and lock time", () => {
    render(
      <EventCard
        event={{
          id: "evt-1",
          leagueName: "EPL",
          title: "Arsenal vs Chelsea",
          lockAtLabel: "마감 5분 전",
          homeOdds: "2.05",
          drawOdds: "3.30",
          awayOdds: "3.80",
        }}
      />,
    );

    expect(screen.getByText("Arsenal vs Chelsea")).toBeInTheDocument();
    expect(screen.getByText("2.05")).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd frontend && pnpm test -- --runInBand event-card`

Expected: `EventCard` component 없음으로 FAIL

- [ ] **Step 3: 공개/관리자 화면 작성**

```tsx
// frontend/src/components/event-card.tsx
type EventCardProps = {
  event: {
    id: string;
    leagueName: string;
    title: string;
    lockAtLabel: string;
    homeOdds: string;
    drawOdds: string;
    awayOdds: string;
  };
};

export function EventCard({ event }: EventCardProps) {
  return (
    <article className="rounded-3xl border border-white/10 bg-white/5 p-6">
      <p className="text-xs uppercase tracking-[0.3em] text-cyan-300">{event.leagueName}</p>
      <h2 className="mt-2 text-xl font-semibold">{event.title}</h2>
      <p className="mt-2 text-sm text-slate-300">{event.lockAtLabel}</p>
      <div className="mt-4 grid grid-cols-3 gap-3 text-center text-sm">
        <div className="rounded-2xl bg-slate-900/70 p-3">{event.homeOdds}</div>
        <div className="rounded-2xl bg-slate-900/70 p-3">{event.drawOdds}</div>
        <div className="rounded-2xl bg-slate-900/70 p-3">{event.awayOdds}</div>
      </div>
    </article>
  );
}
```

```tsx
// frontend/app/admin/prediction-events/page.tsx
import { AppShell } from "@/components/app-shell";

export default function AdminPredictionEventsPage() {
  return (
    <AppShell title="승인 대기 큐">
      <section className="grid gap-4">
        <article className="rounded-3xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-lg font-semibold">수집된 경기와 오즈 상태를 여기서 승인한다.</h2>
        </article>
      </section>
    </AppShell>
  );
}
```

- [ ] **Step 4: 프론트 테스트와 빌드 확인**

Run: `cd frontend && pnpm test -- --runInBand event-card && pnpm build`

Expected: PASS and build success

- [ ] **Step 5: 커밋**

```bash
git add frontend/app/events frontend/app/admin frontend/src/components
git commit -m "feat: add event discovery and admin approval screens"
```

### Task 5: 예약 작업과 파이프라인 통합 테스트 작성

**Files:**
- Create: `backend/src/main/java/com/gachamarket/sourceintake/application/SyncScheduler.java`
- Create: `backend/src/main/java/com/gachamarket/prediction/application/EventLockScheduler.java`
- Test: `backend/src/test/java/com/gachamarket/pipeline/PredictionPipelineIntegrationTest.java`

- [ ] **Step 1: 수집에서 Published까지 이어지는 통합 테스트 작성**

```java
package com.gachamarket.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PredictionPipelineIntegrationTest {

    @Test
    void importsFixtureCollectsOddsAndPublishesAfterApproval() {
        var sourceFixture = new com.gachamarket.sourceintake.adapter.out.provider.SourceFixture(
            "provider-a", "fixture-1", "soccer-epl", java.time.Instant.parse("2026-08-10T11:30:00Z"));
        var event = com.gachamarket.prediction.domain.PredictionEvent.pending(sourceFixture.externalId(), sourceFixture.categorySlug());
        event.approveWithConsensusOdds("2.05", "3.30", "3.80");

        assertThat(event.status().name()).isEqualTo("PUBLISHED");
        assertThat(event.homeOdds()).isEqualTo("2.05");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.pipeline.PredictionPipelineIntegrationTest`

Expected: 통합 wiring 부재 또는 assertion 실패로 FAIL

- [ ] **Step 3: 스케줄러와 파이프라인 wiring 추가**

```java
// backend/src/main/java/com/gachamarket/sourceintake/application/SyncScheduler.java
package com.gachamarket.sourceintake.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncScheduler {

    private final SourceSyncService sourceSyncService;

    public SyncScheduler(SourceSyncService sourceSyncService) {
        this.sourceSyncService = sourceSyncService;
    }

    @Scheduled(fixedDelayString = "${jobs.source-sync.delay:300000}")
    public void syncFixtures() {
        sourceSyncService.sync();
    }
}
```

```java
// backend/src/main/java/com/gachamarket/prediction/application/EventLockScheduler.java
package com.gachamarket.prediction.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EventLockScheduler {

    private final PredictionEventLockService lockService;

    public EventLockScheduler(PredictionEventLockService lockService) {
        this.lockService = lockService;
    }

    @Scheduled(fixedDelayString = "${jobs.lock-events.delay:60000}")
    public void lockEvents() {
        lockService.lockExpiredPublishEvents();
    }
}
```

```java
// backend/src/main/java/com/gachamarket/prediction/application/PredictionEventLockService.java
package com.gachamarket.prediction.application;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PredictionEventLockService {

    private final PredictionEventRepository predictionEventRepository;

    public PredictionEventLockService(PredictionEventRepository predictionEventRepository) {
        this.predictionEventRepository = predictionEventRepository;
    }

    public void lockExpiredPublishEvents() {
        predictionEventRepository.lockAllBefore(Instant.now().plus(Duration.ofMinutes(5)));
    }
}
```

```java
// backend/src/main/java/com/gachamarket/prediction/application/PredictionEventRepository.java
package com.gachamarket.prediction.application;

import java.time.Instant;

public interface PredictionEventRepository {
    void lockAllBefore(Instant threshold);
}
```

- [ ] **Step 4: 통합 테스트 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.pipeline.PredictionPipelineIntegrationTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/sourceintake/application backend/src/main/java/com/gachamarket/prediction/application backend/src/test/java/com/gachamarket/pipeline
git commit -m "feat: wire prediction pipeline schedulers"
```
