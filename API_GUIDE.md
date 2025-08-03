# MC Server API 가이드

## 개요
이 프로젝트는 Spring Boot와 PostgreSQL/H2를 사용한 GitHub 레포지토리 관리 시스템입니다.
JWT 토큰 기반 인증을 제공하며, GitHub API 연동 및 레포지토리 저장 기능을 포함합니다.

## 설치 및 실행

### 개발 환경 (H2 데이터베이스)
개발 환경에서는 H2 인메모리 데이터베이스를 사용할 수 있습니다:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 운영 환경 (PostgreSQL)

#### 1. PostgreSQL 설치 및 설정
```sql
-- 데이터베이스 생성
CREATE DATABASE mc_server_db;

-- 사용자 생성 (선택사항)
CREATE USER your_username WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE mc_server_db TO your_username;
```

#### 2. application.properties 수정
`src/main/resources/application.properties` 파일에서 데이터베이스 설정을 수정하세요:
```properties
# PostgreSQL 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/mc_server_db
spring.datasource.username=your_actual_username
spring.datasource.password=your_actual_password
```

#### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

## 프로젝트 구조
```
src/main/java/com/mc/mc_server/
├── config/
│   ├── SecurityConfig.java          # Spring Security 설정
│   ├── SwaggerConfig.java           # Swagger 설정
│   └── RestTemplateConfig.java      # RestTemplate 설정
├── controller/
│   ├── AuthController.java          # 인증 관련 API
│   ├── GitHubController.java        # GitHub API 관련
│   ├── SavedRepositoryController.java # 저장된 레포지토리 관리
│   └── TestController.java          # 테스트 API
├── dto/
│   ├── LoginRequest.java            # 로그인 요청 DTO
│   ├── SignupRequest.java           # 회원가입 요청 DTO
│   ├── AuthResponse.java            # 인증 응답 DTO
│   ├── GitHubRepository.java        # GitHub 레포지토리 DTO
│   ├── GitHubCommit.java            # GitHub 커밋 DTO
│   ├── GitHubCommitDetail.java      # GitHub 커밋 상세 DTO
│   ├── GitHubFile.java              # GitHub 파일 DTO
│   ├── SaveRepositoryRequest.java   # 레포지토리 저장 요청 DTO
│   └── SavedRepositoryResponse.java # 저장된 레포지토리 응답 DTO
├── entity/
│   ├── User.java                    # 사용자 엔티티
│   └── SavedRepository.java         # 저장된 레포지토리 엔티티
├── filter/
│   └── JwtAuthenticationFilter.java # JWT 인증 필터
├── repository/
│   ├── UserRepository.java          # 사용자 레포지토리
│   └── SavedRepositoryRepository.java # 저장된 레포지토리 레포지토리
├── service/
│   ├── UserService.java             # 사용자 서비스
│   ├── GitHubService.java           # GitHub 서비스
│   └── SavedRepositoryService.java  # 저장된 레포지토리 서비스
├── util/
│   └── JwtUtil.java                 # JWT 유틸리티
└── McServerApplication.java         # 메인 애플리케이션
```

## API 엔드포인트

### 1. 회원가입
- **URL**: `POST /api/auth/signup`
- **Content-Type**: `application/json`
- **Body**:
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```
- **응답**:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "message": "회원가입이 완료되었습니다."
}
```

### 2. 로그인
- **URL**: `POST /api/auth/login`
- **Content-Type**: `application/json`
- **Body**:
```json
{
    "email": "user@example.com",
    "password": "password123"
}
```
- **응답**:
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "message": "로그인 성공"
}
```

### 3. 현재 사용자 정보 조회
- **URL**: `GET /api/auth/me`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "id": 1,
    "email": "user@example.com",
    "createdAt": "2025-01-01T12:00:00",
    "updatedAt": "2025-01-01T12:00:00"
}
```

### 4. 토큰 검증
- **URL**: `POST /api/auth/validate-token`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "valid": true,
    "email": "user@example.com"
}
```

### 5. GitHub API

#### GitHub 레포지토리 목록 조회
- **URL**: `GET /api/github/repositories`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
[
    {
        "id": 123456789,
        "name": "my-awesome-project",
        "fullName": "username/my-awesome-project",
        "description": "This is an awesome project",
        "htmlUrl": "https://github.com/username/my-awesome-project",
        "defaultBranch": "main",
        "privateRepo": false,
        "createdAt": "2023-01-01T00:00:00Z",
        "updatedAt": "2023-12-01T00:00:00Z"
    }
]
```

#### GitHub 레포지토리 커밋 목록 조회
- **URL**: `GET /api/github/repositories/{owner}/{repo}/commits`
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `page`: 페이지 번호 (기본값: 1)
  - `perPage`: 페이지당 항목 수 (기본값: 30)
- **응답**: 커밋 목록 배열

#### GitHub 커밋 상세 정보 조회
- **URL**: `GET /api/github/repositories/{owner}/{repo}/commits/{sha}`
- **Headers**: `Authorization: Bearer {token}`
- **응답**: 커밋 상세 정보 및 변경 파일 목록

### 6. 저장된 레포지토리 관리

