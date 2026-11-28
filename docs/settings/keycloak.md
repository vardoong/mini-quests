# Keycloak 설정

## 회원 연결 흐름

1. Keycloak이 사용자를 인증하고 Access Token 발급
2. mini-quests Resource Server가 토큰 서명과 Issuer 검증
3. `GET /api/users/me`가 검증된 토큰의 `iss`, `sub`를 기준으로 회원을 조회 또는 생성
4. 표시 이름은 `name`, 없으면 `preferred_username`에서 가져옴
5. 이메일은 `email`에서 가져옴

## Realm

| Realm       | 용도             |
|-------------|------------------|
| master      | keycloak 관리    |
| mini-quests | mini-quests 전용 |

## 로컬 실행

- 컨테이너 이미지: `quay.io/keycloak/keycloak:26.8.2`
- 실행 모드: `start-dev` (로컬 개발 전용)
- 관리자 콘솔: `http://127.0.0.1:8080`
- 데이터 저장: `keycloak_data` Docker 볼륨
- 관리자 계정
    - `.env`의 `KEYCLOAK_ADMIN_USERNAME`, `KEYCLOAK_ADMIN_PASSWORD`로 설정

## mini-quests 연결 설정

- 앱 주소: http://127.0.0.1:8081
- issuer-uri: http://127.0.0.1:8080/realms/mini-quests
- Keycloak discovery 문서의 issuer 값이 issuer-uri와 일치해야 함

## Test

### Test User

1. mini-quests 렐름에 테스트 사용자 생성
2. Credentials > Set password: `Temporary` off

### Test Client

| 설정                  | 값                                   |
|-----------------------|--------------------------------------|
| Client ID             | `mini-quests-local`                  |
| Client authentication | Off (Public client)                  |
| Standard flow         | On                                   |
| Direct access grants  | Off                                  |
| Implicit flow         | Off                                  |
| Require PKCE          | On, `S256`                           |
| Valid redirect URI    | `https://oauth.pstmn.io/v1/callback` |

### Test Postman (토큰 테스트)

| 설정                    | 값                                                                     |
|-------------------------|------------------------------------------------------------------------|
| Auth type               | OAuth 2.0                                                              |
| Grant type              | Authorization Code (With PKCE)                                         |
| Callback URL            | https://oauth.pstmn.io/v1/callback                                     |
| Authorize using browser | on                                                                     |
| Auth URL                | http://127.0.0.1:8080/realms/mini-quests/protocol/openid-connect/auth  |
| Access Token URL        | http://127.0.0.1:8080/realms/mini-quests/protocol/openid-connect/token |
| Client ID               | `mini-quests-local`                                                    |
| Code Challenge Method   | SHA-256                                                                |
| Scope                   | `openid profile email`                                                 | 
