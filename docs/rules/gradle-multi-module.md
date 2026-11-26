# 멀티모듈 구조와 규칙

## 목적

- 모듈별로 명확하게 의존성을 관리함
- 기능별로 코드를 분리해 구조를 보기좋게 함

## 적용 이유

- 기능 경계와 의존성을 명시적으로 관리
- 향후 서비스 분리 대상과 필요한 경계를 검토하는데 활용
- 마이크로서비스로 확장하기 쉽도록 함 (모듈 경계가 없는 단일 애플리케이션 대비)

## 구조

### 모듈형 모놀리스

여러 기능 모듈을 하나의 애플리케이션으로 조립함

### 구조도

```text
mini-quests/
├─ app/         # Spring boot 실행 모듈
│  ├─ src
│  └─ build.gradle
├─ modules/
│  ├─ <feature-module>/
│  │  ├─ src
│  │  └─ build.gradle
│  ...
├─ docs/        # GitHub pages 문서들
├─ build.gradle 
└─ settings.gradle
```

## 모듈 역할

### 루트 프로젝트

- `/build.gradle` 플러그인 버전 관리, 공통 모듈 설정 관리
- `settings.gradle` 앱과 기능 모듈 등록

### app

- `java`, `Spring Boot`, `Dependency Management` 플러그인 적용
- API 표현 계층, 보안 및 애플리케이션 설정
- 기능 모듈을 Gradle 프로젝트 의존성으로 연결

### 기능 모듈

- `java-library`, `Dependency Management` 플러그인 적용
- 담당 기능의 도메인, 애플리케이션 로직, 저장소 등 내부 코드 작성
- 해당 기능의 DB 마이그레이션 파일 관리
- 독립 실행 애플리케이션은 아님
    - Spring Boot 실행 플러그인은 적용하지 않음

## 공통 빌드 규칙

- Java 기반 하위 프로젝트는 Java 21 toolchain을 사용함
- toolchain은 루트 `build.gradle`에서 공통 설정하므로 각 모듈의 `build.gradle`에 별도로 선언하지 않음

## 의존성 규칙

- 기본 의존 방향: `app` -> `기능 모듈`
    - 기능 모듈은 `app`에 의존하지 않음
- 기능 모듈 사이에는 필요한 경우만 의존성을 추가하고, 순환 의존성을 만들지 않음

## 기능 모듈 추가 순서

1. `modules/<feature-module>/` 디렉터리와 모듈의 `build.gradle` 생성
2. `settings.gradle`에 해당 하위 프로젝트 등록
3. `app`에서 사용할 모듈이면 `app/build.gradle`에 프로젝트 의존성 추가
    - 예: `app`에서 `user` 모듈 사용
        ```groovy
        implementation project(':modules:user')
        ```
4. 기능 코드를 작성 또는 이동하고 모듈 사이의 의존 방향 확인
