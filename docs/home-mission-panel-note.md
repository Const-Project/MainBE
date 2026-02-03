# 홈 미션 패널 통합 정리

## 요약
- 홈 화면용 데이터는 `GET /api/v1/home`, `GET /api/v1/home/panel`에서 제공한다.
- 오늘 미션 완료 여부는 홈 API 응답에 이미 포함되어 있으므로 별도 MissionPanel API는 제거했다.

## 홈 API에서 제공하는 미션 정보
- `GET /api/v1/home`
  - `todayMissions`: 오늘의 미션 목록 + 완료 여부
  - 항목 예시: `missionType`, `missionTitle`, `isCompleted`
- `GET /api/v1/home/panel`
  - `isDairyCompleted`, `isQuizCompleted`, `isCheckingCompleted`
  - 소망나무 관련 정보 포함

## 제거된 구성
- `UserDailyMissionService.getMissionPanelData(...)`
- `MissionPanelResponse` DTO
- (컨트롤러는 이미 주석 처리 상태였음)

## 결론
홈 화면에서 필요한 미션 상태 표시는 홈 API를 기준으로 통합한다.
