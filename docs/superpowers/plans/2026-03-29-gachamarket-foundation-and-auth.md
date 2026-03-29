# Gachamarket Foundation and Auth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 빈 저장소를 실행 가능한 백엔드/프론트엔드 골격, Google 로그인, 회원/카테고리 셸을 가진 상태로 만든다.

**Architecture:** 저장소 루트에 `backend/`와 `frontend/`를 병렬로 두고, 백엔드는 Spring Modulith 기반 모듈형 모놀리스로 시작한다. 프론트는 Next.js App Router를 사용해 읽기 화면은 서버 렌더링, 상호작용 화면은 클라이언트 컴포넌트로 분리한다. PostgreSQL은 도메인 영속성에만 쓰고, 세션은 백엔드가 쿠키로 관리한다.

**Tech Stack:** Java 21, Gradle, Spring Boot, Spring Modulith, Spring Security OAuth2 Client, PostgreSQL, Flyway, JUnit 5, Testcontainers, Next.js App Router, TypeScript, Tailwind CSS, Vitest, React Testing Library, pnpm

---

## Planned File Structure

- `backend/build.gradle.kts`: 백엔드 의존성과 테스트 설정
- `backend/settings.gradle.kts`: Gradle 루트 이름
- `backend/src/main/java/com/gachamarket/GachamarketApplication.java`: Spring Boot 진입점
- `backend/src/main/java/com/gachamarket/common/...`: 공통 설정, 에러 응답, 시간/감사 유틸
- `backend/src/main/java/com/gachamarket/identity/...`: 회원, 인증, 닉네임 정책
- `backend/src/main/java/com/gachamarket/category/...`: 카테고리 계층과 조회 API
- `backend/src/main/resources/application.yml`: 환경 설정 기본값
- `backend/src/main/resources/db/migration/V1__foundation.sql`: 기초 테이블
- `backend/src/test/java/...`: 모듈 통합 테스트와 스모크 테스트
- `frontend/package.json`: 프론트 의존성과 스크립트
- `frontend/app/...`: App Router 라우트
- `frontend/src/components/...`: 반응형 레이아웃, 로그인 CTA, 카테고리 네비게이션
- `frontend/src/lib/api.ts`: 백엔드 API fetch 래퍼
- `frontend/src/lib/session.ts`: 쿠키 기반 세션 유틸
- `frontend/src/components/__tests__/...`: 프론트 단위 테스트

### Task 1: 백엔드 서비스 골격 만들기

**Files:**
- Create: `backend/settings.gradle.kts`
- Create: `backend/build.gradle.kts`
- Create: `backend/src/main/java/com/gachamarket/GachamarketApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Test: `backend/src/test/java/com/gachamarket/bootstrap/ApplicationContextSmokeTest.java`

- [ ] **Step 1: 실패하는 스모크 테스트 작성**

```java
package com.gachamarket.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationContextSmokeTest {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: 테스트가 실제로 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.bootstrap.ApplicationContextSmokeTest`

Expected: `./gradlew: No such file or directory` 또는 `ApplicationContextSmokeTest`를 찾을 수 없어서 FAIL

- [ ] **Step 3: 최소 백엔드 애플리케이션 골격 작성**

```bash
curl -s "https://start.spring.io/starter.zip?type=gradle-project&language=java&baseDir=backend&groupId=com.gachamarket&artifactId=gachamarket&name=gachamarket&packageName=com.gachamarket&javaVersion=21&dependencies=web,data-jpa,security,oauth2-client,validation,postgresql,flyway,testcontainers" -o /tmp/gachamarket-backend.zip
unzip -q /tmp/gachamarket-backend.zip -d .
mkdir -p backend/src/test/java/com/gachamarket/bootstrap
```

```kotlin
// backend/settings.gradle.kts
rootProject.name = "gachamarket"
```

```java
// backend/src/main/java/com/gachamarket/GachamarketApplication.java
package com.gachamarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GachamarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GachamarketApplication.class, args);
    }
}
```

```yaml
# backend/src/main/resources/application.yml
spring:
  application:
    name: gachamarket
  datasource:
    url: jdbc:postgresql://localhost:5432/gachamarket
    username: gachamarket
    password: gachamarket
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  modulith:
    events:
      jdbc:
        schema-initialization:
          enabled: false
```

- [ ] **Step 4: 테스트가 통과하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.bootstrap.ApplicationContextSmokeTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend
git commit -m "chore: bootstrap backend service"
```

