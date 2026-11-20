# PostgreSQL 설정

## 로컬에서 실행하는 방법

### 1. 필요한 환경
- Docker
- Docker Compose

### 2. 환경 변수 파일
```shell
cp .env.example .env
```
- 복사 후 `.env`파일 내용 변경
- `.env`파일은 `.gitignore`에 포함

### 3. PostgreSQL 실행 및 상태확인
```shell
docker compose config -q
docker compose up -d postgres
docker compose ps
docker compose logs postgres
```
- `docker compose ps`에서 `healthy` 상태가 될 때까지 대기

### 4. DB 연결 확인
```shell
docker compose exec postgres sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "select version();"'
```

### 5. 중지 및 데이터 초기화
```shell
docker compose down     # 컨테이너 중지 및 삭제 (데이터 유지)
docker compose down -v  # ⚠️ 볼륨까지 삭제 (데이터 전체 삭제)
```

---

## DB 에 저장된 데이터
- `compose.yml` 파일에서 named volume `postgres_data`를 컨테이너 `/var/lib/postgresql`에 마운트 함.
  - `postgres_data` : Docker가 관리하는 볼륨 (실제 이름: `<프로젝트명>_postgres_data`)
  - `/var/lib/postgresql/18/docker` : PostgreSQL이 데이터를 저장하는 경로
    - 이미지 버전을 올린다면 데이터를 저장하는 경로가 변경되었는지 확인해야 함.
- `docker compose down` 명령으로 컨테이너를 삭제해도 데이터는 유지되고 `-v` 옵션을 쓸 때만 삭제됨.
- 볼륨 이름 확인:
    ```shell
    docker volume ls | grep postgres_data
    ```
- 볼륨의 경로 확인
    ```shell
    docker volume inspect <위에서 확인한 볼륨 이름>
    ```