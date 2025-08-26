package com.example.cp_main_be.domain.mission.diary.service;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.diary.dto.request.CreateDiaryRequest;
import com.example.cp_main_be.domain.mission.diary.dto.request.UpdateDiaryRequest;
import com.example.cp_main_be.domain.mission.diary.dto.response.DiaryInfoResponse;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.mission.diaryimage.domain.DiaryImageRepository;
import com.example.cp_main_be.domain.social.like.domain.repository.LikeRepository;
import com.example.cp_main_be.global.common.CustomApiException;
import com.example.cp_main_be.global.common.ErrorCode;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

  private final DiaryRepository diaryRepository;
  private final DiaryImageRepository diaryImageRepository;
  private final LikeRepository likeRepository;

  public Long createDiary(User user, CreateDiaryRequest request) {
    // 1. 먼저 Diary 객체를 생성하고 저장합니다
    Diary diary =
        Diary.builder()
            .user(user)
            .title(request.getTitle())
            .content(request.getContent())
            .isPublic(request.getIsPublic())
            .build();
    Diary savedDiary = diaryRepository.save(diary);

    // Step 2: 요청에 imageId가 포함된 경우, 미리 업로드된 이미지와 연결
    if (request.getImageId() != null) {
      DiaryImage diaryImage =
          diaryImageRepository
              .findById(request.getImageId())
              .orElseThrow(() -> new CustomApiException(ErrorCode.IMAGE_NOT_FOUND));

      // 보안: 이미지를 업로드한 사용자와 일기 작성자가 동일한지 확인
      if (!diaryImage.getUser().getId().equals(user.getId())) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED);
      }

      // 연관관계 편의 메서드를 사용하여 양방향 관계를 모두 설정합니다.
      // 이렇게 하면 savedDiary 객체에서도 diaryImage를 즉시 참조할 수 있습니다.
      savedDiary.updateImage(diaryImage);
    }

    return savedDiary.getId();
  }

  // 일기 조회 (읽기 전용)
  @Transactional(readOnly = true)
  public Diary findDiaryById(Long diaryId) {
    return diaryRepository
        .findById(diaryId)
        .orElseThrow(() -> new CustomApiException(ErrorCode.DIARY_NOT_FOUND));
  }

  // 일기 상세 조회 (읽기 전용)
  @Transactional(readOnly = true)
  public DiaryInfoResponse getDiaryInfo(Long diaryId, User currentUser) {
    // 1. N+1 문제를 해결하기 위해 연관된 엔티티(작성자, 댓글 등)를 함께 조회합니다.
    Diary diary =
        diaryRepository
            .findByIdWithDetails(diaryId)
            .orElseThrow(() -> new CustomApiException(ErrorCode.DIARY_NOT_FOUND));

    // 비공개 글 접근 제어: 비로그인 또는 작성자 외 사용자는 차단
    if (!diary.isPublic()) {
      if (currentUser == null || !diary.getUser().getId().equals(currentUser.getId())) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED, "비공개 일기를 볼 권한이 없습니다.");
      }
    }

    boolean isLiked = false;
    if (currentUser != null) {
      isLiked = likeRepository.existsByUserAndTargetIdAndTargetType(currentUser, diaryId, "DIARY");
      // 가능하다면 userId 기반 시그니처(existsByUser_Id...) 사용을 권장합니다.
    }

    // 3. 조회된 엔티티와 '좋아요' 여부를 DTO의 팩토리 메서드로 변환하여 반환합니다.
    return DiaryInfoResponse.from(diary, isLiked);
  }

  // 내 일기 목록 조회 (읽기 전용)
  @Transactional(readOnly = true)
  public List<Diary> findMyDiaries(User user) {
    return diaryRepository.findByUserOrderByCreatedAtDesc(user);
  }

  public Diary updateDiary(Long userId, Long diaryId, UpdateDiaryRequest request) {
    Diary diary = findDiaryById(diaryId);

    // 소유권 확인
    if (!Objects.equals(diary.getUser().getId(), userId)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "일기를 수정할 권한이 없습니다.");
    }

    // 일기 내용 수정
    diary.updateDiary(request.getTitle(), request.getContent(), request.getIsPublic());

    // --- 이미지 처리 로직 개선 ---
    DiaryImage oldImage = diary.getDiaryImage();
    Long newImageId = request.getImageId();

    // Case 1: 이미지가 변경되지 않은 경우 (둘 다 없거나, ID가 같음)
    if (Objects.equals(oldImage != null ? oldImage.getId() : null, newImageId)) {
      return diary;
    }

    // Case 2: 기존 이미지를 제거해야 하는 경우 (교체 또는 삭제)
    if (oldImage != null) {
      diaryImageRepository.delete(oldImage);
      diary.setDiaryImage(null);
    }

    // Case 3: 새로운 이미지를 연결해야 하는 경우 (추가 또는 교체)
    if (newImageId != null) {
      DiaryImage newImage =
          diaryImageRepository
              .findById(newImageId)
              .orElseThrow(() -> new CustomApiException(ErrorCode.IMAGE_NOT_FOUND));

      // 새 이미지의 소유권 확인
      if (!newImage.getUser().getId().equals(userId)) {
        throw new CustomApiException(ErrorCode.ACCESS_DENIED, "다른 사용자의 이미지를 사용할 수 없습니다.");
      }
      diary.updateImage(newImage); // 연관관계 편의 메서드로 새 이미지 연결
    }
    return diary;
  }

  // 일기 삭제
  public void deleteDiary(Long userId, Long diaryId) {
    Diary diary = findDiaryById(diaryId);

    if (!Objects.equals(diary.getUser().getId(), userId)) {
      throw new CustomApiException(ErrorCode.ACCESS_DENIED, "일기를 삭제할 권한이 없습니다.");
    }

    diaryRepository.delete(diary);
  }
}
