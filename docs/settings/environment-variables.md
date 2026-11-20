# 환경변수

| 변수 | 값 |
| --- | --- |
| POSTGRES_DB | PostreSQL DB 명 |
| POSTGRES_USER | PostgreSQL 사용자명|
| POSTGRES_PASSWORD | PostgreSQL 비밀번호 |
| POSTGRES_PORT | PostgreSQL 포트<br/>없으면 기본 포트(5432) 사용 |

---

### 참고
현재 환경변수 확인 (비밀번호 제외)
```shell
for v in POSTGRES_DB POSTGRES_USER POSTGRES_PORT; do echo "$v=$(printenv $v)"; done
```
