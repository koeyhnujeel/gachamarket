# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 프로젝트 개요

Gachamarket은 스포츠 예측 플랫폼(축구/EPL, 야구/KBO, 농구/NBA)입니다. Spring Boot 백엔드와 Next.js 프론트엔드로 구성된 모노레포입니다.

## 명령어

### 백엔드 (`backend/` 디렉토리에서 실행)

```bash
./gradlew test                    # 전체 테스트 실행
./gradlew bootRun                 # 앱 실행 (기본 local 프로필, Docker Compose로 PostgreSQL 시작)
./gradlew test --tests "com.gachamarket.identity.domain.MemberTest"  # 특정 테스트 클래스만 실행
```

### 프론트엔드 (`frontend/` 디렉토리에서 실행)

```bash
pnpm test          # 테스트 실행 (vitest run)
pnpm dev           # 개발 서버 실행
pnpm build         # 프로덕션 빌드
pnpm lint          # ESLint 실행
```

## 아키텍처

### 백엔드 — 헥사고날 / 포트 & 어댑터

각 비즈니스 모듈(`identity`, `category`)은 `com.gachamarket` 하위의 기능 루트 패키지이며, 다음 구조를 따릅니다:

```
com.gachamarket/
  shared/                          # 여러 모듈이 공통으로 사용하는 도메인 개념
                                   # 공통 VO, 기본 클래스, 공통 예외 등
  support/                         # 인프라/기술 지원
                                   # 공통 설정, 웹 예외 핸들러, 유틸리티 등
  <module>/                        # 비즈니스 모듈 (identity, category, ...)
    domain/                        # 순수 Java, 프레임워크 의존성 없음
                                   # Domain Entity(Aggregate), VO, Repository 포함
    application/
      service/                     # 유스케이스 구현
      port/in/                     # 유스케이스 계약 (어댑터가 호출)
      port/out/                    # 외부 자원 계약 (어댑터가 구현)
      dto/command/                 # 쓰기 요청 DTO (Java record)
      dto/query/                   # 읽기 요청 DTO (Java record)
      dto/result/                  # 응답 DTO (Java record)
    adapter/
      in/
        web/                       # REST 컨트롤러
        dto/request                # 요청 객체 (Java record)
        dto/response               # 응답 객체 (Java record)
      out/persistence/             # JPA 엔티티(*JpaEntity), Spring Data 리포지토리, 영속성 어댑터
```

의존성 방향: `adapter → application → domain`. 도메인은 의존성이 없습니다.

핵심 규칙:
- 도메인 모델은 Lombok 기반 순수 Java 클래스이며, JPA 엔티티와 분리됩니다
- JPA 엔티티(`*JpaEntity`)는 `adapter.out.persistence`에만 존재합니다
- 도메인 모델과 JPA 엔티티 간 변환은 영속성 어댑터의 책임입니다
- 애플리케이션 계층은 JPA 어노테이션, `JdbcTemplate`, `JpaRepository`에 직접 접근하지 않습니다
- 모든 식별자는 `Long` 타입입니다

### 프론트엔드 — Next.js App Router

페이지는 `frontend/src/app/`, 공유 컴포넌트는 `frontend/src/components/`, 유틸리티는 `frontend/src/lib/`에 위치합니다. 경로 별칭 `@/`는 `./src/`를 가리킵니다.

### 데이터베이스

PostgreSQL 17, Docker Compose(`backend/compose.yaml`)로 관리합니다. Flyway 마이그레이션은 `backend/src/main/resources/db/migration/`에 있습니다. JPA `ddl-auto`는 `validate` — 스키마 변경은 반드시 Flyway를 통해서만 수행합니다.

## 테스트

### 백엔드

| 계층 | 전략 | 세부 사항 |
|---|---|---|
| `domain` | 순수 단위 테스트 | Spring 컨텍스트 없음, 목 없음 — 비즈니스 규칙과 상태 테스트 |
| `application` | Mockito 단위 테스트 | 아웃 포트와 협력 객체를 목킹하고 오케스트레이션을 검증 |
| `adapter/in/web` | 통합 테스트 | `@SpringBootTest` + Testcontainers + `MockMvc` — HTTP + DB 함께 테스트 |
| `adapter/out/persistence` | 영속성 매핑 테스트 | JPA 엔티티 ↔ 도메인 모델 변환 검증 |
| 아키텍처 | `ModulithArchitectureTests` | 모듈 경계나 패키지 구조가 변경될 때 실행 |

DB 정리 방식: 수동 정리보다 `@Transactional` 롤백을 우선합니다.

### 프론트엔드

Vitest + React Testing Library + jsdom. 컴포넌트와 같은 디렉토리의 `__tests__/` 폴더에 테스트를 작성합니다.

## 커밋 규칙

- 커밋 전 `git diff --staged`로 변경 범위를 확인합니다
- 모듈별로 커밋합니다 (한 커밋에 여러 모듈을 섞지 않습니다)
- 형식: `type(module): summary` (한국어 요약, 72자 이내)
- 본문 필수: `- reason and details` 형식
- 타입: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`
- `.gitignore`에 등록된 파일은 절대 강제로 추가하지 않습니다

## 검증

- 영향 범위가 불확실하면 → `backend/`에서 `./gradlew test` 실행
- 단일 모듈 변경 시 → 해당 모듈 테스트 + 관련 테스트 실행
- 패키지 구조나 모듈 경계 변경 시 → `ModulithArchitectureTests`도 함께 실행
- 설정이나 환경 변경 시 → `compose.yaml`과 애플리케이션 설정 파일 검토
