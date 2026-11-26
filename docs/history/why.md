# 설명이 필요해

## build.gradle (:app)

DB 연결만 확인하는데 jdbc 의존성을 추가하지 않고, jpa 추가한 이유?

- 어차피 jpa 사용할거고, jpa 에 jdbc 가 포함되어 있어서.
    - jpa 내부적으로 jdbc 사용
- 굳이굳이 jdbc 만으로 연결확인하고, 나중에 다시 바꿀 필요까진 없어서.

## mkdocs theme

dracula -> material 변경 이유?

- dracula theme 사용하니까, list 요소 하위의 코드블록이 깨짐 (inline code 처리됨)
- material 로 theme 변경하고 features 통해서 정상화 함
