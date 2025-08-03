# AI 주석 시스템 사용 가이드

## 개요
이 시스템은 GitHub 커밋에 AI가 생성한 주석을 적용하는 기능을 제공합니다. 
**수정 가능한 워크플로우**를 통해 사용자가 주석을 검토하고 수정한 후 푸시할 수 있습니다.

## API 워크플로우

### 1단계: 주석 미리보기 생성
```
POST /api/github/repositories/{owner}/{repo}/commits/{sha}/preview-comments?branch=main
```

**응답 예시:**
```json
{
  "commitSha": "abc123def456",
  "commitMessage": "feat: Add new user service",
  "branch": "main",
  "sessionId": "uuid-session-id",
  "files": {
    "src/main/java/UserService.java": {
      "filename": "src/main/java/UserService.java",
      "originalContent": "public class UserService {\n    public void createUser() {\n        // 구현\n    }\n}",
      "commentedContent": "public class UserService {\n    // 새 기능: 사용자 관리를 위한 서비스 클래스\n    public void createUser() {\n        // 새 기능: 새로운 사용자를 생성하는 메서드\n        // 구현\n    }\n}",
      "addedComments": [
        {
          "lineNumber": 1,
          "comment": "새 기능: 사용자 관리를 위한 서비스 클래스",
          "codeLine": "public class UserService {"
        },
        {
          "lineNumber": 2,
          "comment": "새 기능: 새로운 사용자를 생성하는 메서드",
          "codeLine": "public void createUser() {"
        }
      ],
      "modified": true
    }
  }
}
```

### 2단계: 주석 수정 (선택사항)
```
PUT /api/github/comments/session/{sessionId}
```

**요청 본문:**
```json
{
  "sessionId": "uuid-session-id",
  "updatedFiles": {
    "src/main/java/UserService.java": "public class UserService {\n    // 사용자 관리 서비스 - 수정된 주석\n    public void createUser() {\n        // 사용자 생성 로직 - 수정된 주석\n        // 구현\n    }\n}"
  }
}
```

### 3단계: GitHub에 푸시
```
POST /api/github/comments/session/{sessionId}/push
```

**응답 예시:**
```json
{
  "success": true,
  "message": "성공적으로 1개 파일에 주석을 적용하고 커밋했습니다. 커밋 SHA: def456gh",
  "sessionId": "uuid-session-id"
}
```

### 선택사항: 세션 삭제
```
DELETE /api/github/comments/session/{sessionId}
```

## 기존 방식 (바로 푸시)
기존의 바로 푸시하는 방식도 여전히 사용 가능합니다:
```
POST /api/github/repositories/{owner}/{repo}/commits/{sha}/apply-comments?branch=main
```

## 세션 관리
- 세션은 1시간 후 자동으로 만료됩니다
- 세션은 메모리에 저장되므로 서버 재시작 시 초기화됩니다
- 운영 환경에서는 Redis나 데이터베이스 사용을 권장합니다

## 지원 파일 형식
- Java (.java)
- JavaScript/TypeScript (.js, .ts, .jsx, .tsx)
- Python (.py)
- CSS/SCSS (.css, .scss)
- HTML (.html)
- Vue (.vue)
- PHP (.php)
- Go (.go)
- Rust (.rs)
- C/C++ (.cpp, .c, .h)

## 주석 생성 규칙
- 커밋 메시지를 분석하여 컨텍스트 기반 주석 생성
- 메서드, 클래스, 함수 선언에 주석 추가
- 기존 주석이 있는 라인은 건너뜀
- 파일 형식에 맞는 주석 스타일 사용

## 프론트엔드 구현 예시

### React 컴포넌트 예시
```javascript
import React, { useState } from 'react';

function CommitCommentEditor() {
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);

  // 1. 미리보기 생성
  const generatePreview = async (owner, repo, sha, branch = 'main') => {
    setLoading(true);
    try {
      const response = await fetch(`/api/github/repositories/${owner}/${repo}/commits/${sha}/preview-comments?branch=${branch}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await response.json();
      setPreview(data);
    } catch (error) {
      console.error('미리보기 생성 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  // 2. 주석 수정
  const updateComments = async (sessionId, updatedFiles) => {
    try {
      await fetch(`/api/github/comments/session/${sessionId}`, {
        method: 'PUT',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ sessionId, updatedFiles })
      });
    } catch (error) {
      console.error('주석 수정 실패:', error);
    }
  };

  // 3. 푸시
  const pushComments = async (sessionId) => {
    try {
      const response = await fetch(`/api/github/comments/session/${sessionId}/push`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const result = await response.json();
      alert(result.message);
    } catch (error) {
      console.error('푸시 실패:', error);
    }
  };

  return (
    <div>
      {/* UI 구현 */}
    </div>
  );
}
```

## 에러 처리
- 400: 잘못된 요청 데이터
- 401: 인증 실패
- 404: 세션을 찾을 수 없음 (만료 또는 존재하지 않음)
- 500: 서버 내부 오류

모든 에러 응답은 다음 형식을 따릅니다:
```json
{
  "success": false,
  "error": "에러 메시지"
}
```
