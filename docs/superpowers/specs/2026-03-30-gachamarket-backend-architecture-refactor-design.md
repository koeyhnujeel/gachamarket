# Gachamarket Backend Architecture Refactor Design

작성일: 2026-03-30
상태: 승인됨
범위: Foundation 단계 백엔드 계층 구조 및 테스트 전략 리팩터링

## 1. 목적

이 문서는 현재 Foundation 단계에서 생성된 백엔드 코드를 기능별 루트 기반 헥사고날 구조로 재정렬하기 위한 세부 규칙을 고정한다. 목표는 의존 방향, 영속성 경계, 테스트 책임을 명확히 해 이후 기능 추가 시 application과 domain이 프레임워크 세부사항에 오염되지 않게 만드는 것이다.

## 2. 적용 범위

- `identity`
- `category`
- 백엔드 테스트 패키지 구조
- 저장소 운영 규칙 문서(`AGENTS.md`)

이번 변경은 기능 추가가 아니라 구조 정리다. 기존 API 범위와 데이터 스키마는 유지한다.

## 3. 패키지 구조 규칙

각 기능은 기능별 루트를 유지한다.

예시:

- `com.gachamarket.identity.domain`
- `com.gachamarket.identity.application.service`
- `com.gachamarket.identity.application.port.in`
- `com.gachamarket.identity.application.port.out`
- `com.gachamarket.identity.application.dto.command`
- `com.gachamarket.identity.application.dto.query`
- `com.gachamarket.identity.application.dto.result`
- `com.gachamarket.identity.adapter.in.web`
- `com.gachamarket.identity.adapter.in.web.request`
- `com.gachamarket.identity.adapter.in.web.response`
- `com.gachamarket.identity.adapter.out.persistence`

`category`도 같은 규칙을 따른다.

## 4. 의존성 규칙

- `adapter -> application, domain`
- `application -> domain`
- `domain -> nothing`

추가 규칙:

- `application`은 `JdbcTemplate`, `JpaRepository`, JPA annotation에 직접 의존하지 않는다.
- 영속성 기술 선택과 매핑은 `adapter.out.persistence` 안에서만 처리한다.
- 웹 요청/응답 처리는 `adapter.in.web`에서 끝내고, application에는 use case와 DTO만 노출한다.

## 5. 도메인 모델과 JPA 엔티티 분리

- domain 모델은 Lombok 기반 순수 자바 class로 유지한다.
- application/adapter DTO는 Java record로 유지한다.
- JPA entity는 `adapter.out.persistence`에 두고 이름은 `*JpaEntity`로 맞춘다.
- domain 모델은 비즈니스 의미를 표현하고, DB 컬럼 구조를 직접 드러내지 않는다.
- JPA entity와 domain 모델 간 변환은 persistence adapter 내부 책임으로 둔다.

초기 적용 대상:

- `Category` domain model / `CategoryJpaEntity`
- `Member`, `Wallet` domain model / `MemberJpaEntity`, `WalletJpaEntity`

## 6. application 계층 구조

application 계층은 다음 하위 패키지로 나눈다.

- `service`: use case 구현
- `port.in`: adapter가 호출하는 use case 계약
- `port.out`: 외부 자원 접근 계약
- `dto.command`: 입력이 있는 쓰기 use case 요청
- `dto.query`: 입력이 있는 읽기 use case 요청
- `dto.result`: application이 반환하는 결과

입력이 없는 조회 use case는 빈 query record를 만들지 않고 메서드 인자를 생략한다.

초기 use case:

- Category visible leaf 조회
- Member 조회 또는 신규 등록

## 7. 테스트 전략

### 7.1 domain

- 순수 단위 테스트
- Spring context 사용 금지
- 도메인 규칙과 상태 계산을 검증

### 7.2 application

- mocking 기반 단위 테스트
- out port와 생성기 같은 협력 객체는 Mockito로 대체
- 서비스가 도메인 규칙과 port 호출 순서를 올바르게 수행하는지 검증

### 7.3 adapter

- 컨트롤러는 `@SpringBootTest + Testcontainers` 통합 테스트 사용
- HTTP 응답과 DB 상태를 함께 검증
- Flyway 마이그레이션을 포함한 실제 영속성 경로를 사용

## 8. 완료 조건

- `identity`, `category`가 새 패키지 규칙을 따른다.
- application 계층에서 직접 DB 접근 코드가 제거된다.
- domain 모델과 JPA entity가 분리된다.
- adapter/application/domain 테스트가 각 계층 규칙에 맞게 재배치된다.
- `AGENTS.md`에 백엔드 패키지/의존성/테스트 규칙이 반영된다.