### Task 2: 프론트엔드 서비스와 반응형 셸 만들기

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/app/layout.tsx`
- Create: `frontend/app/page.tsx`
- Create: `frontend/src/components/app-shell.tsx`
- Test: `frontend/src/components/__tests__/app-shell.test.tsx`

- [ ] **Step 1: 반응형 셸 테스트 작성**

```tsx
import { render, screen } from "@testing-library/react";
import { AppShell } from "@/components/app-shell";

describe("AppShell", () => {
  it("renders desktop and mobile navigation labels", () => {
    render(<AppShell title="Gachamarket">content</AppShell>);

    expect(screen.getByText("경기")).toBeInTheDocument();
    expect(screen.getByText("리더보드")).toBeInTheDocument();
    expect(screen.getByText("프로필")).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd frontend && pnpm test app-shell`

Expected: `frontend` 디렉터리가 없거나 `AppShell` import 실패로 FAIL

- [ ] **Step 3: Next.js 앱과 기본 셸 작성**

```bash
pnpm dlx create-next-app@latest frontend --ts --eslint --app --src-dir --tailwind --import-alias "@/*" --use-pnpm
mkdir -p frontend/src/components/__tests__
cd frontend && pnpm add -D vitest jsdom @testing-library/react @testing-library/jest-dom @vitejs/plugin-react
```

```json
// frontend/package.json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "test": "vitest"
  }
}
```

```ts
// frontend/vitest.config.ts
import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import path from "node:path";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
```

```tsx
// frontend/src/components/app-shell.tsx
type Props = {
  title: string;
  children: React.ReactNode;
};

export function AppShell({ title, children }: Props) {
  return (
    <div className="min-h-screen bg-slate-950 text-white">
      <header className="border-b border-white/10">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
          <div>
            <p className="text-xs uppercase tracking-[0.3em] text-cyan-300">Predict Better</p>
            <h1 className="text-xl font-semibold">{title}</h1>
          </div>
          <nav className="flex gap-3 text-sm text-slate-300">
            <span>경기</span>
            <span>리더보드</span>
            <span>프로필</span>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">{children}</main>
    </div>
  );
}
```

```tsx
// frontend/app/page.tsx
import { AppShell } from "@/components/app-shell";

export default function HomePage() {
  return (
    <AppShell title="Gachamarket">
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <section className="rounded-3xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-2xl font-semibold">카테고리 허브</h2>
          <p className="mt-2 text-sm text-slate-300">이 화면은 로그인 전에도 볼 수 있는 공개 홈이다.</p>
        </section>
        <aside className="rounded-3xl border border-white/10 bg-white/5 p-6">
          <h2 className="text-lg font-semibold">빠른 이동</h2>
          <ul className="mt-3 space-y-2 text-sm text-slate-300">
            <li>축구</li>
            <li>야구</li>
            <li>농구</li>
          </ul>
        </aside>
      </div>
    </AppShell>
  );
}
```

- [ ] **Step 4: 프론트 테스트가 통과하는지 확인**

Run: `cd frontend && pnpm test -- --runInBand app-shell`

Expected: `1 passed`

- [ ] **Step 5: 커밋**

```bash
git add frontend
git commit -m "chore: bootstrap frontend shell"
```

### Task 3: DB 베이스라인과 Category Catalog 모듈 추가

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__foundation.sql`
- Create: `backend/src/main/java/com/gachamarket/category/domain/Category.java`
- Create: `backend/src/main/java/com/gachamarket/category/application/CategoryQueryService.java`
- Create: `backend/src/main/java/com/gachamarket/category/adapter/in/web/CategoryController.java`
- Test: `backend/src/test/java/com/gachamarket/category/CategoryCatalogIntegrationTest.java`

- [ ] **Step 1: 카테고리 조회 통합 테스트 작성**

```java
package com.gachamarket.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoryCatalogIntegrationTest {

    @Autowired
    private com.gachamarket.category.application.CategoryQueryService categoryQueryService;

    @Test
    void returnsSeededSportsTree() {
        List<String> slugs = categoryQueryService.findVisibleLeafSlugs();
        assertThat(slugs).contains("soccer-epl", "baseball-kbo", "basketball-nba");
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.category.CategoryCatalogIntegrationTest`

Expected: `CategoryQueryService` bean 부재 또는 migration/table 부재로 FAIL

- [ ] **Step 3: 기초 테이블과 카테고리 모듈 작성**

```sql
-- backend/src/main/resources/db/migration/V1__foundation.sql
create table members (
    id uuid primary key,
    email varchar(255) not null unique,
    nickname varchar(32) not null unique,
    active_title_id uuid null,
    nickname_change_free_used boolean not null default false,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table wallets (
    member_id uuid primary key references members(id),
    current_point int not null,
    updated_at timestamp not null
);

create table categories (
    id uuid primary key,
    parent_id uuid null references categories(id),
    slug varchar(64) not null unique,
    name varchar(64) not null,
    depth int not null,
    sort_order int not null,
    visible boolean not null
);

insert into categories (id, parent_id, slug, name, depth, sort_order, visible) values
('00000000-0000-0000-0000-000000000001', null, 'sports', '스포츠', 0, 0, true),
('00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', 'soccer', '축구', 1, 0, true),
('00000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', 'baseball', '야구', 1, 1, true),
('00000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', 'basketball', '농구', 1, 2, true),
('00000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000011', 'soccer-epl', 'EPL', 2, 0, true),
('00000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000012', 'baseball-kbo', 'KBO', 2, 0, true),
('00000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000013', 'basketball-nba', 'NBA', 2, 0, true);
```

```java
// backend/src/main/java/com/gachamarket/category/application/CategoryQueryService.java
package com.gachamarket.category.application;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryService {

    private final JdbcTemplate jdbcTemplate;

    public CategoryQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> findVisibleLeafSlugs() {
        return jdbcTemplate.queryForList("""
            select slug
            from categories c
            where visible = true
              and not exists (select 1 from categories child where child.parent_id = c.id)
            order by sort_order
            """, String.class);
    }
}
```

- [ ] **Step 4: 마이그레이션과 조회 테스트 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.category.CategoryCatalogIntegrationTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/resources/db backend/src/main/java/com/gachamarket/category backend/src/test/java/com/gachamarket/category
git commit -m "feat: add category catalog baseline"
```

### Task 4: Google 로그인과 회원 자동 등록 구현

**Files:**
- Create: `backend/src/main/java/com/gachamarket/identity/domain/Member.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/MemberRegistrationService.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/NicknameGenerator.java`
- Create: `backend/src/main/java/com/gachamarket/identity/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/in/web/MeController.java`
- Test: `backend/src/test/java/com/gachamarket/identity/MemberRegistrationIntegrationTest.java`

- [ ] **Step 1: 신규 로그인 시 회원 자동 등록 테스트 작성**

```java
package com.gachamarket.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberRegistrationIntegrationTest {

    @Autowired
    private com.gachamarket.identity.application.MemberRegistrationService memberRegistrationService;

    @Test
    void createsMemberWithInitialNicknameAndPoints() {
        var member = memberRegistrationService.registerOrLoad("user@example.com");

        assertThat(member.email()).isEqualTo("user@example.com");
        assertThat(member.nickname()).startsWith("GM-");
        assertThat(member.currentPoint()).isEqualTo(1000);
        assertThat(member.id()).isInstanceOf(UUID.class);
    }
}
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.identity.MemberRegistrationIntegrationTest`

Expected: `MemberRegistrationService` 미구현으로 FAIL

- [ ] **Step 3: 최소 로그인/회원 모듈 구현**

```java
// backend/src/main/java/com/gachamarket/identity/application/NicknameGenerator.java
package com.gachamarket.identity.application;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {

    public String generate() {
        return "GM-" + ThreadLocalRandom.current().nextInt(100000, 999999);
    }
}
```

```java
// backend/src/main/java/com/gachamarket/identity/application/MemberRegistrationService.java
package com.gachamarket.identity.application;

import com.gachamarket.identity.domain.Member;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MemberRegistrationService {

    private final JdbcTemplate jdbcTemplate;
    private final NicknameGenerator nicknameGenerator;

    public MemberRegistrationService(JdbcTemplate jdbcTemplate, NicknameGenerator nicknameGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.nicknameGenerator = nicknameGenerator;
    }

    public Member registerOrLoad(String email) {
        var existing = jdbcTemplate.query("""
            select m.id, m.email, m.nickname, w.current_point
            from members m
            join wallets w on w.member_id = m.id
            where m.email = ?
            """, rs -> rs.next() ? new Member(
                UUID.fromString(rs.getString("id")),
                rs.getString("email"),
                rs.getString("nickname"),
                rs.getInt("current_point")) : null, email);

        if (existing != null) {
            return existing;
        }

        var member = new Member(UUID.randomUUID(), email, nicknameGenerator.generate(), 1000);
        jdbcTemplate.update("""
            insert into members (id, email, nickname, created_at, updated_at)
            values (?, ?, ?, ?, ?)
            """, member.id(), member.email(), member.nickname(), Instant.now(), Instant.now());
        jdbcTemplate.update("""
            insert into wallets (member_id, current_point, updated_at)
            values (?, ?, ?)
            """, member.id(), 1000, Instant.now());
        return member;
    }
}
```

```java
// backend/src/main/java/com/gachamarket/identity/domain/Member.java
package com.gachamarket.identity.domain;

import java.util.UUID;

public record Member(UUID id, String email, String nickname, int currentPoint) {
}
```

```java
// backend/src/main/java/com/gachamarket/identity/config/SecurityConfig.java
package com.gachamarket.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/internal/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/me").authenticated()
                .anyRequest().permitAll())
            .oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }
}
```

- [ ] **Step 4: 통합 테스트 통과 확인**

Run: `cd backend && ./gradlew test --tests com.gachamarket.identity.MemberRegistrationIntegrationTest`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add backend/src/main/java/com/gachamarket/identity backend/src/test/java/com/gachamarket/identity
git commit -m "feat: add member registration and google login skeleton"
```

