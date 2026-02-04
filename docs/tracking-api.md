# 2주 트래킹(설문 기반) API 명세

## 공통 응답 포맷
모든 API는 `ApiResponse` 형식을 사용합니다.

### 성공 예시
```json
{
  "isSuccess": true,
  "code": "S200",
  "message": "OK",
  "result": {}
}
```

### 실패 예시
```json
{
  "isSuccess": false,
  "code": "E40011",
  "message": "잘못된 요청입니다.",
  "result": null
}
```

---

## 1) 오늘의 질문 조회
- **Method**: `GET`
- **Path**: `/api/v1/survey`
- **Auth**: Bearer Token

### Response
```json
{
  "isSuccess": true,
  "code": "S200",
  "message": "OK",
  "result": {
    "questionId": 123,
    "questionText": "오늘 기분은 어땠나요?",
    "isAnswered": false
  }
}
```

### 필드 설명
- `questionId`: 오늘 질문 ID
- `questionText`: 오늘 질문 텍스트
- `isAnswered`: 오늘 이미 답변했는지 여부

### 실패 케이스
- 인증 실패
- 서버 내부 오류

---

## 2) 오늘의 질문 답변 저장
- **Method**: `POST`
- **Path**: `/api/v1/survey/answer`
- **Auth**: Bearer Token
- **Body**:
  - `questionId` (Long)
  - `answer` (Integer)

### Request
```json
{
  "questionId": 123,
  "answer": 1
}
```

### Answer 값 매핑
- `1` = YES (그렇다)
- `2` = NEUTRAL (보통이다)
- `3` = NO (아니다)

### Response
```json
{
  "isSuccess": true,
  "code": "S200",
  "message": "OK",
  "result": null
}
```

### 실패 케이스
- 이미 답변한 경우
  - `code`: `E40011`
  - `message`: `이미 오늘의 질문에 답변했습니다.`
- `questionId` 누락
- `answer` 누락 또는 범위 외
- 인증 실패

---

## 3) 2주 트래킹 리포트 조회
- **Method**: `GET`
- **Path**: `/api/v1/tracking/report`
- **Auth**: Bearer Token
- **기간 기준**: 오늘 포함 최근 14일 (today ~ today-13일)

### Response
```json
{
  "isSuccess": true,
  "code": "S200",
  "message": "OK",
  "result": {
    "trackingType": "IMPROVING",
    "totalScore": 18,
    "praiseDayCount": 4,
    "message": "마음이 점점 더 단단해지고 있어요! 긍정적인 변화가 보입니다. 🌱"
  }
}
```

### trackingType 의미
- `ALWAYS_GOOD`: 2주 총점이 높음
- `IMPROVING`: 2주차 평균이 1주차보다 개선됨
- `NEEDS_COMFORT`: 기본값 (그 외)

### 실패 케이스
- 인증 실패
- 사용자 없음
  - `code`: `E40401`
  - `message`: `해당 사용자를 찾을 수 없습니다.`

---

# 프론트 플로우 가이드
1. 앱 진입 시 → `GET /api/v1/survey`
2. 설문 답변 제출 → `POST /api/v1/survey/answer`
3. 2주 리포트 표시 시 → `GET /api/v1/tracking/report`
