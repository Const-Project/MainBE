# 성능 분석 리포트: MainBE 코드베이스

## 요약

| 심각도 | 문제 유형 | 파일 | 영향 |
|--------|----------|------|------|
| 🔴 심각 | N+1 쿼리 (FollowService) | FollowService.java | 팔로워 수에 비례한 쿼리 증가 |
| 🔴 심각 | 스케줄 알림 루프 | NotificationService.java | 유저 수 × 3 쿼리 발생 |
| 🔴 심각 | ORDER BY RAND() | DiaryRepository.java | 전체 테이블 스캔 |
| 🔴 심각 | 프로필 조회 7+ 쿼리 | UserService.java | DB 커넥션 풀 고갈 위험 |
| 🟠 중간 | 캐싱 미적용 | FeedService.java | 반복 조회로 DB 부하 |
| 🟠 중간 | 중복 Stream 연산 | HomeService.java | CPU 낭비 |
| 🟠 중간 | 과도한 Fetch Join | UserRepository.java | 카테시안 곱 발생 |
| 🟠 중간 | 메모리 낭비 (2배 fetching) | FeedService.java | 불필요한 객체 생성 |
| 🟡 낮음 | 제한 없는 전체 조회 | AdminService.java | OOM 위험 |
| 🟡 낮음 | 인덱스 미적용 | 여러 Repository | 테이블 풀 스캔 |

---

## ~~1. N+1 쿼리 - FollowService 팔로워/팔로잉 조회 🔴~~ **해결 완료**

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/social/follow/service/FollowService.java`
- **라인**: 83-104 (getFollowers, getFollowing 메서드)

### 문제 코드
```java
List<User> userList = followRepository.findByFollowing(user).stream()
    .map(Follow::getFollower).toList();

return userList.stream()
    .map(member -> FollowResponseDTO.builder()
        .username(member.getNickname())
        .userImageUrl(member.getAvatarList().isEmpty()  // ← N+1 발생!
            ? null
            : member.getAvatarList().get(0).getImageUrl())
        .userId(member.getId())
        .build())
    .toList();
```

### 문제 설명
- `followRepository.findByFollowing(user)`로 Follow 목록 조회 (1번 쿼리)
- 각 User의 `getAvatarList()` 호출 시 Lazy Loading으로 추가 쿼리 발생
- **팔로워 100명 = 101번 쿼리**

### 해결 방안
```java
@Query("SELECT f FROM Follow f " +
       "JOIN FETCH f.follower u " +
       "LEFT JOIN FETCH u.avatarList " +
       "WHERE f.following = :user")
List<Follow> findByFollowingWithFollowerAndAvatars(@Param("user") User user);
```

---

## 2. 스케줄링 알림 대량 쿼리 - NotificationService 🔴

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/member/notification/service/NotificationService.java`
- **라인**: 288-301, 304-317, 320-344

### 문제 코드
```java
@Scheduled(cron = "0 0 6 * * *")
public void sendSunshineNotification() {
    List<User> users = userRepository.findAll();  // 모든 유저 조회
    for (User user : users) {
        if (!Boolean.TRUE.equals(user.getNotificationEnabled())) {
            continue;
        }
        send(user, user, NotificationType.SUNSHINE, "/garden", null);  // 루프 내 send()
    }
}
```

### 문제 설명
- 매일 6시, 12시, 8시간마다 실행되는 스케줄러
- `userRepository.findAll()`로 모든 유저 조회
- 각 유저마다 `send()` 호출 → emitter 조회 + deviceToken 조회
- **유저 1,000명 = 3,000번+ 쿼리**

### 해결 방안
- 알림 활성화된 유저만 조회: `findAllByNotificationEnabledTrue()`
- 배치 처리로 deviceToken 일괄 조회
- 비동기 처리 또는 메시지 큐 사용

---

## 3. ORDER BY RAND() 사용 - DiaryRepository 🔴

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/mission/diary/domain/repository/DiaryRepository.java`
- **라인**: 135-158

### 문제 코드
```java
@Query(value = """
    SELECT d.diary_id
    FROM diaries d
    WHERE d.is_public = true
    AND (COALESCE(:excludeIds, NULL) IS NULL OR d.diary_id NOT IN (:excludeIds))
    AND d.user_id NOT IN (:blockedUserIds)
    ORDER BY RAND()
    LIMIT :limit
    """, nativeQuery = true)
List<Long> findRandomPublicDiaryIdsExcludingAndBlocked(...);
```

### 문제 설명
- `ORDER BY RAND()`는 MySQL에서 **전체 테이블을 스캔**하고 정렬
- 일기 100만개 = 매번 100만 행 스캔 후 정렬
- **시간 복잡도: O(n log n)**

### 해결 방안
```sql
-- 방법 1: 랜덤 ID 범위 사용
SELECT * FROM diaries
WHERE id >= (SELECT FLOOR(RAND() * (SELECT MAX(id) FROM diaries)))
LIMIT 10;

