# DBP_BE

CampusMarket 백엔드 레포지토리입니다. (Spring 미사용: Servlet + JDBC)

<br />

## 🛠 기술 스택

| 구분           | 기술                                                         |
| -------------- | ------------------------------------------------------------ |
| Language       | Java 21                                                      |
| Server         | Apache Tomcat 11 (Jakarta Servlet 6)                         |
| Build Tool     | Maven                                                        |
| DB             | Oracle 12cR2 (학과 서버 `dblab.dongduk.ac.kr`, 팀 계정)      |
| DB 접근        | JDBC + `JDBCUtil` (DBCP2 커넥션 풀, 강의 예제)               |
| JSON           | Jackson (React 프론트와 REST 통신)                           |
| Code Quality   | Spotless (google-java-format, AOSP) + Checkstyle             |
| Git Convention | Git hooks (`.githooks`: commit-msg, pre-commit)              |
| Test           | JUnit 5                                                      |
| IDE            | IntelliJ IDEA                                                |

<br />

## 🚀 시작하기

### 사전 준비

- JDK **21**
- IntelliJ IDEA (Maven 내장)
- 학교 네트워크 또는 VPN (학과 DB 서버 접속용)

### 설치 및 실행

```bash
git clone https://github.com/DBP2/DBP_BE.git
cd DBP_BE
git checkout develop

git config core.hooksPath .githooks   # 커밋 규칙 훅 설정 (clone 후 1회, 필수)
```

1. **DB 접속 정보 설정**
   `src/main/resources/dbinfo.properties.example` 을 복사해 같은 폴더에 **`dbinfo.properties`** 로 저장하고 `db.password` 를 채웁니다.

   ```bash
   cp src/main/resources/dbinfo.properties.example src/main/resources/dbinfo.properties
   # Windows PowerShell: copy src\main\resources\dbinfo.properties.example src\main\resources\dbinfo.properties
   ```

   > ⚠️ `dbinfo.properties` 는 `.gitignore` 에 등록되어 있습니다. **절대 커밋하지 마세요.**

2. **IntelliJ 로 열기**
   File → Open → `pom.xml` 선택 → **Open as Project** → 우측 Maven 창에서 🔄 Reload
   (File → Project Structure → SDK 를 **21** 로 설정)

### 명령어

터미널에 `mvn` 이 없으면 IntelliJ 우측 **Maven 창 → Plugins / Lifecycle** 에서 같은 goal 을 더블클릭해 실행합니다.

| 명령어                           | 설명                                     |
| -------------------------------- | ---------------------------------------- |
| `mvn spotless:apply`             | 포맷 적용                                |
| `mvn spotless:check`             | 포맷 검사                                |
| `mvn checkstyle:check`           | Lint 검사                                |
| `mvn test`                       | 전체 테스트 (DB 테스트 포함, 학교망 필요) |
| `mvn test -DexcludedGroups=db`   | DB 연결 없이 가능한 테스트만 실행        |
| `mvn package`                    | 빌드 (`target/campusmarket.war`)          |

> 💡 IntelliJ 는 `.editorconfig` 를 자동 적용합니다. 저장 시 포맷까지 맞추려면
> Settings → Plugins 에서 **google-java-format** 설치 후 활성화(스타일 AOSP)하세요.

<br />

## 📁 폴더 구조

강의 예제 `StudentManagement` 의 패키지 구조를 따릅니다.
(과제 지침에 따라 **DAO 인터페이스는 생략**하고 클래스만 정의합니다.)

```
src/main/java/
├── controller/          # Servlet (요청 URI 처리, JSON 응답)
├── domain/              # 도메인 클래스 (클래스 다이어그램 기준, DTO로도 사용)
├── persistence/
│   ├── dao/             # DAO 클래스 (SQL 실행 → 객체 변환)
│   └── util/
│       └── JDBCUtil.java  # 강의 예제 그대로 사용
└── service/             # Manager(Service) 클래스 (여러 DAO 호출, 트랜잭션, 비즈니스 로직)
    └── dto/             # 도메인으로 표현 못 하는 조회 결과/요청 데이터 DTO
src/main/resources/
├── dbinfo.properties.example   # DB 접속 정보 양식 (실제 파일은 커밋 금지)
└── logback.xml
src/test/java/           # 테스트 (JUnit 5)
```

<br />

## 🧩 코드 규칙

### 이름 규칙

