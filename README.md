# 🎓 **Studify - MSA 전환 프로젝트**

> **Studify 학습 플랫폼**의 핵심 기능(**회원가입/로그인**, **게시물 관리**)을 **마이크로서비스 아키텍처로 완전 전환**한 프로젝트입니다.  
> **Spring Cloud**, **Eureka Server**, **API Gateway**, **JWT 인증**, **CORS 설정** 등 MSA 핵심 기술을 실무 수준으로 적용했습니다.

---

## **📋 목차**

1. [🏗️ 프로젝트 개요](#프로젝트-개요)
2. [🎯 MSA 전환 범위](#msa-전환-범위)
3. [🧩 시스템 아키텍처](#시스템-아키텍처)
4. [🔧 기술 스택](#기술-스택)
5. [📁 프로젝트 구조](#프로젝트-구조)
6. [🚀 실행 방법](#실행-방법)
7. [🔐 JWT 인증 구조](#jwt-인증-구조)
8. [🗄️ 데이터베이스 구성](#데이터베이스-구성)
9. [📡 API 엔드포인트](#api-엔드포인트)
10. [🎓 학습 포인트](#학습-포인트)

---

## **🏗️ 프로젝트 개요**

### **프로젝트 목표**
- 모놀리스 애플리케이션의 **핵심 기능만 선택적으로 MSA 전환**
- Spring Cloud 기반의 **실무 수준 MSA 인프라** 구축
- JWT 기반의 **분산 인증 시스템** 구현
- Eureka를 통한 **동적 서비스 디스커버리** 적용

### **전환 전략**
```
기존 모놀리스 (studify-be-main)
    ↓ 핵심 기능만 분리
├─ User Service (회원가입/로그인) → MSA 전환 ✅
├─ Post Service (게시물 관리) → MSA 전환 ✅
└─ 기타 기능 (댓글, 알림, 팀 등) → 모놀리스 유지
```

---

## **🎯 MSA 전환 범위**

### **✅ MSA로 전환된 서비스**

| 서비스 | 포트 | 기능 | DB | 상태 |
|--------|------|------|-----|------|
| **Eureka Server** | 8761 | 서비스 디스커버리 및 헬스체크 | - | ✅ 완료 |
| **API Gateway** | 8080 | 라우팅, CORS 처리, Security 설정 | - | ✅ 완료 |
| **User Service** | 8081 | 회원가입, 로그인, JWT 발급/검증 | studify_users | ✅ 완료 |
| **Post Service** | 8082 | 게시물 CRUD, 포지션별 검색 | studify_posts | ✅ 완료 |
| **Notification Service** | 8083 | 알림 관리 (기본 구조) | - | ✅ 완료 |

### **🎯 주요 구현 사항**
- ✅ **Spring Cloud Eureka** 서비스 디스커버리
- ✅ **Spring Cloud Gateway** 동적 라우팅 및 LoadBalancing
- ✅ **JWT 인증** User Service에서 발급 및 검증
- ✅ **CORS 설정** API Gateway에서 중앙 관리
- ✅ **데이터베이스 분리** 각 서비스별 독립 DB
- ✅ **RewritePath 필터** `/studify` prefix 처리

---

## **🧩 시스템 아키텍처**

### **현재 구성도**
```
┌─────────────────────────────────────────────────────────────┐
│              🌐 Frontend (React) - Port 3000                │
│                http://localhost:3000                        │
│              • API 호출 (axios)                             │
│              • CORS: http://localhost:3000                  │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTP 요청
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              🚪 API Gateway - Port 8080                     │
│        • CorsWebFilter (http://localhost:3000)              │
│        • SecurityWebFilterChain (공개 경로 허용)            │
│        • 라우팅:                                             │
│          - /api/v1/users/** → User Service                  │
│          - /api/v1/posts/** → Post Service                  │
│          - /studify/** → Post Service (RewritePath)         │
│          - /api/v1/notifications/** → Notification Service  │
└─────────────────────┬───────────────────────────────────────┘
                      │ Eureka Client로 서비스 탐색
                      ▼
┌─────────────────────────────────────────────────────────────┐
│            🌐 Eureka Server - Port 8761                     │
│        • 서비스 등록 & 상태 관리                              │
│        • Health Check (30초마다)                            │
│        • Dashboard: http://localhost:8761                   │
└─────────────────────┬───────────────────────────────────────┘
                      │ 등록된 서비스들
        ┌─────────────┼─────────────┬─────────────┐
        ▼             ▼             ▼             ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│👥 User Service│ │📝 Post Service│ │🔔 Notification│ │🚪 API Gateway│
│  Port 8081   │ │  Port 8082   │ │  Port 8083   │ │  Port 8080   │
├──────────────┤ ├──────────────┤ ├──────────────┤ └──────────────┘
│• 회원가입    │ │• 게시물 CRUD │ │• 알림 관리   │
│• 로그인      │ │• 포지션 검색 │ │• 기본 구조   │
│• JWT 발급    │ │• 키워드 검색 │ └──────────────┘
│• JWT 검증    │ │• 작성자별 조회│
│• 사용자 관리 │ └──────┬───────┘
└──────┬───────┘        │
       │                │
       ▼                ▼
┌──────────────┐ ┌──────────────┐
│DB:studify_users│ │DB:studify_posts│
│  MariaDB     │ │  MariaDB     │
│  Port 3306   │ │  Port 3306   │
│  (MariaDB)       │       │  (MariaDB)       │
└──────────────────┘       └──────────────────┘
```

---

## **🔧 기술 스택**

### **Backend (MSA)**
- **Spring Boot** 3.4.9
- **Spring Cloud** 2024.0.0
  - Spring Cloud Netflix Eureka (Service Discovery)
  - Spring Cloud Gateway (API Gateway)
  - Spring Cloud OpenFeign (Inter-service Communication)
- **Spring Security** 6.x + JWT (jjwt 0.12.6)
- **Spring Data JPA**
- **MariaDB** 10.6+
- **Lombok**
- **Validation**
- **Springdoc OpenAPI** (Swagger UI)

### **Frontend**
- **React** 18
- **Axios** (HTTP Client)

### **Infrastructure**
- **Java** 17
- **Gradle** 8.x
- **Git**

---

## **📁 프로젝트 구조**

```
team4/
├── studify-msa/                  # ✅ MSA 마이크로서비스들
│   │
│   ├── eureka-server/            # 🌐 서비스 디스커버리 (Port 8761)
│   │   ├── src/main/java/com/lgcns/studify/eureka/
│   │   │   └── EurekaServerApplication.java (@EnableEurekaServer)
│   │   ├── src/main/resources/
│   │   │   └── application.yml (포트: 8761, self-preservation: false)
│   │   └── build.gradle (spring-cloud-starter-netflix-eureka-server)
│   │
│   ├── api-gateway/              # 🚪 API 게이트웨이 (Port 8080)
│   │   ├── src/main/java/com/lgcns/studify/gateway/
│   │   │   ├── ApiGatewayApplication.java (@EnableDiscoveryClient)
│   │   │   └── config/
│   │   │       ├── CorsConfig.java (CORS 중앙 관리)
│   │   │       └── SecurityConfig.java (공개 경로 설정)
│   │   ├── src/main/resources/
│   │   │   └── application.yml (라우팅: user-service, post-service, notification-service)
│   │   └── build.gradle (spring-cloud-starter-gateway, eureka-client)
│   │
│   ├── user-service/             # 👥 회원 관리 서비스 (Port 8081)
│   │   ├── src/main/java/com/lgcns/studify/user/
│   │   │   ├── UserServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java (Spring Security)
│   │   │   │   └── WebConfig.java (CORS 제거 - Gateway에서 처리)
│   │   │   ├── controller/
│   │   │   │   └── UserController.java (CRUD, 페이징)
│   │   │   ├── service/
│   │   │   │   └── UserService.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java (JWT 발급/검증)
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   └── User.java (nickname, email, password, status)
│   │   │   │   ├── dto/
│   │   │   │   │   ├── UserRequestDTO.java
│   │   │   │   │   └── UserResponseDTO.java
│   │   │   │   └── enums/
│   │   │   │       └── UserStatus.java (ACTIVE, INACTIVE, BANNED, PENDING)
│   │   │   └── repository/
│   │   │       └── UserRepository.java
│   │   ├── src/main/resources/
│   │   │   └── application.yml (DB: studify_users, Eureka 등록)
│   │   └── build.gradle (spring-boot-starter-security, jjwt, mariadb)
│   │
│   ├── post-service/             # 📝 게시물 관리 서비스 (Port 8082)
│   │   ├── src/main/java/com/lgcns/studify/post/
│   │   │   ├── PostServiceApplication.java
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java (CORS 제거 - Gateway에서 처리)
│   │   │   ├── controller/
│   │   │   │   ├── PostController.java (CRUD, 검색, 필터링)
│   │   │   │   └── TestPostController.java (테스트용)
│   │   │   ├── service/
│   │   │   │   └── PostService.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   └── Post.java (title, content, category, status 등)
│   │   │   │   ├── dto/
│   │   │   │   │   ├── PostRequestDTO.java
│   │   │   │   │   └── PostResponseDTO.java
│   │   │   │   └── enums/
│   │   │   │       ├── Category.java (STUDY, PROJECT)
│   │   │   │       └── Position.java (BE, FE, PM, DESIGNER 등)
│   │   │   └── repository/
│   │   │       └── PostRepository.java
│   │   ├── src/main/resources/
│   │   │   └── application.yml (DB: studify_posts, Eureka 등록)
│   │   └── build.gradle (spring-boot-starter-data-jpa, mariadb)
│   │
│   └── notification-service/     # 🔔 알림 서비스 (Port 8083, 기본 구조)
│       ├── src/main/java/com/lgcns/studify/notification/
│       │   └── NotificationServiceApplication.java
│       ├── src/main/resources/
│       │   └── application.yml (Eureka 등록)
│       └── build.gradle
│
├── studify-fe-main/              # 🌐 React 프론트엔드 (Port 3000)
│   ├── src/
│   │   ├── api/
│   │   │   ├── axios.js (baseURL: http://localhost:8080)
│   │   │   └── post.js (getPosts, searchPostsByPosition 등)
│   │   ├── components/
│   │   │   ├── Navbar.jsx
│   │   │   ├── Search.jsx (포지션 필터 수정)
│   │   │   ├── Filters.jsx
│   │   │   └── PostCard.jsx
│   │   ├── pages/
│   │   │   ├── Home.jsx (게시글 목록)
│   │   │   ├── PostDetail.jsx (상세 보기)
│   │   │   ├── WritePage.jsx (작성/수정)
│   │   │   ├── SignIn.jsx (로그인)
│   │   │   ├── SignUp.jsx (회원가입)
│   │   │   └── MyPage.jsx (마이페이지)
│   │   └── mock/
│   │       └── posts.js (POSITIONS, TYPES 상수)
│   └── package.json
│
└── studify-be-main/              # 📦 기존 모놀리스 (참고용 유지)
    └── src/main/java/com/lgcns/studify_be/
│   │   │   └── application.yml
│   │   └── build.gradle
│   │
│   ├── post-service/             # 게시물 관리 서비스
│   │   ├── src/main/java/com/lgcns/studify/post/
│   │   │   ├── PostServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── PostController.java
│   │   │   ├── service/
│   │   │   │   └── PostService.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Post.java
│   │   │   │   │   ├── Category.java
│   │   │   │   │   ├── PostStatus.java
│   │   │   │   │   └── Position.java
│   │   │   │   └── dto/
│   │   │   │       ├── PostRequestDTO.java
│   │   │   │       └── PostResponseDTO.java
│   │   │   └── repository/
│   │   │       └── PostRepository.java
│   │   ├── src/main/resources/
│   │   │   └── application.yml
│   │   └── build.gradle
│   │
│   └── notification-service/     # 알림 서비스 (준비 중)
│       └── NotificationServiceApplication.java
│
└── studify-fe-main/              # React 프론트엔드
    ├── src/
    │   ├── api/
    │   │   ├── axios.js
    │   │   └── post.js
    │   ├── components/
    │   ├── pages/
    │   └── App.js
    └── package.json
```

---

## **🚀 실행 방법**

### **1. 사전 요구사항**
- Java 17+
- Node.js 16+
- MariaDB 10.6+
- Gradle 8.x

### **2. 데이터베이스 설정**

```sql
-- 데이터베이스 생성
CREATE DATABASE studify_users CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE studify_posts CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER 'studify'@'localhost' IDENTIFIED BY 'studify123';
GRANT ALL PRIVILEGES ON studify_users.* TO 'studify'@'localhost';
GRANT ALL PRIVILEGES ON studify_posts.* TO 'studify'@'localhost';
FLUSH PRIVILEGES;
```

### **3. 백엔드 서비스 실행 순서**

⚠️ **중요: 반드시 순서대로 실행해야 합니다!**

#### **방법 1: 터미널에서 백그라운드 실행 (권장)**

```powershell
# 1️⃣ Eureka Server 시작 (포트 8761)
cd c:\Users\user\Desktop\team4\studify-msa\eureka-server
.\gradlew.bat bootRun
# 백그라운드로 실행, 10초 대기

# 2️⃣ API Gateway 시작 (포트 8080)
cd c:\Users\user\Desktop\team4\studify-msa\api-gateway
.\gradlew.bat bootRun
# 백그라운드로 실행, 15초 대기

# 3️⃣ User Service 시작 (포트 8081)
cd c:\Users\user\Desktop\team4\studify-msa\user-service
.\gradlew.bat bootRun
# 백그라운드로 실행

# 4️⃣ Post Service 시작 (포트 8082)
cd c:\Users\user\Desktop\team4\studify-msa\post-service
.\gradlew.bat bootRun
# 백그라운드로 실행

# 5️⃣ (선택) Notification Service 시작 (포트 8083)
cd c:\Users\user\Desktop\team4\studify-msa\notification-service
.\gradlew.bat bootRun
```

#### **방법 2: 외부 PowerShell 창으로 실행**

```powershell
# 새 PowerShell 창에서 각각 실행
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\Users\user\Desktop\team4\studify-msa\eureka-server; .\gradlew.bat bootRun"

# 10초 대기 후
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\Users\user\Desktop\team4\studify-msa\api-gateway; .\gradlew.bat bootRun"

# 이하 동일...
```

### **4. 프론트엔드 실행**

```powershell
cd c:\Users\user\Desktop\team4\studify-fe-main

# 최초 1회만 실행
npm install

# 개발 서버 시작
npm start
# 브라우저에서 http://localhost:3000 자동 오픈
```

### **5. 서비스 정상 작동 확인**

| 서비스 | URL | 확인 방법 |
|--------|-----|-----------|
| **Eureka Dashboard** | http://localhost:8761 | 등록된 서비스 목록 확인 |
| **API Gateway** | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| **User Service** | http://localhost:8081/actuator/health | `{"status":"UP"}` |
| **Post Service** | http://localhost:8082/actuator/health | `{"status":"UP"}` |
| **Frontend** | http://localhost:3000 | React 앱 로딩 |

#### **PowerShell 테스트 명령어**

```powershell
# 게시글 목록 조회 (CORS 헤더 포함)
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/posts" -Headers @{"Origin"="http://localhost:3000"} -UseBasicParsing
Write-Host "Status:" $response.StatusCode
Write-Host "CORS Header:" $response.Headers["Access-Control-Allow-Origin"]
```

### **6. 문제 해결**

#### **CORS 오류 발생 시**
```
Access-Control-Allow-Origin header contains multiple values
```
✅ **해결**: 각 마이크로서비스의 CORS 설정을 제거하고 API Gateway에서만 처리

#### **400 Bad Request - positions parameter**
```
Required request parameter 'positions' for method parameter type List is not present
```
✅ **해결**: 프론트엔드에서 `position` → `positions`로 파라미터 이름 수정

#### **500 Error - "all" position 선택 시**
✅ **해결**: Search.jsx에서 `position !== "all"` 조건 추가

### **7. 서비스 종료**

```powershell
# 실행 중인 Java 프로세스 확인
Get-Process -Name java | Where-Object {$_.MainWindowTitle -like "*gradle*"}

# 특정 포트의 프로세스 종료
# 예: 8080 포트
netstat -ano | findstr "8080"
Stop-Process -Id <PID> -Force
```

| 서비스 | URL | 설명 |
|--------|-----|------|
| Eureka Dashboard | http://localhost:8761 | 등록된 서비스 확인 |
| User Service | http://localhost:8081/api/v1/users | 사용자 API |
| Post Service | http://localhost:8082/api/v1/posts | 게시물 API |
| API Gateway | http://localhost:8080 | 통합 진입점 |
| Frontend | http://localhost:3000 | React 앱 |

---

## **🔐 JWT 인증 구조**

### **인증 플로우**

```
1. 회원가입/로그인
   Client → User Service: POST /api/v1/auth/login
   User Service → Client: { accessToken, refreshToken }

2. API 요청 (인증 필요)
   Client → API Gateway: GET /api/v1/posts
                        Header: Authorization: Bearer {accessToken}
   API Gateway → JWT 검증
   API Gateway → Post Service: 요청 전달
   Post Service → Client: 응답

3. 토큰 갱신
   Client → User Service: POST /api/v1/auth/refresh
                         Body: { refreshToken }
   User Service → Client: { accessToken, refreshToken }
```

### **JWT 구성**

#### **Access Token**
```json
{
  "sub": "user@example.com",
  "iat": 1234567890,
  "exp": 1234571490
}
```
- 유효기간: 1시간
- 용도: API 요청 인증

#### **Refresh Token**
```json
{
  "sub": "user@example.com",
  "iat": 1234567890,
  "exp": 1235777490
}
```
- 유효기간: 2주
- 용도: Access Token 재발급
- DB 저장: User당 1개 정책

---

## **🗄️ 데이터베이스 구성**

### **studify_users (User Service)**

#### **users 테이블**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX uk_users_email (email)
);
```

#### **refresh_token 테이블**
```sql
CREATE TABLE refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    token VARCHAR(512) NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### **studify_posts (Post Service)**

#### **posts 테이블**
```sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    position VARCHAR(50),
    start_date DATE,
    end_date DATE,
    max_members INT,
    current_members INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
```

---

---

## **📡 API 엔드포인트**

### **User Service (8081)**

#### **인증 API**
```http
POST   /api/v1/auth/login          # 로그인
POST   /api/v1/auth/refresh        # 토큰 갱신
POST   /api/v1/auth/logout         # 로그아웃
```

#### **사용자 API**
```http
POST   /api/v1/users/signup        # 회원가입
GET    /api/v1/users               # 전체 사용자 조회
GET    /api/v1/users/{id}          # 사용자 상세 조회
GET    /api/v1/users/email?email=  # 이메일로 조회
PUT    /api/v1/users/{id}          # 사용자 정보 수정
DELETE /api/v1/users/{id}          # 사용자 삭제 (Soft Delete)
GET    /api/v1/users/check/email   # 이메일 중복 검사
GET    /api/v1/users/check/nickname # 닉네임 중복 검사
```

### **Post Service (8082)**

```http
POST   /api/v1/posts                          # 게시물 생성
GET    /api/v1/posts                          # 게시물 전체 조회
GET    /api/v1/posts/{postId}                 # 게시물 상세 조회
PUT    /api/v1/posts/{postId}                 # 게시물 수정
DELETE /api/v1/posts/{postId}                 # 게시물 삭제
GET    /api/v1/posts/author/{authorId}       # 작성자별 조회
GET    /api/v1/posts/search?keyword=         # 키워드 검색
GET    /api/v1/posts/search/position?positions=  # 포지션별 검색
GET    /api/v1/posts/health                   # 헬스체크
```

### **API Gateway Routes (8080)**

```
GET    /api/v1/posts              → POST-SERVICE (lb://post-service)
GET    /studify/api/v1/post/**    → POST-SERVICE (RewritePath 적용)
GET    /api/v1/users/**           → USER-SERVICE (lb://user-service)
GET    /api/auth/**               → USER-SERVICE (lb://user-service)
GET    /api/v1/notifications/**   → NOTIFICATION-SERVICE
```

### **API 요청 예시**

#### **회원가입 (Gateway 경유)**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "테스터",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### **게시물 전체 조회 (Gateway 경유)**
```bash
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Origin: http://localhost:3000"
```

#### **포지션별 검색**
```bash
curl -X GET "http://localhost:8080/api/v1/posts/search/position?positions=BE&positions=FE" \
  -H "Origin: http://localhost:3000"
```

#### **게시물 생성 (Gateway 경유)**
```bash
curl -X POST http://localhost:8080/api/v1/posts \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{
    "title": "Spring Boot 스터디 모집",
    "content": "Spring Boot 3.x 스터디원 모집합니다",
    "category": "study",
    "position": ["be", "fe"],
    "recruitmentCount": 5,
    "deadline": "2025-12-31T00:00:00",
    "meetingType": "온라인",
    "duration": "3개월",
    "techStack": ["Spring Boot", "JPA"]
  }'
```

---

---

## **🎓 학습 포인트 및 주요 구현 사항**

### **1. 마이크로서비스 아키텍처 (MSA)**
- ✅ 모놀리스를 기능별로 분리하는 방법
- ✅ 서비스 간 독립적인 배포 전략
- ✅ 데이터베이스 분리 및 관리

### **2. Spring Cloud**
- ✅ **Eureka Server**: 서비스 등록 및 디스커버리
- ✅ **API Gateway**: 라우팅, 로드밸런싱, 필터링
- ✅ **OpenFeign**: 서비스 간 통신 (준비 중)

### **3. 인증 및 보안**
- ✅ JWT 기반 Stateless 인증
- ✅ Access Token / Refresh Token 전략
- ✅ Spring Security 통합
- ✅ API Gateway에서의 인증 필터링

### **4. 실무 패턴**
- ✅ REST API 설계
- ✅ DTO 패턴
- ✅ Repository 패턴
- ✅ Service Layer 분리
- ✅ Exception Handling

### **5. DevOps 기초**
- ✅ Gradle 멀티 모듈 프로젝트
- ✅ Profile별 설정 관리
- ✅ 서비스 의존성 관리

---

## **🔍 MSA 전환 과정에서 해결한 문제들**

### **1. 패키지 구조 문제**
- **문제**: 패키지 선언이 `main.java.com.lgcns...`로 잘못 설정
- **해결**: 모든 파일의 패키지를 `com.lgcns.studify...`로 수정
- **교훈**: Spring Boot의 Component Scan은 패키지 구조에 민감

### **2. 의존성 관리**
- **문제**: JWT, Spring Security, Validation 의존성 누락
- **해결**: 각 서비스의 build.gradle에 필요한 의존성 추가
- **교훈**: MSA에서는 각 서비스가 독립적인 의존성 관리 필요

### **3. JWT API 변경**
- **문제**: jjwt 0.12.x에서 API 변경 (`parserBuilder()` → `parser()`)
- **해결**: 최신 API에 맞게 JwtTokenProvider 수정
- **교훈**: 라이브러리 버전 업그레이드 시 Breaking Change 확인 필수

### **4. 데이터베이스 분리**
- **문제**: User와 Post 데이터를 어떻게 분리할 것인가?
- **해결**: DB를 `studify_users`, `studify_posts`로 완전 분리
- **교훈**: MSA는 Database per Service 패턴 권장

### **5. 서비스 디스커버리**
- **문제**: 서비스들이 서로를 어떻게 찾을 것인가?
- **해결**: Eureka Server를 통한 동적 서비스 등록/탐색
- **교훈**: MSA에서는 하드코딩된 URL 대신 서비스명 사용

---

## **📊 성능 및 확장성**

### **✅ 구현 완료된 기능**
- ✅ **Spring Cloud Eureka** - 서비스 디스커버리
- ✅ **Spring Cloud Gateway** - API Gateway 및 라우팅
- ✅ **CORS 중앙 관리** - CorsWebFilter 적용
- ✅ **JWT 인증** - User Service에서 발급/검증
- ✅ **데이터베이스 분리** - studify_users, studify_posts
- ✅ **독립적 배포** - 서비스별 독립 실행
- ✅ **RewritePath 필터** - `/studify` prefix 처리
- ✅ **포지션별 검색** - List<String> 파라미터 처리
- ✅ **에러 처리** - 400/500 에러 해결

### **🔄 향후 개선 가능 영역**
- 🔄 API Gateway에 JWT 인증 필터 적용 (현재 Security만 적용)
- 🔄 서비스 간 통신에 OpenFeign 적용
- 🔄 Config Server를 통한 중앙화된 설정 관리
- 🔄 Circuit Breaker 패턴 적용 (Resilience4j)
- 🔄 분산 추적 시스템 (Zipkin, Sleuth)
- 🔄 댓글/알림 기능 MSA 전환
- 🔄 Docker Compose로 컨테이너화

---

## **🎯 MSA의 장단점**

### **✅ 장점**
- **독립 배포**: User Service만 수정했다면 User Service만 재배포
- **확장 유연성**: 트래픽이 많은 Post Service만 인스턴스 증설 가능
- **장애 격리**: Post Service 장애 시에도 User Service는 정상 작동
- **기술 자율성**: 서비스마다 다른 프레임워크/DB 선택 가능

### **❌ 단점**
- **복잡성 증가**: 서비스가 많아질수록 관리 포인트 증가
- **네트워크 통신**: 서비스 간 HTTP 통신으로 인한 오버헤드
- **분산 트랜잭션**: 여러 서비스에 걸친 트랜잭션 관리 복잡
- **디버깅 어려움**: 여러 서비스의 로그를 종합적으로 분석 필요

---

## **🤝 기여 방법**

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## **📝 라이선스**

이 프로젝트는 학습 목적으로 제작되었습니다.

---

## **👥 팀 정보**

**Team 4 - Studify MSA Project**

---

## **📚 참고 자료**

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Netflix Eureka](https://github.com/Netflix/eureka)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [JWT.io](https://jwt.io/)
- [Microservices Patterns](https://microservices.io/patterns/index.html)

---

**🎉 Studify MSA 프로젝트에 오신 것을 환영합니다!**

**현재는 User Service와 Post Service만 MSA로 전환되었지만, 점진적으로 다른 서비스들도 전환할 예정입니다.**

---

# 인프라 작업 ( FE / BE )
## 🚀 배포 및 테스트

본 프로젝트는 프론트엔드(Vercel)와 백엔드(AWS EC2)가 분리 배포되어 있습니다.

### 1. 🖥️ 서비스 접속 및 테스트

가장 간단한 테스트 방법은 배포된 Vercel URL에 접속하는 것입니다.

* **Vercel (FE) 접속 URL:** [https://msateam4.vercel.app](https://msateam4.vercel.app)

위 주소로 접속하여 회원가입, 로그인, 게시글 작성 등 모든 기능을 테스트할 수 있습니다.

### 2. ⚙️ 배포 아키텍처

이 서비스가 동작하는 방식은 다음과 같습니다.

1.  **사용자 (브라우저)**가 [https://msateam4.vercel.app](https://msateam4.vercel.app) 에 접속합니다.
2.  **Vercel (React)**이 `REACT_APP_API_BASE_URL` 환경 변수(`http://[EC2 퍼블릭 IP 주소]/api`)를 읽어 백엔드 API를 호출합니다.
3.  **AWS EC2 (Nginx)**가 `80`번 포트로 `/api` 요청을 받습니다.
4.  **Nginx**가 이 요청을 서버 내부의 `API-Gateway` 컨테이너(`127.0.0.1:8080`)로 전달(Proxy)합니다.
5.  **API-Gateway**가 `Eureka`에 등록된 각 마이크로서비스(User, Post 등)를 호출하여 작업을 처리합니다.

* **프론트엔드:** Vercel
* **백엔드 API 엔드포인트:** `http://[EC2 퍼블릭 IP 주소]/api`
* **백엔드 인프라:** AWS EC2 (Docker + Nginx)

### 3. 🩺 백엔드 상태 확인 (EC2 서버 SSH 접속 시)

백엔드 서버가 정상적으로 작동하는지 확인하는 방법입니다.

1.  EC2 서버에 SSH로 접속합니다.
2.  `docker-compose.yml` 파일이 있는 폴더로 이동합니다.
    ```bash
    cd ~/studify-be
    ```
3.  Docker Hub에서 최신 이미지(수정본) 다운로드 합니다.
    ```bash
    sudo docker compose pull
    ```
4. 모든 컨테이너를 시작(실행)시킵니다.
   ```bash
   sudo docker compose up -d
   ```
   
5.  8개의 모든 컨테이너가 `Up` (실행 중) 상태인지 확인합니다.
    ```bash
    sudo docker compose ps
    ```
6.  Eureka 서버에 모든 서비스가 정상 등록되었는지 확인합니다.
    ```bash
    curl -s http://localhost:8761/eureka/apps | grep '<name>'
    ```
    * **정상 출력 (예시):** `API-GATEWAY`, `USER-SERVICE`, `POST-SERVICE`, `NOTIFICATION-SERVICE` 4개의 이름이 모두 표시되어야 합니다.

확인해야 할 사항

- [ ] vercel에서 정상적으로 프론트엔드 화면이 나오는지 확인
- [ ] EC2 환경에서 백엔드 서버가 정상적으로 작동 하는지 확인