-- 방법 2: 별도 랜덤 테이블 유지
-- 방법 3: 애플리케이션에서 랜덤 처리
```

---

## 4. 과도한 쿼리 수 - UserService.getUserProfile 🔴

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/member/user/service/UserService.java`
- **라인**: 175-268

### 문제 코드
```java
public UserProfileResponse getUserProfile(Long currentUserId, Long profileUserId) {
    User currentUser = userRepository.findById(currentUserId);           // 쿼리 1
    User profileUser = userRepository.findByIdWithGardensAndAvatars(...); // 쿼리 2

    int profileUserWateringCount = friendWateringLogRepository
        .countByWaterGiverAndWateredAtAfter(profileUser, ...);            // 쿼리 3

    int currentUserWateringCount = friendWateringLogRepository
        .countByWaterGiverAndWateredAtAfter(currentUser, ...);            // 쿼리 4

    Set<Long> wateredGardenIds = friendWateringLogRepository
        .findWateredGardenIdsByGiverAndDate(...);                         // 쿼리 5

    boolean isFollowing = followRepository
        .existsByFollowerAndFollowing(currentUser, profileUser);          // 쿼리 6

    boolean isFollowedByProfileUser = followRepository
        .existsByFollowerAndFollowing(profileUser, currentUser);          // 쿼리 7
    // ...
}
```

### 문제 설명
- 프로필 조회 1건에 **최소 7번의 쿼리** 발생
- 동시 사용자가 많으면 DB 커넥션 풀 고갈 위험

### 해결 방안
- 여러 조회를 하나의 복합 쿼리로 통합
- 상호 팔로우 여부를 단일 쿼리로 조회
- 캐싱 적용 (특히 자주 조회되는 인기 유저)

---

## 5. 캐싱 미적용 - 차단 유저 반복 조회 🟠

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/social/feed/service/FeedService.java`
- **라인**: 57-99, 103-179

### 문제 코드
```java
// getFeed() 메서드
public List<FeedResponse> getFeed(UUID currentUserUuid, ...) {
    User currentUser = userRepository.findByUuid(currentUserUuid);
    List<Long> blockedUserIds = userBlockRepository
        .findBlockedUserIdsByBlocker(currentUser);  // DB 쿼리
    // ...
}

// getRandomFeed() 메서드 - 동일한 조회 반복
public FeedScrollResponse getRandomFeed(UUID currentUserUuid, ...) {
    User currentUser = userRepository.findByUuid(currentUserUuid);
    List<Long> blockedUserIds = userBlockRepository
        .findBlockedUserIdsByBlocker(currentUser);  // 동일 쿼리 반복
    // ...
}
```

### 문제 설명
- 피드 로드할 때마다 차단 목록을 DB에서 조회
- 차단 목록은 자주 변경되지 않는 데이터

### 해결 방안
```java
@Cacheable(value = "blockedUsers", key = "#user.id")
public List<Long> findBlockedUserIdsByBlocker(User user) {
    return userBlockRepository.findBlockedUserIdsByBlocker(user);
}

// 차단/차단해제 시 캐시 무효화
@CacheEvict(value = "blockedUsers", key = "#blockerId")
public void blockUser(Long blockerId, Long blockedId) { ... }
```

---

## 6. 중복 Stream 연산 - HomeService 🟠

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/home/service/HomeService.java`
- **라인**: 162-202, 216-270

### 문제 코드
```java
List<UserDailyMission> todayMissionEntities =
    userDailyMissionRepository.findTodayMissionsWithMasterByUser(...);

// 1차 순회
boolean isQuizCompleted = todayMissionEntities.stream()
    .filter(m -> m.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
    .anyMatch(UserDailyMission::isCompleted);

// 2차 순회 - 동일한 필터 조건
boolean isQuizResultAvailable = todayMissionEntities.stream()
    .filter(m -> m.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
    .anyMatch(m -> m.isCompleted() && m.getDailyMissionMaster().getQuiz() != null);
```

### 문제 설명
- 동일한 리스트를 동일한 필터 조건으로 여러 번 순회
- `getHomeScreenData`와 `getPannelData`에서도 동일 로직 중복

### 해결 방안
```java
// 한 번만 필터링
Optional<UserDailyMission> quizMission = todayMissionEntities.stream()
    .filter(m -> m.getDailyMissionMaster().getMissionType() == MissionType.QUIZ)
    .findFirst();

boolean isQuizCompleted = quizMission.map(UserDailyMission::isCompleted).orElse(false);
boolean isQuizResultAvailable = quizMission
    .map(m -> m.isCompleted() && m.getDailyMissionMaster().getQuiz() != null)
    .orElse(false);
```

---