| 종류       | 규칙                                | 예시                                 |
| ---------- | ----------------------------------- | ------------------------------------ |
| 도메인     | 단수 명사                           | `SafeZone`, `ChatRoom`               |
| DAO        | `도메인명DAO`                       | `SafeZoneDAO`, `ChatMessageDAO`      |
| Manager    | `도메인그룹Manager`                 | `SafeZoneManager`, `ChatManager`     |
| DTO        | 용도가 드러나게 (`DTO` 접미사 선택) | `ChatRoomSummary`, `TradeSzStatus`   |
| Controller | `동작+대상+Controller`              | `ListChatRoomController`             |
| 테스트     | `대상클래스Test`                    | `SafeZoneDAOTest`                    |

### DB 규칙

- SQL 의 테이블/컬럼명은 DBP_DOCS 레포 `database/campusmarket_ddl.ddl` 과 **동일하게** 씁니다.
- 테이블/컬럼 변경이 필요하면 **DBP_DOCS 의 Data Modeler 모델을 먼저 수정**하고 DDL 을 다시 생성한 뒤 PR 에 명시합니다.
- PK 값은 시퀀스로 생성합니다. 예) `SEQ_CHAT_ROOM.NEXTVAL`
- 여러 DML 을 묶는 기능은 Manager 에서 트랜잭션(commit / rollback)으로 처리합니다.

### 테스트 규칙

- DB 에 실제로 접속하는 테스트는 클래스에 **`@Tag("db")`** 를 붙입니다. (CI 에서는 학과 DB 에 접속할 수 없어 제외됩니다)

```java
@Tag("db")
class SafeZoneDAOTest {
    @Test
    void findSafeZoneList() { ... }
}
```

<br />

## 🤝 협업 규칙

### 이슈 규칙

- 작업 시작 전 **이슈 먼저 생성**
- 이슈 제목 형식: `[FEAT] 기능명` / `[FIX] 버그명`
- 이슈 템플릿(`.github/ISSUE_TEMPLATE`) 사용
  - 기능 요청 → `feature.md`
  - 버그 리포트 → `bug.md`

### 브랜치 전략

| 브랜치                  | 설명                            |
| ----------------------- | ------------------------------- |
| `main`                  | 배포 브랜치. **직접 push 금지** |
| `develop`               | 개발 통합 브랜치                |
| `feat/#이슈번호-기능명` | 기능 개발                       |
| `fix/#이슈번호-버그명`  | 버그 수정                       |

예시: `feat/#12-safezone-dao` / `fix/#34-chat-room-query`

```bash
git checkout develop
git pull origin develop
git checkout -b "feat/#12-safezone-dao"   # PowerShell 에서는 # 때문에 따옴표 권장
```

작업 기간이 길어져 develop과 차이가 벌어지면, **PR을 올리기 전 develop을 작업 브랜치에 병합**해 충돌을 미리 해결합니다.

```bash
git checkout "feat/#12-safezone-dao"
git pull origin develop   # 충돌 해결 후 커밋
```

### 커밋 컨벤션

형식: `타입: 제목`

| 타입       | 설명                      |
| ---------- | ------------------------- |
| `feat`     | 새로운 기능               |
| `fix`      | 버그 수정                 |
| `style`    | 코드 포맷 (기능 변화 없음) |
| `refactor` | 리팩토링                  |
| `chore`    | 설정, 의존성 등 기타 변경 |
| `docs`     | 문서 수정                 |
| `test`     | 테스트 추가/수정          |
| `revert`   | 커밋 되돌리기             |

예시: `feat: SafeZoneDAO 조회 메소드 구현` / `fix: 채팅 목록 정렬 오류 수정`

> ⚠️ 커밋 형식이 맞지 않으면 **자동으로 커밋이 막힙니다.** (`.githooks/commit-msg`)
> 커밋 시 변경된 Java 파일에 포맷(Spotless)이 자동 적용됩니다. (`.githooks/pre-commit`, `mvn` 이 있을 때)
> 훅이 동작하지 않으면 `git config core.hooksPath .githooks` 를 실행했는지 확인하세요.

### PR 규칙

- PR 제목은 커밋 컨벤션과 동일한 형식
- **이슈 없이 PR 금지** (`closes #이슈번호` 필수)
- PR 템플릿(`.github/pull_request_template.md`) 양식에 맞춰 작성
- `feat` / `fix` 브랜치 → `develop` 으로 PR
- `develop` → `main` 은 배포 시점에만 머지
- PR 승인 **1명 이상** 필수
- PR의 모든 리뷰 댓글 resolve 후 머지
- 리뷰 요청 후 팀원들에게 공지
- PR 생성 시 CI가 자동으로 **PR 제목, 포맷, 린트, 테스트, 빌드**를 검증

### 소통 및 회의 규칙

- 작업 완료 시 팀 채널에 공유
- 작업 지연이나 이슈 발생 시 즉시 공유
- API 추가/변경 시 FE 담당자에게 공유 (요청 URI, 파라미터, 응답 JSON 형식)
