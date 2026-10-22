# Spring Boot 스터디룸 예약 서비스

스터디룸 조회와 시간대별 예약, 예약 취소를 제공하는 Spring Boot 기반 서비스입니다. 사용자는 원하는 시간의 스터디룸을 예약하고, 관리자는 공간 정보와 예약 현황을 관리할 수 있도록 개발하고 있습니다.

동일 시간대의 중복 예약 방지와 사용자별 접근 권한을 핵심 요구사항으로 다룹니다. 예약 데이터의 정합성을 검증하고, 반복 조회에 대한 캐싱과 쿼리 개선 효과를 측정할 계획입니다.

## 현재 진행 상태

**초기 실행 환경 구성 완료 · 서비스 기능 개발 예정**

- Spring Initializr 기반 프로젝트 생성
- Spring Boot 및 MySQL 연결 설정
- 로컬 애플리케이션 실행 및 임시 Health API 확인
- `/health` 인증 없이 접근 허용, 나머지 요청은 인증 요구

현재 로컬 환경에서 애플리케이션 실행과 Health API 응답을 확인했습니다. 회원·스터디룸·예약 기능은 구현 예정이며, 자동화 테스트 및 성능 검증 결과는 구현 진행에 따라 추가합니다.

## 기술 구성

| 구분 | 현재 구성 |
| --- | --- |
| 언어 | Java 25 |
| 프레임워크 | Spring Boot 4.1.1 |
| 빌드 | Gradle 9.7.1 Wrapper · Groovy DSL |
| 웹 | Spring Web MVC |
| 데이터 접근 | Spring Data JPA · MySQL Driver |
| 데이터베이스 | MySQL 8.4 LTS — 프로젝트 선정 버전 |
| 보안 | Spring Security — 초기 접근 제어 설정 |
| 기타 의존성 | Validation, Lombok, Java Mail Sender, DevTools |
| 테스트 기반 | Spring Boot 테스트 의존성 · JUnit Platform |

이메일 인증, JWT 인증, JPA 기반 도메인 로직은 구현 예정입니다.

## 로컬 실행

### 1. 준비 사항

- JDK 25
- IntelliJ IDEA 또는 Java 개발 환경
- 실행 중인 MySQL 서버와 `studyroom` 데이터베이스
- MySQL을 컨테이너로 실행하는 경우 Docker 실행 환경

현재 로컬 개발은 MySQL을 Docker에서 실행하고 Spring Boot는 IntelliJ에서 실행하는 방식입니다. 프로젝트에는 아직 Dockerfile이나 Docker Compose 설정 파일이 없습니다.

### 2. MySQL 실행 및 연결 설정

Docker에서 생성한 MySQL 컨테이너를 시작하고, 호스트의 `3306` 포트로 연결할 수 있는지 확인합니다. 컨테이너 이름과 계정 정보는 각자의 로컬 환경에 맞춥니다.

현재 `src/main/resources/application.yaml`의 연결 대상은 다음과 같습니다.

| 항목 | 설정 |
| --- | --- |
| 호스트 | `localhost` |
| 포트 | `3306` |
| 데이터베이스 | `studyroom` |
| 시간대 | `Asia/Seoul` |

데이터베이스는 실행 전에 생성되어 있어야 합니다. 계정과 비밀번호는 실행 중인 MySQL 설정과 일치해야 합니다.

실행 환경에 맞춰 IntelliJ 실행 구성의 환경 변수로 아래 Spring Boot 설정을 지정할 수 있습니다.

| 환경 변수 | 입력할 값 |
| --- | --- |
| `SPRING.DATASOURCE.URL` | `jdbc:mysql://localhost:3306/studyroom?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useUnicode=true` |
| `SPRING.DATASOURCE.USERNAME` | 로컬 DB 사용자 이름 |
| `SPRING.DATASOURCE.PASSWORD` | 해당 사용자의 DB 비밀번호 |

실제 비밀번호는 README나 커밋에 포함하지 않습니다. 환경 변수로 덮어쓰더라도 설정 파일에 이미 적힌 비밀번호가 제거되는 것은 아니므로, 첫 커밋 전에 파일의 비밀정보 분리를 완료해야 합니다.

### 3. 애플리케이션 실행

IntelliJ에서 Gradle 프로젝트를 불러오고 JDK를 25로 지정한 뒤, `com.studyroom.reservation.ReservationApplication`의 `main` 메서드를 실행합니다.

터미널에서는 DB 연결 환경 변수를 설정한 뒤 프로젝트 루트에서 Gradle Wrapper로 실행할 수 있습니다.

Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
sh ./gradlew bootRun
```

### 4. Health API 확인

기본 포트로 실행한 경우 브라우저에서 `http://localhost:8080/health`에 접속합니다.

| 항목 | 예상 결과 |
| --- | --- |
| 요청 | `GET /health` |
| 인증 | 불필요 |
| HTTP 상태 | `200 OK` |
| 응답 본문 | `Health` |

이 API는 HTTP 요청과 응답 흐름을 확인하기 위한 임시 API입니다. DB 상태를 직접 조회하거나 외부 시스템의 상태를 진단하지는 않습니다.

## 현재 소스 구조

```text
src/
├── main/
│   ├── java/com/studyroom/reservation/
│   │   ├── ReservationApplication.java   # 실행 진입점 및 임시 Health API
│   │   └── config/
│   │       └── SecurityConfig.java        # 초기 HTTP 접근 제어
│   └── resources/
│       └── application.yaml              # 애플리케이션 및 DB 연결 설정
└── test/java/com/studyroom/reservation/
    └── ReservationApplicationTests.java  # 생성된 컨텍스트 로딩 테스트
```

## 향후 개발 계획

요구사항과 데이터 모델을 구체화한 뒤, 인증·인가와 예약 로직부터 순서대로 구현합니다.

- 요구사항에 따른 DB/ERD 설계 및 도메인 구현
- 회원, 이메일 인증, Spring Security/JWT 인증·인가
- 스터디룸 관리 및 예약 비즈니스 로직
- 핵심 로직 테스트와 예약 동시성 문제 재현·해결
- Redis 캐싱과 DB 쿼리·인덱스 개선
- Swagger/OpenAPI, Docker, AWS EC2, GitHub Actions CI/CD
- 서비스 이용을 위한 최소 프론트 화면 및 API·운영 문서

### 검증 계획

- **예약 정합성:** 동시 요청에서 중복 예약 발생 여부를 재현하고, 동시성 제어 적용 후 동일 조건으로 검증합니다.
- **캐싱 효과:** MySQL-only 기준 성능을 먼저 측정한 뒤 Redis 적용 전후의 응답 시간과 DB 부하를 비교합니다.
- **쿼리 개선:** 인덱스 적용 전후의 EXPLAIN과 실행 시간을 비교합니다.

각 검증의 실행 조건과 결과를 함께 기록합니다.
