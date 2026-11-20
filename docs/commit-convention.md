# Commit Convention
### Type
- `feat:` 새로운 피처
- `fix:` 버그 수정
- `refactor:` 기능추가나 오류수정 없이, 내부 구조 개선
- `perf:` 성능 개선
- `test:` 테스트 추가, 수정
- `docs:` 문서 변경
- `build:` 빌드 시스템, 의존성, 패키징 변경
- `ci:` ci/cd 파이프라인 변경
- `chore:` 다른 타입에 해당하지 않는 유지보수나 설정
- `style:` 동작 변화 없는 포맷이나 공백 정리
- `revert:` 이전 변경 되돌리기
### Scope
1. type 뒤에 이어서 괄호안에 붙여줌
2. 코드의 업무 영역에 대해 설명
3. 예시: `feat(reservation):`
### Content
1. type(scope): 이후에 작업에 대해 한줄 설명
2. 추가 설명이 필요한 경우 첫줄 이후 빈 줄을 하나 두고 이어서 설명한다
3. 예시:
```text
feat(reservation): 예약 상태 변경

예약을 확인한 경우 예약 상태를 자동으로 변경해 준다.
[확인중] -> [예약됨]
```