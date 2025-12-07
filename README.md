# 🎄 EveryVent

<div align="center">

### 매일의 이벤트를 받아보세요, **EveryVent!**

*크리스마스를 기다리는 설렘, 매일매일 새로운 이벤트로 채워보세요*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)

</div>

---

## 🎅 EveryVent가 특별한 이유

**EveryVent**는 크리스마스를 기다리며,
나만의 크리스마스 여정을 만들고 친구들과 공유하며 함께 설렘을 키워가는 **소셜 플랫폼**입니다.

### 🎁 나만의 어드벤트 캘린더 만들기
- 12월 1일부터 25일까지, **나만의 특별한 이벤트**를 계획하세요
- 매일 최대 3개의 미션을 설정하고 달성해보세요
- **원하는 색상**으로 캘린더를 꾸미고, 나만의 스타일을 표현하세요

### 🌟 매일매일 설렘 가득
- 자정이 되면 **새로운 이벤트가 열립니다**
- 오늘의 미션을 완료하고 성취감을 느껴보세요
- 친구들의 캘린더를 **스크랩**하여 함께 즐겨보세요

### 👥 함께 즐기는 크리스마스
- 공개 범위를 설정하여 전체 공개, 팔로워만 공개, 또는 나만의 비밀 캘린더로
- 다른 사용자들의 창의적인 캘린더를 **발견**하고 영감을 받으세요
- 팔로우 기능으로 친구들과 소통하세요

### ~~🏆 경쟁과 재미(미구현)~~
- ~~**일일 랭킹**: 어제 가장 많은 미션을 달성한 사람은 누구?~~
- ~~**월간 랭킹**: 이번 달 가장 인기있는 캘린더는?~~
- ~~순위권에 도전하고 최고의 크리스마스 플래너가 되어보세요!~~

---

## 🚀 주요 기능

### 📅 캘린더 시스템
- 공식 어드벤트 캘린더 자동 제공
- 개인 캘린더 최대 3개 생성 (스크랩 포함)
- 날짜별 태스크 최대 3개 설정
- 실시간 태스크 공개 (자정 0시 자동 오픈)
- 완료 체크로 진행상황 추적

### 🎨 자유로운 커스터마이징
- 캘린더 제목, 설명, 색상 자유 설정
- 공개 범위 선택 (전체/팔로워/비공개)
- 태스크 공개 기간 설정 가능
- 스크랩한 캘린더 색상 변경

### 🔐 간편한 소셜 로그인
- 카카오, ~~네이버, 구글~~ OAuth 지원
- 닉네임만으로 간단한 회원가입
- 프로필 커스터마이징 (소개글, 프로필 이미지)


### ~~📊 랭킹 & 통계~~ (미구현)
- 일별 최다 달성률 TOP 사용자
- 월별 최다 스크랩 캘린더
- 월별 통합 달력 뷰

---

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL (Production), H2 (Development)
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle
- **Authentication**: OAuth 2.0 (Kakao, Naver, Google)

### Architecture
```
Clean Architecture + Layered Architecture
├── Presentation Layer (Controller)
├── Business Layer (Service)
├── Persistence Layer (Repository)
└── Domain Layer (Entity)
```

---

## 📦 프로젝트 구조

```
src/main/java/kr/santanrudolph/everyvent/
├── user/               # 사용자 관리 (회원가입, 프로필, 탈퇴)
├── calendar/           # 캘린더 & 태스크 관리
├── follow/             # 팔로우/팔로워 소셜 기능
├── auth/               # OAuth 소셜 로그인 인증
└── common/             # 공통 설정, 예외처리, 응답 포맷
```

---

## 🎯 핵심 비즈니스 로직

### 캘린더 생성 규칙
- 모든 사용자에게 **공식 캘린더 1개 자동 제공** (12월 1일 시작)
- 개인 캘린더는 **최대 3개**까지 생성 가능 (공식 캘린더 제외)
- 각 날짜별로 **최대 3개의 태스크** 생성 가능

### 태스크 공개 시스템
- 태스크는 **해당 날짜 0시**가 되어야 조회 가능
- 사전 조회 절대 불가 (스포일러 방지)
- 태스크 공개 기간을 캘린더 생성 시 설정 가능

### 스크랩 기능
- 접근 권한 있는 캘린더만 스크랩 가능 (전체공개, 팔로워공개)
- 스크랩한 캘린더는 **원본 내용 수정 불가**
- **색상만 변경** 가능으로 나만의 컬렉션 구성
- 다음 달 캘린더는 **1일 기준 7일 전**부터 미리 스크랩 가능

### 랭킹 산정
- **일별 랭킹**: 전날(어제) 태스크 완료율 기준
- **월별 랭킹**: 해당 월 스크랩 수 기준

## 📝 API 문서

API 문서는 Swagger를 통해 제공됩니다.

https://api.everyvent.cloud:8080/swagger-ui.html

### 🎅 **매일의 설렘, EveryVent와 함께하세요!**

*크리스마스까지 매일매일 특별한 이벤트*

Made with ❤️ by SantanRudolph Team

</div>
