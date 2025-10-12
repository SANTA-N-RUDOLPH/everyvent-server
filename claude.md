# EveryVent Server - 크리스마스 어드벤트 캘린더

## 📅 프로젝트 개요

### 목적
크리스마스 어드벤트 캘린더 소셜 플랫폼으로, 사용자가 개인 맞춤형 캘린더를 생성하고 공유할 수 있는 서비스

### 핵심 기능

#### 회원가입/로그인
- **소셜 로그인**: 카카오, 네이버, 구글 OAuth 인증
- **회원가입 시 필수 입력**: 닉네임(중복 불가)
- **선택 입력**: 소개글, 프로필 이미지(기본 이미지 제공, 추후 업데이트 예정)
- **회원 관리**: 정보 수정, soft delete 탈퇴

#### 캘린더 기능
- **공식 캘린더**: 12/1부터 제공되는 기본 크리스마스 캘린더 (삭제 가능, 3개 제한에 미포함)
- **개인 캘린더**: 제목, 설명, 기간, 공개범위(전체/팔로워/비공개), 일별 태스크, 색상 설정
- **일별 태스크**: 1~25일 각 날짜별로 최대 3개 태스크 생성 가능
- **태스크 공개 설정**: 캘린더 생성 시 공개할 태스크의 시작일/종료일 설정 가능
- **태스크 열람**: 해당 날짜의 00시가 되면 태스크 조회 가능 (사전 조회 불가)
- **완료 체크**: 사용자가 태스크 달성 시 완료 체크 기능
- **스크랩 기능**: 공개된 캘린더를 개인 컬렉션에 저장 (재스크랩 불가, 색상만 수정 가능)
- **캘린더 발견**: 다음달 캘린더 목록은 다음달 1일 기준 7일 전부터 스크랩 가능 
- **통합 달력**: 월별 보기

#### 소셜 기능
- **팔로우/팔로워**: 사용자 간 팔로우 관계

#### 랭킹 기능
- **일별 최다 달성률**: 매일 어제의 태스크 완료율이 높은 사용자 랭킹
- **월별 최다 스크랩 캘린더**: 해당 월에 가장 많이 스크랩된 캘린더 랭킹

### 기술 스택
- **Backend**: Spring Boot 3.5.6, Java 17
- **Database**: PostgreSQL (운영), H2 (개발)
- **ORM**: Spring Data JPA
- **Build**: Gradle
- **인증**: OAuth 2.0 (카카오, 네이버, 구글)

## 🏗️ 아키텍처

### 패키지 구조 (예시)
```
src/main/java/kr/santanrudolph/everyvent/
├── user/               # 사용자 관리
├── calendar/           # 캘린더 및 태스크 관리
├── follow/             # 팔로우/팔로워 기능
├── auth/               # OAuth 소셜 로그인
└── common/             # 공통 설정, 예외처리, 응답
```

### 주요 엔티티 (예시)
- **User**: 사용자 정보, 닉네임, 소개글
- **Calendar**: 캘린더 메타데이터, 공개범위, 태스크 공개 시작일/종료일, 색상
- **CalendarTask**: 날짜별 태스크 내용 (1~25일, 날짜당 최대 3개)
- **TaskCompletion**: 사용자별 태스크 완료 기록
- **Follow**: 팔로우/팔로워 관계

## 🎯 개발 가이드

### 브랜치 전략
- **main**: 운영 배포용
- **develop**: 개발 통합
- **feature/#이슈번호-기능명**: 새 기능 개발 (예: `feature/#12-user-login`)
- **hotfix/#이슈번호-수정명**: 긴급 버그 수정

### 커밋 메시지 규칙
```
타입(범위): 간단한 설명

- feat: 새로운 기능
- fix: 버그 수정
- docs: 문서 수정
- refactor: 코드 리팩토링
- test: 테스트 추가/수정
- chore: 빌드/설정 변경
```

### 코딩 컨벤션
#### 기본 원칙
1. **커밋은 의미 있는 최소 단위로 작성한다.**
    - 한 커밋이 하나의 기능 추가, 버그 수정, 코드 리팩토링 등 **명확한 목적**을 갖도록 한다.

2. **커밋 메시지는 명확하고 일관되게 작성한다.**
    - 메시지 형식: `타입(scope): 간단한 설명`
    - 필요 시 상세 설명과 bullet point 활용
    - 관련 이슈 번호 포함 가능 (`fixes #17`)

3. **커밋 단위와 PR 단위를 구분한다.**
    - **커밋 단위**: 작은 단위, 의미 있는 최소 변경 사항
    - **PR 단위**: 기능 단위, 완성된 작업 단위


#### 네이밍
- **클래스**: PascalCase (`CalendarService`)
- **메서드/변수**: camelCase (`createCalendar`)
- **상수**: UPPER_SNAKE_CASE (`MAX_CALENDAR_COUNT`)
- **패키지**: 소문자 (`kr.santanrudolph.everyvent.calendar`)

#### 레이어별 작성 규칙

##### Controller 작성 규칙
- `@RestController`, `@RequestMapping` 사용
- ResponseEntity 사용으로 HTTP 상태 명시
- 비즈니스 로직 금지 → Service 위임
- DTO 사용: Request DTO 필수, Response DTO 권장
- `@Valid`로 요청 데이터 검증
- 예외 발생 시 `@ControllerAdvice`와 연계

##### Service 작성 규칙
- `@Service`, `@RequiredArgsConstructor` 기본
- 클래스 레벨: `@Transactional(readOnly = true)`
- 쓰기 작업만 메서드 레벨 `@Transactional`
- 하나의 Service는 한 도메인만 담당
- Controller와 통신 시 Request DTO 사용
- Repository와 통신 시 Entity 직접 사용 가능, 필요 시 Projection/DTO 사용

##### Entity 작성 규칙
- `@Entity`, `@Table(name = "테이블명")` 명시
- `@Builder` 패턴으로 객체 생성
- `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용
- 연관관계: 지연로딩(LAZY) 기본
- BaseTimeEntity 상속 → 생성/수정 시간 자동 관리

##### DTO 작성 규칙
- 불변 객체 선호 (final 필드 + `@Builder`)
- Validation 어노테이션 사용: `@NotNull`, `@Size`, `@Email` 등
- Request DTO: Controller → Service 전달
- Response DTO: Service → Controller 전달
- 필드가 많거나 optional이면 DTO로 묶기

#### 클린코드 규칙
- 메서드 길이 30줄 이내
- 매개변수 3개 이상 시 DTO 사용
- 의미있는 변수명 사용
- boolean 변수는 `is`, `has`, `can` 접두사

### API 설계 원칙
- RESTful URL 구조
- HTTP 상태코드 명시적 사용
- 일관된 응답 형태 (`ApiResponse<T>`)
- 요청 데이터 validation 필수

### 주요 비즈니스 규칙
- 공식 캘린더는 1개만 제공, 모든 가입자에게 자동 제공
- 캘린더 최대 3개 생성 가능 (공식 캘린더 제외)
- 일별 태스크는 각 날짜별로 최대 3개까지 생성 가능
- 태스크는 해당 날짜가 되어야 조회 가능 (사전 조회 불가)
- 스크랩은 유저가 접근 권한 있는 캘린더만 가능 (전체공개, 팔로워공개)
- 스크랩한 캘린더는 내용 수정 불가, 색상만 변경 가능
- 랭킹은 일별 태스크 달성률과 월별 스크랩 수 기준으로 산정