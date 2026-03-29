# Gachamarket Backend Architecture Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 기능별 루트를 유지하면서 백엔드를 `domain`, `application`, `adapter` 경계로 재정렬하고 계층별 테스트 전략을 적용한다.

**Architecture:** 각 기능 아래에 `application/service`, `application/port`, `application/dto/{command,query,result}`, `adapter/in/web/{request,response}`, `adapter/out`, `domain` 패키지를 둔다. application은 out port를 통해서만 영속성에 접근하고, JPA entity는 persistence adapter 내부로 제한한다. domain은 Lombok 기반 class로 두고 DTO는 record로 유지한다.

**Tech Stack:** Java 21, Spring Boot, Spring Data JPA, Flyway, JUnit 5, Mockito, Testcontainers

---

## Planned File Structure

- `backend/src/main/java/com/gachamarket/category/application/...`: 카테고리 조회 use case, result DTO, port
- `backend/src/main/java/com/gachamarket/category/adapter/out/persistence/...`: 카테고리 JPA entity, repository, adapter
- `backend/src/main/java/com/gachamarket/category/adapter/in/web/response/...`: 카테고리 응답 DTO
- `backend/src/main/java/com/gachamarket/category/domain/...`: 카테고리 순수 도메인 class
- `backend/src/main/java/com/gachamarket/identity/application/...`: 회원 등록 use case, command/result DTO, port
- `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/...`: 회원/지갑 JPA entity, repository, adapter
- `backend/src/main/java/com/gachamarket/identity/adapter/in/web/response/...`: 회원 응답 DTO
- `backend/src/main/java/com/gachamarket/identity/domain/...`: 회원/지갑 순수 도메인 class
- `backend/src/test/java/com/gachamarket/...`: domain/application/adapter 책임에 맞는 테스트
- `AGENTS.md`: 백엔드 계층 규칙과 테스트 기준 반영

### Task 1: 계층 경계를 고정하는 테스트 추가

**Files:**
- Create: `backend/src/test/java/com/gachamarket/category/domain/CategoryTest.java`
- Create: `backend/src/test/java/com/gachamarket/category/application/service/CategoryQueryServiceTest.java`
- Create: `backend/src/test/java/com/gachamarket/identity/domain/MemberTest.java`
- Create: `backend/src/test/java/com/gachamarket/identity/domain/WalletTest.java`
- Create: `backend/src/test/java/com/gachamarket/identity/application/service/MemberRegistrationServiceTest.java`
- Create: `backend/src/test/java/com/gachamarket/category/adapter/in/web/CategoryControllerIntegrationTest.java`

- [ ] **Step 1: domain/application/adapter 기대 동작을 표현하는 테스트를 작성한다**
- [ ] **Step 2: 새 테스트를 선택 실행해 실제로 실패하는지 확인한다**

Run: `cd backend && ./gradlew test --tests com.gachamarket.category.domain.CategoryTest --tests com.gachamarket.identity.application.service.MemberRegistrationServiceTest --tests com.gachamarket.category.adapter.in.web.CategoryControllerIntegrationTest`

Expected: 컴파일 오류 또는 빈/클래스 부재로 FAIL

### Task 2: category를 port/service/dto + persistence adapter 구조로 이동

**Files:**
- Modify: `backend/src/main/java/com/gachamarket/category/adapter/in/web/CategoryController.java`
- Create: `backend/src/main/java/com/gachamarket/category/application/dto/result/CategoryLeafResult.java`
- Create: `backend/src/main/java/com/gachamarket/category/adapter/in/web/response/CategoryLeafResponse.java`
- Create: `backend/src/main/java/com/gachamarket/category/application/port/in/GetVisibleLeafCategoriesUseCase.java`
- Create: `backend/src/main/java/com/gachamarket/category/application/port/out/LoadCategoryPort.java`
- Create: `backend/src/main/java/com/gachamarket/category/application/service/CategoryQueryService.java`
- Create: `backend/src/main/java/com/gachamarket/category/adapter/out/persistence/CategoryJpaEntity.java`
- Create: `backend/src/main/java/com/gachamarket/category/adapter/out/persistence/CategoryJpaRepository.java`
- Create: `backend/src/main/java/com/gachamarket/category/adapter/out/persistence/CategoryPersistenceAdapter.java`
- Modify: `backend/src/main/java/com/gachamarket/category/domain/Category.java`

- [ ] **Step 1: failing test를 통과시키는 최소 category domain/application 구조를 작성한다**
- [ ] **Step 2: JPA entity와 repository를 추가하고 persistence adapter에서 domain 변환을 처리한다**
- [ ] **Step 3: 컨트롤러가 in port를 호출하도록 바꾸고 통합 테스트를 통과시킨다**

### Task 3: identity를 port/service/dto + persistence adapter 구조로 이동

**Files:**
- Create: `backend/src/main/java/com/gachamarket/identity/application/dto/command/RegisterMemberCommand.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/dto/result/RegisteredMemberResult.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/port/in/RegisterMemberUseCase.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/port/out/LoadMemberPort.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/port/out/SaveMemberPort.java`
- Create: `backend/src/main/java/com/gachamarket/identity/application/service/MemberRegistrationService.java`
- Modify: `backend/src/main/java/com/gachamarket/identity/application/NicknameGenerator.java`
- Modify: `backend/src/main/java/com/gachamarket/identity/domain/Member.java`
- Create: `backend/src/main/java/com/gachamarket/identity/domain/Wallet.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/in/web/response/MeResponse.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/MemberJpaEntity.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/WalletJpaEntity.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/MemberJpaRepository.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/WalletJpaRepository.java`
- Create: `backend/src/main/java/com/gachamarket/identity/adapter/out/persistence/MemberPersistenceAdapter.java`

- [ ] **Step 1: failing test를 통과시키는 최소 identity domain/application 구조를 작성한다**
- [ ] **Step 2: 영속성 로직을 adapter로 이동하고 JPA entity와 domain model을 분리한다**
- [ ] **Step 3: application 테스트와 전체 backend 테스트를 다시 실행해 회귀가 없는지 확인한다**

### Task 4: 저장소 운영 규칙 업데이트

**Files:**
- Modify: `AGENTS.md`

- [ ] **Step 1: 기능별 루트, 계층 의존성, entity 분리, 계층별 테스트 규칙을 문서화한다**
- [ ] **Step 2: 백엔드 전체 테스트를 실행해 문서와 코드 상태가 일치하는지 검증한다**

Run: `cd backend && ./gradlew test`

Expected: `BUILD SUCCESSFUL`