#### 레포지토리 저장
- **URL**: `POST /api/saved-repositories`
- **Headers**: `Authorization: Bearer {token}`
- **Content-Type**: `application/json`
- **Body**:
```json
{
    "repositoryId": 123456789,
    "repositoryName": "my-awesome-project",
    "repositoryFullName": "username/my-awesome-project",
    "repositoryDescription": "This is an awesome project",
    "repositoryUrl": "https://github.com/username/my-awesome-project",
    "defaultBranch": "main",
    "isPrivate": false,
    "repositoryCreatedAt": "2023-01-01T00:00:00Z",
    "repositoryUpdatedAt": "2023-12-01T00:00:00Z"
}
```
- **응답**:
```json
{
    "id": 1,
    "repositoryId": 123456789,
    "repositoryName": "my-awesome-project",
    "repositoryFullName": "username/my-awesome-project",
    "repositoryDescription": "This is an awesome project",
    "repositoryUrl": "https://github.com/username/my-awesome-project",
    "defaultBranch": "main",
    "isPrivate": false,
    "savedAt": "2023-12-15T10:30:00",
    "repositoryCreatedAt": "2023-01-01T00:00:00Z",
    "repositoryUpdatedAt": "2023-12-01T00:00:00Z"
}
```

#### 저장된 레포지토리 목록 조회
- **URL**: `GET /api/saved-repositories`
- **Headers**: `Authorization: Bearer {token}`
- **응답**: 저장된 레포지토리 목록 배열

#### 레포지토리 저장 여부 확인
- **URL**: `GET /api/saved-repositories/check/{repositoryId}`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "isSaved": true
}
```

#### 저장된 레포지토리 삭제
- **URL**: `DELETE /api/saved-repositories/{repositoryId}`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "message": "저장된 레포지토리가 성공적으로 삭제되었습니다."
}
```

#### 저장된 레포지토리 개수 조회
- **URL**: `GET /api/saved-repositories/count`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "count": 5
}
```

### 7. 테스트 엔드포인트

#### 공개 엔드포인트 (인증 불필요)
- **URL**: `GET /api/test/public`
- **응답**:
```json
{
    "message": "이것은 공개 엔드포인트입니다. 인증이 필요하지 않습니다."
}
```

#### 보호된 엔드포인트 (인증 필요)
- **URL**: `GET /api/test/protected`
- **Headers**: `Authorization: Bearer {token}`
- **응답**:
```json
{
    "message": "이것은 보호된 엔드포인트입니다. 인증이 필요합니다.",
    "user": "user@example.com",
    "authorities": []
}
```

## cURL 예제

### 인증 관련

#### 회원가입
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 현재 사용자 정보 조회
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### GitHub API

#### GitHub 레포지토리 목록 조회
```bash
curl -X GET http://localhost:8080/api/github/repositories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### GitHub 레포지토리 커밋 목록 조회
```bash
curl -X GET http://localhost:8080/api/github/repositories/username/repo-name/commits \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 저장된 레포지토리 관리

#### 레포지토리 저장
```bash
curl -X POST http://localhost:8080/api/saved-repositories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryId": 123456789,
    "repositoryName": "my-awesome-project",
    "repositoryFullName": "username/my-awesome-project",
    "repositoryDescription": "This is an awesome project",
    "repositoryUrl": "https://github.com/username/my-awesome-project",
    "defaultBranch": "main",
    "isPrivate": false,
    "repositoryCreatedAt": "2023-01-01T00:00:00Z",
    "repositoryUpdatedAt": "2023-12-01T00:00:00Z"
  }'
```

#### 저장된 레포지토리 목록 조회
```bash
curl -X GET http://localhost:8080/api/saved-repositories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 레포지토리 저장 여부 확인
```bash
curl -X GET http://localhost:8080/api/saved-repositories/check/123456789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 저장된 레포지토리 삭제
```bash
curl -X DELETE http://localhost:8080/api/saved-repositories/123456789 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 에러 응답
모든 에러는 다음 형식으로 반환됩니다:
```json
{
    "error": "에러 메시지"
}
```

## 주요 기능
- **인증 시스템**
  - JWT 기반 인증
  - BCrypt 비밀번호 암호화
  - Spring Security 통합
- **GitHub API 연동**
  - GitHub 레포지토리 목록 조회
  - 레포지토리 커밋 히스토리 조회
  - 커밋 상세 정보 및 변경 파일 조회
- **레포지토리 관리**
  - 관심 있는 레포지토리 저장
  - 저장된 레포지토리 목록 관리
  - 레포지토리 저장/삭제 기능
- **데이터베이스**
  - PostgreSQL/H2 데이터베이스 지원
  - JPA/Hibernate ORM
- **기타**
  - 입력 검증 (이메일 형식, 비밀번호 최소 길이)
  - CORS 지원
  - Swagger API 문서화

## 보안 설정
- JWT 토큰 만료 시간: 24시간 (설정에서 변경 가능)
- 비밀번호 최소 길이: 6자
- BCrypt 암호화 사용
- 세션 사용 안함 (Stateless)

## 데이터베이스 스키마

### users 테이블
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    github_token VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### saved_repositories 테이블
```sql
CREATE TABLE saved_repositories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    repository_id BIGINT NOT NULL,
    repository_name VARCHAR(255) NOT NULL,
    repository_full_name VARCHAR(255) NOT NULL,
    repository_description TEXT,
    repository_url VARCHAR(255) NOT NULL,
    default_branch VARCHAR(255),
    is_private BOOLEAN,
    created_at TIMESTAMP,
    repository_created_at VARCHAR(255),
    repository_updated_at VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, repository_id)
);
```

## GitHub 토큰 설정
GitHub API를 사용하기 위해서는 개인 액세스 토큰이 필요합니다:

1. GitHub → Settings → Developer settings → Personal access tokens
2. "Generate new token" 클릭
3. 필요한 권한 선택 (repo, user 등)
4. 생성된 토큰을 사용자 계정에 저장

```bash
# 토큰 저장 예제 (데이터베이스에서 직접 업데이트)
UPDATE users SET github_token = 'your_github_token_here' WHERE email = 'your_email@example.com';
```
