# Gachamarket MVP Implementation Plan Index

다음 순서대로 실행한다.

1. [Foundation and Auth](/Users/zunza/Documents/project/gachamarket/docs/superpowers/plans/2026-03-29-gachamarket-foundation-and-auth.md)
   - 저장소 골격
   - 백엔드/프론트 초기 실행
   - Google 로그인
   - 회원/카테고리 셸

2. [Backend Architecture Refactor](/Users/zunza/Documents/project/gachamarket/docs/superpowers/plans/2026-03-30-gachamarket-backend-architecture-refactor.md)
   - 기능별 루트 유지
   - application port/service/dto 분리
   - domain model 과 JPA entity 분리
   - 계층별 테스트 전략 정리

3. [Prediction Pipeline](/Users/zunza/Documents/project/gachamarket/docs/superpowers/plans/2026-03-29-gachamarket-prediction-pipeline.md)
   - 경기 수집
   - 오즈 계산
   - 승인 큐
   - 공개 이벤트 목록/상세

4. [Gameplay, Community, and Rank](/Users/zunza/Documents/project/gachamarket/docs/superpowers/plans/2026-03-29-gachamarket-gameplay-community-and-rank.md)
   - 포인트 지갑
   - 베팅
   - 자동 정산
   - 칭호/리더보드
   - 댓글/좋아요/대댓글

실행 기준:

- 각 문서는 독립적으로 테스트 가능한 상태를 목표로 한다.
- 다음 문서로 넘어가기 전에 앞 문서의 테스트와 수동 점검을 먼저 통과시킨다.
- 구현은 항상 문서 안의 태스크 순서를 따른다.
