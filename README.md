# EveryVent 🎄

> 크리스마스 어드벤트 캘린더 소셜 플랫폼

사용자가 개인 맞춤형 어드벤트 캘린더를 생성하고, 일별 태스크를 관리하며, 다른 사용자와 공유할 수 있는 소셜 플랫폼입니다.

현재 개발 중에 있고, 개발 편의를 위해 서버만 1차 배포를 완료하였습니다.
- 배포된 주소: http://43.201.101.45:8080/swagger-ui/index.html

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

## 📑 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [ERD](#-erd)
- [실행 방법](#-실행-방법)
- [API 문서](#-api-문서)
- [기술적 의사결정](#-기술적-의사결정)
- [트러블슈팅](#-트러블슈팅)

## ✨ 담당한 주요 기능

### 1. 소셜 로그인 & 사용자 관리
- **OAuth 2.0 기반 소셜 로그인**: 카카오, 네이버, 구글
- **JWT 토큰 기반 인증**: Access Token + Refresh Token
- **Redis 토큰 관리**: Refresh Token 저장 및 블랙리스트 관리
- **회원 정보 관리**: 닉네임, 소개글, 프로필 이미지
- **Soft Delete 탈퇴**: 데이터 복구 가능한 논리적 삭제

### 2. 소셜 기능
- **팔로우/팔로워**: 사용자 간 팔로우 관계 관리
- **공개범위별 캘린더 공유**: 전체공개, 팔로워공개, 비공개

### 3. 랭킹 시스템 (개발 진행중)
- **일별 최다 달성률**: 전날 태스크 완료율 기준 랭킹
- **월별 최다 스크랩 캘린더**: 해당 월 스크랩 수 기준 랭킹

## 🛠 기술 스택

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.6
- **Security**: Spring Security + JWT + OAuth 2.0
- **ORM**: Spring Data JPA (Hibernate)
- **Build Tool**: Gradle 8.5

### Database
- **Production**: PostgreSQL 16
- **Development**: H2 Database
- **Cache**: Redis 7.2

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Deployment**: AWS RDS (PostgreSQL), AWS ElastiCache (Redis)

### Documentation & Testing
- **API Documentation**: Swagger (OpenAPI 3.0)
- **Testing**: JUnit 5, Mockito, Spring Test

## 🏗 아키텍처

### 레이어드 아키텍처

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│    (Controller + DTO + Swagger)     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Application Layer           │
│     (Service + Business Logic)      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Persistence Layer           │
│   (Repository + Entity + JPA)       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Database Layer             │
│    (PostgreSQL + Redis + RDS)       │
└─────────────────────────────────────┘
```

### 도메인 모듈 구조

```
everyvent/
├── auth/                   # 인증 & 보안
│   ├── controller/        # OAuth2, JWT API
│   ├── service/           # 토큰 관리, 로그아웃
│   ├── security/          # JWT Provider, Filter, Handler
│   └── repository/        # Redis Token Repository
│
├── domain/
│   ├── user/              # 사용자 도메인
│   │   ├── User.java      # 사용자 엔티티
│   │   ├── UserController.java
│   │   ├── UserService.java
│   │   └── UserRepository.java
│   │
│   ├── calendar/          # 캘린더 도메인
│   │   ├── entity/
│   │   │   ├── Calendar.java              # 기본 캘린더
│   │   │   ├── OriginalCalendar.java      # 원본 캘린더
│   │   │   ├── OfficialCalendar.java      # 공식 캘린더
│   │   │   └── DistributedCalendar.java   # 배포 캘린더
│   │   ├── CalendarController.java
│   │   ├── CalendarService.java
│   │   └── CalendarRepository.java
│   │
│   └── follow/            # 팔로우 도메인
│       ├── Follow.java    # 팔로우 엔티티
│       ├── FollowController.java
│       ├── FollowService.java
│       └── FollowRepository.java
│
└── global/                # 글로벌 설정
    ├── config/            # Spring Security, Swagger, CORS, JPA, Redis
    ├── entity/            # BaseEntity (생성/수정 시간)
    └── exception/         # 전역 예외 처리
```

## 📊 ERD

### 핵심 엔티티 관계

```
┌─────────────┐          ┌──────────────┐
│    User     │──────────│    Follow    │
│             │ 1      * │              │
│ - id        │          │ - follower   │
│ - nickname  │          │ - following  │
│ - intro     │          └──────────────┘
└──────┬──────┘
       │ 1
       │
       │ *
┌──────▼──────────┐
│    Calendar     │
│                 │
│ - id            │◄─────────┐
│ - title         │          │ (상속)
│ - description   │          │
│ - startDate     │     ┌────┴────────────────┐
│ - endDate       │     │                     │
│ - visibility    │ ┌───▼────────────┐ ┌─────▼──────────┐
│ - color         │ │ Original       │ │  Official      │
└─────────────────┘ │ Calendar       │ │  Calendar      │
                    │                │ │                │
                    │ - owner        │ │ - distributeAt │
                    │ - taskStart    │ └────────────────┘
                    │ - taskEnd      │          │
                    └────────────────┘          │ 1
                                                │
                                                │ *
                                         ┌──────▼────────────┐
                                         │  Distributed      │
                                         │  Calendar         │
                                         │                   │
                                         │ - originalId      │
                                         │ - recipient       │
                                         └───────────────────┘


## 🚀 실행 방법

### swagger 테스트
현재 서버만 1차 배포를 완료한 상태로, 아래에서 테스트해볼 수 있습니다.
http://43.201.101.45:8080/swagger-ui/index.html


### 2. 로컬 개발 환경 실행

```bash
# 1. Redis 실행 (Docker)
docker run -d -p 6379:6379 redis:7.2-alpine

# 2. 애플리케이션 실행 (dev 프로파일)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. 환경변수 설정

`.env` 파일 예시:

```env
# Database (PostgreSQL)
RDS_HOST=your-rds-endpoint.amazonaws.com
RDS_PORT=5432
RDS_DB_NAME=everyvent
RDS_USERNAME=postgres
RDS_PASSWORD=your-password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# JWT
JWT_SECRET=your-jwt-secret-key-min-256-bits

# OAuth2 (Kakao)
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

## 📖 API 문서

### Swagger UI
애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI 스펙**: `http://localhost:8080/v3/api-docs`

> **참고**: 현재 프론트엔드 연동은 진행 중이며, Swagger UI를 통해 모든 API를 테스트할 수 있습니다.

### 주요 API 엔드포인트

#### 인증 (Auth)
- `POST /api/auth/refresh` - Access Token 갱신
- `POST /api/auth/logout` - 로그아웃 (토큰 무효화)

#### 사용자 (User)
- `GET /api/users/me` - 내 정보 조회
- `PATCH /api/users/me/nickname` - 닉네임 수정
- `PATCH /api/users/me/introduction` - 소개글 수정
- `DELETE /api/users/me` - 회원 탈퇴 (Soft Delete)

#### 캘린더 (Calendar)
- `POST /api/calendars` - 개인 캘린더 생성
- `GET /api/calendars/{id}` - 캘린더 상세 조회
- `GET /api/calendars/me` - 내 캘린더 목록 조회
- `POST /api/calendars/official` - 공식 캘린더 생성 (관리자)
- `POST /api/calendars/distribute/{id}` - 공식 캘린더 배포 (관리자)

#### 팔로우 (Follow)
- `POST /api/follow/me/followings/{targetId}` - 팔로우
- `DELETE /api/follow/me/followings/{targetId}` - 언팔로우
- `GET /api/follow/me/followers` - 내 팔로워 목록
- `GET /api/follow/me/followings` - 내 팔로잉 목록

## 🎯 핵심 성과

### 1. 깔끔한 인증 로직 설계
- **OAuth2 + JWT 통합**: 소셜 로그인 성공 시 JWT 발급을 하나의 플로우로 구현
- **토큰 관리 전략**: Access Token (15분) + Refresh Token (7일) + Redis 블랙리스트
- **보안 강화**: 로그아웃 시 즉시 토큰 무효화, 토큰 갱신 시 이중 검증

### 2. 에러 처리 표준화
- **범용 에러 코드 정책**: 80줄 → 30줄로 코드 경량화 (60% 감소)
- **구조화된 로깅**: 요청 정보 + 에러 메시지를 JSON 형태로 기록
- **일관된 API 응답**: 모든 에러가 `code`, `message`, `detail` 구조 준수

### 3. 테스트 커버리지 관리
- **핵심 도메인 우선 작성**: Auth, User, Follow 도메인 84% 테스트 성공률
- **단위 테스트 격리**: Mockito로 의존성 분리, 빠른 피드백 루프 확보
- **가독성 높은 테스트**: AAA 패턴 + @DisplayName으로 테스트 의도 명확화

---

## 💡 기술적 의사결정

### 1. PostgreSQL 선택 이유
- **JSON 타입 지원**: 일별 태스크를 유연하게 저장 (1~25일, 최대 3개/날짜)
- **복잡한 쿼리 성능**: 팔로우 관계, 공개범위별 캘린더 조회 등 복잡한 관계형 쿼리 최적화
- **AWS RDS 통합**: 관리형 서비스로 백업, 모니터링 자동화

### 2. Redis 도입 이유
- **Refresh Token 저장**: 빠른 조회 성능 + TTL 자동 만료
- **토큰 블랙리스트**: 로그아웃 시 Access Token 무효화
- **향후 확장성**: 캘린더 조회 캐싱, 랭킹 시스템 구현 대비

### 3. JWT vs Session
- **JWT 선택**: Stateless 인증으로 수평 확장 용이
- **Refresh Token 전략**: Access Token 탈취 위험 최소화 (15분 만료)
- **Redis 저장**: Refresh Token 관리 및 즉시 무효화 가능

### 4. 도메인 주도 설계 (DDD)
- **도메인별 모듈 분리**: user, calendar, follow, auth
- **응집도 향상**: 각 도메인이 독립적인 Repository, Service, Entity 보유
- **유지보수성**: 기능 추가 시 해당 도메인만 수정

### 5. 상속 구조 설계 (Calendar)
- **공통 필드 추출**: Calendar 추상 클래스로 중복 제거
- **타입별 특화**: OriginalCalendar, OfficialCalendar, DistributedCalendar 각각 필요한 필드만 보유
- **다형성 활용**: CalendarRepository에서 모든 타입 조회 가능

## 🔧 트러블슈팅

### 1. 전역 예외 처리 설계: 범용 에러 코드 vs 도메인별 에러 코드

**문제 상황**
- 프로젝트 초기에는 모든 에러 상황마다 개별 ErrorCode를 정의 (예: `CALENDAR_NOT_FOUND`, `USER_NOT_FOUND`, `ALREADY_FOLLOWING`)
- ErrorCode enum이 80줄 이상으로 비대해짐
- 비슷한 에러 코드가 중복 생성됨 (예: `CALENDAR_NOT_FOUND`, `USER_NOT_FOUND`, `FOLLOW_NOT_FOUND` 모두 동일한 404 응답)

**해결 방법: 범용 에러 코드 + Detail 메시지 전략**

```java
// Before: 도메인별 세부 에러 코드 (비효율적)
throw new EveryventException(ErrorCode.CALENDAR_NOT_FOUND);
throw new EveryventException(ErrorCode.USER_NOT_FOUND);
throw new EveryventException(ErrorCode.ALREADY_FOLLOWING);

// After: 범용 에러 코드 + 상세 메시지 (효율적)
throw new EveryventException(ErrorCode.NOT_FOUND, "캘린더를 찾을 수 없습니다.");
throw new EveryventException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
throw new EveryventException(ErrorCode.ALREADY_EXIST, "이미 팔로우한 사용자입니다.");
```

**에러 코드 설계 원칙 수립**
```java
@AllArgsConstructor
public enum ErrorCode {
  // [원칙 1] 범용 에러 코드 사용을 기본으로 한다
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
  ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

  // [원칙 2] 핵심 비즈니스 규칙만 별도 에러 코드로 선언한다
  // - 클라이언트가 특별한 UI/UX 처리가 필요한 경우
  // - 모니터링/분석이 중요한 비즈니스 제약사항
  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
  ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 액세스 토큰입니다."),
  BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "로그아웃된 토큰입니다."),
  // ...
}
```

**GlobalExceptionHandler에서 상세 메시지 처리**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String LOG_FORMAT = """
      {
          "RequestURI": "{} {}",
          "RequestBody": {},
          "ErrorMessage": "{}"
      }
      """;

  @ExceptionHandler(EveryventException.class)
  public ResponseEntity<ErrorResponse> handleEveryventException(
      HttpServletRequest request, EveryventException e) {

    ErrorCode errorCode = e.getErrorCode();

    // 요청 정보 + 에러 메시지 구조화된 로깅
    log.warn(LOG_FORMAT,
             request.getMethod(),
             request.getRequestURI(),
             getRequestBody(request),
             e.getMessage());

    // detail이 있으면 상세 메시지 포함, 없으면 기본 메시지만 반환
    ErrorResponse response = e.getDetail() != null
        ? ErrorResponse.of(errorCode, e.getDetail())
        : ErrorResponse.of(errorCode);

    return ResponseEntity
        .status(errorCode.getHttpStatus())
        .body(response);
  }
}
```

**결과**
- ErrorCode enum 크기 80줄 → 30줄로 60% 감소
- 에러 응답 일관성 향상 (모든 응답이 `code`, `message`, `detail` 구조)
- 로깅 구조화로 에러 추적 용이
- 클라이언트는 HTTP 상태 코드 + ErrorCode로 적절한 UI 처리 가능

**에러 응답 예시**
```json
{
  "code": "NOT_FOUND",
  "message": "해당 리소스를 찾을 수 없습니다.",
  "detail": "ID 123번 캘린더를 찾을 수 없습니다."
}
```

---

### 2. OAuth2 로그인 후 JWT 발급 문제

**문제 상황**
- OAuth2 인증 성공 후 JWT 토큰을 프론트엔드에 전달하는 방법 고민
- Spring Security의 기본 `OAuth2LoginSuccessHandler`는 redirect만 지원

**해결 방법**
```java
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {
        // 1. OAuth2 인증 정보에서 사용자 식별
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. DB에서 사용자 조회 or 생성
        User user = userService.findOrCreateUser(oAuth2User);

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 4. Refresh Token은 Redis에 저장
        redisTokenRepository.save(refreshToken, user.getId());

        // 5. 프론트엔드로 리다이렉트 (쿼리 파라미터로 토큰 전달)
        String redirectUrl = String.format("%s?accessToken=%s&refreshToken=%s",
                                          frontendUrl, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
```

**결과**
- OAuth2 인증과 JWT 발급을 하나의 플로우로 통합
- 프론트엔드는 리다이렉트 URL에서 토큰 추출 후 로컬 스토리지 저장

---

### 2. Gradle 빌드 시 Docker 권한 문제

**문제 상황**
```bash
# Dockerfile에서 gradlew 실행 시 권한 오류
Step 5/10 : RUN ./gradlew --no-daemon build -x test
 ---> Running in abc123def456
bash: ./gradlew: Permission denied
```

**원인 분석**
- Windows 환경에서 Git이 파일 권한을 올바르게 추적하지 못함
- `gradlew` 파일의 실행 권한이 Docker 이미지에 반영되지 않음

**해결 방법**
```dockerfile
# Dockerfile
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# 1. Gradle Wrapper 파일 복사
COPY gradlew .
COPY gradle gradle

# 2. 명시적으로 실행 권한 부여 (핵심!)
RUN chmod +x gradlew

# 3. 의존성 파일 복사 및 다운로드
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon dependencies

# 4. 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew --no-daemon build -x test
```

**추가 조치**
```bash
# Git에 실행 권한 추적 설정
git update-index --chmod=+x gradlew
git commit -m "fix: Dockerfile gradlew 권한 문제 해결"
```

**결과**
- Docker 빌드 성공
- CI/CD 파이프라인에서도 안정적으로 동작
- 배포 swagger 주소: http://43.201.101.45:8080/swagger-ui/index.html

---

### 3. 테스트 커버리지 향상을 위한 노력

**현재 테스트 현황**
```bash
# 총 31개 테스트 중 26개 성공 (84% 성공률)
./gradlew test

✅ AuthServiceTest (100% 통과)
  - JWT 토큰 갱신 로직 검증
  - Refresh Token 유효성 검사
  - 로그아웃 시 토큰 블랙리스트 등록

✅ UserServiceTest (100% 통과)
  - 사용자 정보 조회/수정
  - 닉네임 중복 검증
  - Soft Delete 탈퇴 처리

✅ FollowServiceTest (부분 통과)
  - 팔로우 생성/삭제 (통과)
  - 팔로워/팔로잉 목록 조회 (통과)
  - 맞팔로우 여부 확인 (일부 실패 - 리팩토링 중)
```

**테스트 작성 원칙**
1. **AAA 패턴**: Arrange (준비) → Act (실행) → Assert (검증)
2. **Mockito 활용**: 의존성 Mock으로 단위 테스트 격리
3. **@DisplayName**: 테스트 의도 명확화 (한글 사용)

**테스트 코드 예시 (AuthServiceTest)**
```java
@DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
@Test
void refreshAccessToken_Success() {
    // Given
    String refreshToken = "valid-refresh-token";
    Long userId = 1L;
    String expectedAccessToken = "new-access-token";

    when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
    when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
    when(redisTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(userId));
    when(jwtTokenProvider.generateAccessToken(userId)).thenReturn(expectedAccessToken);

    // When
    String actualAccessToken = authService.refreshAccessToken(refreshToken);

    // Then
    assertThat(actualAccessToken).isEqualTo(expectedAccessToken);
    verify(jwtTokenProvider).validateToken(refreshToken);
    verify(jwtTokenProvider).generateAccessToken(userId);
}
```


## 📝 개발 가이드

### 커밋 메시지 규칙
```
타입(범위): 간단한 설명

예시:
- feat(calendar): 공식 캘린더 배포 기능 구현
- fix(auth): JWT 토큰 갱신 시 만료 시간 오류 수정
- docs(readme): 트러블슈팅 섹션 추가
```

### 브랜치 전략
- `main`: 운영 배포용
- `develop`: 개발 통합
- `feature/#이슈번호-기능명`: 새 기능 개발
- `hotfix/#이슈번호-수정명`: 긴급 버그 수정
