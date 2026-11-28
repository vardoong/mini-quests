# mini-quests

필요한 만큼만 한단계씩 확장해 나가는 저장소

## 문서보기

https://vardoong.github.io/mini-quests/

## 기술 스택

| 분야     | 기술                              |
|----------|-----------------------------------|
| Language | java 21                           |
| Build    | Gradle                            |
| Server   | Spring Boot 4.1.2                 |
| DB       | PostgreSQL                        |
| ORM      | Spring Data JPA<br/>Hibernate     |
| API 명세 | springdoc Open API<br/>Swagger UI |
| CI       | GitHub Actions                    |

## 로컬 개발 환경

- git hooks 설정

    - 저장소를 clone한 뒤 한 번만 실행하세요.

        ```sh
        ./gradlew installGitHooks
        ```

    - 이후 `git push` 할 때마다 `./gradlew build`가 실행되고, 실패하면 push가 중단됩니다.
    - Windows PowerShell/CMD에서는 `.\gradlew installGitHooks`를 사용하세요.

- [환경변수](https://vardoong.github.io/mini-quests/settings/environment-variables/)
- [PostgreSQL](https://vardoong.github.io/mini-quests/settings/postgres/)