### Task 5: 프론트에 세션 기반 로그인/프로필/카테고리 네비게이션 연결

**Files:**
- Create: `frontend/src/lib/api.ts`
- Create: `frontend/src/lib/session.ts`
- Create: `frontend/app/profile/page.tsx`
- Modify: `frontend/app/page.tsx`
- Test: `frontend/src/components/__tests__/home-page.test.tsx`

- [ ] **Step 1: 홈 화면이 카테고리와 로그인 CTA를 보여주는 테스트 작성**

```tsx
import { render, screen } from "@testing-library/react";
import HomePage from "@/app/page";

describe("HomePage", () => {
  it("shows category cards and login CTA", async () => {
    render(await HomePage());

    expect(screen.getByText("축구")).toBeInTheDocument();
    expect(screen.getByText("야구")).toBeInTheDocument();
    expect(screen.getByText("Google로 로그인")).toBeInTheDocument();
  });
});
```

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `cd frontend && pnpm test -- --runInBand home-page`

Expected: 카테고리 텍스트 또는 로그인 CTA 부재로 FAIL

- [ ] **Step 3: 백엔드 API 연동과 반응형 페이지 작성**

```ts
// frontend/src/lib/api.ts
export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${process.env.API_BASE_URL}${path}`, {
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`GET ${path} failed`);
  }

  return response.json() as Promise<T>;
}
```

```tsx
// frontend/app/page.tsx
import Link from "next/link";
import { AppShell } from "@/components/app-shell";