## 7. 과도한 Fetch Join - UserRepository 🟠

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/member/user/domain/repository/UserRepository.java`
- **라인**: 19-25

### 문제 코드
```java
@Query("SELECT u FROM User u "
    + "LEFT JOIN FETCH u.gardens g "
    + "LEFT JOIN FETCH g.avatar a "
    + "LEFT JOIN FETCH a.avatarMaster am "
    + "WHERE u.id = :userId")
Optional<User> findByIdWithGardensAndAvatars(@Param("userId") Long userId);
```

### 문제 설명
- User → Gardens (4개) → Avatar → AvatarMaster 전체를 Fetch Join
- **카테시안 곱** 발생: 4개 정원 × 각 관계 = 중복 행 다수
- 필요 없는 데이터까지 모두 로드

### 해결 방안
- 필요한 관계만 선택적으로 로드
- `@BatchSize` 사용으로 N+1 방지하면서 필요시 로드
- 용도별로 다른 쿼리 메서드 생성

---

## 8. 메모리 낭비 - FeedService 2배 fetching 🟠

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/social/feed/service/FeedService.java`
- **라인**: 125-165

### 문제 코드
```java
// 20개 반환을 위해 40개씩 로드
int diaryFetchSize = diarySize * 2;      // size=20 → 40
int avatarPostFetchSize = avatarPostSize * 2;  // size=20 → 40

List<DiaryFeedItemResponse> diaryItems =
    fetchAndMapFeedItems(randomDiaryIds, ...);      // 40개 객체 생성
List<AvatarPostFeedItemResponse> avatarPostItems =
    fetchAndMapFeedItems(randomAvatarPostIds, ...); // 40개 객체 생성

feedItems.addAll(diaryItems);      // 80개
feedItems.addAll(avatarPostItems);

Collections.shuffle(feedItems);    // 80개 shuffle

return feedItems.stream().limit(size).toList();  // 20개만 반환
```

### 문제 설명
- 20개 반환을 위해 80개 객체 생성
- 60개 객체는 생성 후 바로 GC 대상
- 메모리 낭비 + GC 부하

### 해결 방안
- DB 레벨에서 랜덤 + 제한 적용
- 또는 ID만 먼저 가져와서 shuffle 후 필요한 것만 엔티티 로드

---

## 9. 제한 없는 전체 조회 - AdminService 🟡

### 위치
- **파일**: `src/main/java/com/example/cp_main_be/domain/admin/service/AdminService.java`
- **라인**: 146-148

### 문제 코드
```java
public List<User> getUsers() {
    return userService.findAllUsers();  // 페이징 없이 전체 조회
}
```

### 문제 설명
- 모든 사용자를 메모리에 로드
- 유저 100만명 = **OOM (Out Of Memory) 위험**
- 응답 시간 매우 길어짐

### 해결 방안
```java
public Page<User> getUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
}
```

---

## 10. 인덱스 미적용 - 자주 호출되는 쿼리 🟡

### 위치
- 여러 Repository 파일

### 인덱스가 필요한 쿼리 패턴

| 쿼리 메서드 | 테이블 | 필요한 인덱스 |
|------------|--------|--------------|
| `findBlockedUserIdsByBlocker` | user_block | `(blocker_id)` |
| `countByWaterGiverAndWateredAtAfter` | friend_watering_log | `(water_giver_id, watered_at)` |
| `existsByWaterGiverAndWateredGardenAndWateredAtAfter` | friend_watering_log | `(water_giver_id, watered_garden_id, watered_at)` |
| `findAllByReceiverIdOrderByCreatedAtDesc` | notification | `(receiver_id, created_at)` |

### 문제 설명
- 인덱스 없이 자주 호출되는 쿼리 = 테이블 풀 스캔
- 데이터가 많아질수록 성능 급격히 저하

### 해결 방안
```sql
-- user_block 테이블
CREATE INDEX idx_user_block_blocker ON user_block(blocker_id);

-- friend_watering_log 테이블
CREATE INDEX idx_watering_log_giver_date ON friend_watering_log(water_giver_id, watered_at);
CREATE INDEX idx_watering_log_giver_garden_date ON friend_watering_log(water_giver_id, watered_garden_id, watered_at);

-- notification 테이블
CREATE INDEX idx_notification_receiver_created ON notification(receiver_id, created_at DESC);
```

---

## 우선순위별 개선 로드맵

### Phase 1: 즉시 개선 (심각한 문제)
1. FollowService N+1 → Fetch Join 적용
2. NotificationService 스케줄러 → 배치 처리 적용
3. DiaryRepository RAND() → 효율적인 랜덤 알고리즘
4. UserService.getUserProfile → 쿼리 통합

### Phase 2: 단기 개선 (중간 우선순위)
5. 차단 목록 캐싱 적용
6. HomeService Stream 연산 최적화
7. Fetch Join 범위 조정
8. FeedService fetching 최적화

### Phase 3: 장기 개선 (낮은 우선순위)
9. AdminService 페이징 적용
10. 인덱스 추가 (DBA와 협의)
