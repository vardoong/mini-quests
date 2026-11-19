# 멀티모듈 적용
## 적용 이유
- 모듈별로 명확하게 의존성을 관리함
- 기능별로 코드를 분리해 구조를 보기좋게 함
- 마이크로서비스로 확장하기 쉽도록 함

## 구조
```text
mini-quests/
├─ app/         # Spring boot 실행 모듈
│  ├─ src
│  └─ build.gradle
├─ modules/
│  ├─ example01-module/
│  │  ├─ src
│  │  └─ build.gradle
│  ├─ example02-module/
│  │  ├─ src
│  │  └─ build.gradle
│  ...
├─ docs/        # GitHub pages 문서들
├─ build.gradle 
└─ settings.gradle
```

## 역할
- `/build.gradle` 플러그인 버전 관리, 공통 모듈 설정 관리
- `/app/build.gradle`, `/modules/*/build.gradle` Java, Spring Boot 플러그인과 앱 의존성 관리
- `/settings.gradle` 하위 프로젝트 등록