const categories = [
  { name: "축구", description: "EPL, 5대 리그, K리그1" },
  { name: "야구", description: "MLB, KBO" },
  { name: "농구", description: "NBA" },
];

export default async function HomePage() {
  return (
    <AppShell title="Gachamarket">
      <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {categories.map((category) => (
            <article key={category.name} className="rounded-3xl border border-white/10 bg-white/5 p-6">
              <h2 className="text-xl font-semibold">{category.name}</h2>
              <p className="mt-2 text-sm text-slate-300">{category.description}</p>
            </article>
          ))}
        </section>
        <aside className="rounded-3xl border border-cyan-400/30 bg-cyan-400/10 p-6">
          <h2 className="text-lg font-semibold">지금 바로 시작</h2>
          <p className="mt-2 text-sm text-slate-200">가입 시 1,000P를 받고 예측을 시작한다.</p>
          <Link href={`${process.env.API_BASE_URL}/oauth2/authorization/google`} className="mt-4 inline-flex rounded-full bg-white px-4 py-2 text-sm font-semibold text-slate-950">
            Google로 로그인
          </Link>
        </aside>
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
      <section className="rounded-3xl border border-white/10 bg-white/5 p-6">
        <h2 className="text-xl font-semibold">프로필 기본 구조</h2>
        <p className="mt-2 text-sm text-slate-300">포인트, 닉네임, 활성 칭호 영역을 이 레이아웃 안에 배치한다.</p>
      </section>
    </AppShell>
  );
}
```

- [ ] **Step 4: 프론트 테스트와 정적 빌드 확인**

Run: `cd frontend && pnpm test -- --runInBand home-page && pnpm build`

Expected: 테스트 PASS, `Compiled successfully`

- [ ] **Step 5: 커밋**

```bash
git add frontend
git commit -m "feat: add responsive home and profile shell"
```
