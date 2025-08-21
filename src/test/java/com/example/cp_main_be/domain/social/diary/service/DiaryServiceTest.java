package com.example.cp_main_be.domain.social.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.cp_main_be.domain.content.image.ImageUploader;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.social.diary.dto.request.DiaryWriteRequest;
import com.example.cp_main_be.domain.social.diary.dto.response.DiaryResponse;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImageRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@DataJpaTest
@Import(DiaryService.class) // 테스트할 서비스 클래스를 빈으로 등록
class DiaryServiceTest {

  @Autowired private DiaryService diaryService;
  @Autowired private DiaryRepository diaryRepository;
  @Autowired private UserRepository userRepository; // User 엔티티 저장을 위해 필요
  @Autowired private DiaryImageRepository diaryImageRepository;
  @MockBean private ImageUploader imageUploader;

  private User ownerUser;
  private User anotherUser;
  private Diary testDiary;
  private DiaryImage testDiaryImage;

  @BeforeEach
  void setUp() {
    // 테스트용 사용자 2명 생성 및 저장
    ownerUser =
        userRepository.save(User.builder().username("owner").uuid(UUID.randomUUID()).build());
    anotherUser =
        userRepository.save(User.builder().username("another").uuid(UUID.randomUUID()).build());

    // ownerUser가 작성한 다이어리 생성 및 저장
    testDiary =
        diaryRepository.save(
            Diary.builder()
                .title("Test Diary Title")
                .content("Test Diary Content")
                .user(ownerUser)
                .build());

    // SecurityContextHolder에 현재 로그인한 사용자 정보 설정
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(ownerUser.getUuid().toString(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @DisplayName("특정 유저의 다이어리 목록 조회 성공")
  @Test
  void findAllDiariesByUserId_Success() {
    // given
    User user1 = userRepository.save(User.builder().username("testUser1").build());
    User user2 = userRepository.save(User.builder().username("testUser2").build());

    Diary diary1 =
        diaryRepository.save(
            Diary.builder().title("Diary 1").content("Content 1").user(user1).build());
    Diary diary2 =
        diaryRepository.save(
            Diary.builder().title("Diary 2").content("Content 2").user(user1).build());

    Diary diary3 =
        diaryRepository.save(
            Diary.builder().title("Diary 3").content("Content 3").user(user2).build());

    // when
    List<DiaryResponse> result = diaryService.findAllDiariesByUserId(user1.getId());

    // then
    assertThat(result).isNotNull();
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.get(0).getTitle()).isEqualTo(diary1.getTitle());
    assertThat(result.get(1).getTitle()).isEqualTo(diary2.getTitle());
  }

  @DisplayName("다이어리 등록 후 데이터베이스에 저장되었는지 확인")
  @Test
  void registerDiary_and_verify_saved_in_db() {
    // given
    User user = userRepository.save(User.builder().username("testUser").build());

    String title = "Test Title";
    String content = "Test Content";
    String keyword = "testKeyword";
    Boolean isPublic = true;

    // when
    Long newDiaryId = diaryService.registerDiary(title, content, keyword, user, isPublic);

    // then
    // 반환된 ID를 사용해 데이터베이스에서 다이어리 객체를 조회
    Optional<Diary> foundDiary = diaryRepository.findById(newDiaryId);

    // 조회된 객체가 존재하는지, 필드 값이 예상과 일치하는지 확인
    assertThat(foundDiary).isPresent();
    assertThat(foundDiary.get().getTitle()).isEqualTo(title);
    assertThat(foundDiary.get().getContent()).isEqualTo(content);
    assertThat(foundDiary.get().getUser().getId()).isEqualTo(user.getId());
  }

  @DisplayName("특정 다이어리 조회 성공 by id")
  @Test
  void getDiaryById_success() throws Exception {
    // given
    User user1 = userRepository.save(User.builder().username("testUser1").build());
    User user2 = userRepository.save(User.builder().username("testUser2").build());

    Diary diary1 =
        diaryRepository.save(
            Diary.builder().title("Diary 1").content("Content 1").user(user1).build());
    Diary diary2 =
        diaryRepository.save(
            Diary.builder().title("Diary 2").content("Content 2").user(user2).build());

    // when
    DiaryResponse diary1ById = diaryService.getDiaryResponseById(diary1.getId());
    DiaryResponse diary2ById = diaryService.getDiaryResponseById(diary2.getId());

    // then
    assertThat(diary1ById).isNotNull();
    assertThat(diary1ById.getId()).isEqualTo(diary1.getId());
    assertThat(diary1ById.getTitle()).isEqualTo(diary1.getTitle());
    assertThat(diary1ById.getContent()).isEqualTo(diary1.getContent());

    assertThat(diary2ById).isNotNull();
    assertThat(diary2ById.getId()).isEqualTo(diary2.getId());
    assertThat(diary2ById.getTitle()).isEqualTo(diary2.getTitle());
    assertThat(diary2ById.getContent()).isEqualTo(diary2.getContent());
  }

  @DisplayName("다이어리 수정 성공")
  @Test
  void update_diary_success() throws Exception {
    // given
    Long diaryId = testDiary.getId();
    DiaryWriteRequest diaryWriteRequest =
        DiaryWriteRequest.builder().title("update Title").content("update Content").build();

    // when
    Diary updatedDiary = diaryService.updateDiary(diaryId, diaryWriteRequest);

    // then
    assertThat(updatedDiary.getTitle()).isEqualTo(diaryWriteRequest.getTitle());
    assertThat(updatedDiary.getContent()).isEqualTo(diaryWriteRequest.getContent());
  }

  @DisplayName("다른 사람의 일기 수정 실패")
  @Test
  void update_others_diary_fail() throws Exception {
    // given
    // SecurityContextHolder에 다른 사용자 정보로 변경
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(anotherUser.getUuid().toString(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Long diaryId = testDiary.getId();
    DiaryWriteRequest diaryWriteRequest =
        DiaryWriteRequest.builder().title("update Title").content("update Content").build();

    // when & then
    assertThatThrownBy(() -> diaryService.updateDiary(diaryId, diaryWriteRequest))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 다이어리를 수정할 권한이 없습니다.");
  }

  @DisplayName("다이어리 삭제 성공: 일기 작성자 본인이 삭제하는 경우")
  @Test
  void deleteDiaryById_success() {
    // given
    Long diaryIdToDelete = testDiary.getId();

    // when
    diaryService.deleteDiaryById(diaryIdToDelete);

    // then
    Optional<Diary> deletedDiary = diaryRepository.findById(diaryIdToDelete);
    assertThat(deletedDiary).isNotPresent();
  }

  @DisplayName("다이어리 삭제 실패: 다른 사용자가 삭제하려는 경우")
  @Test
  void deleteDiaryById_fail_whenUnauthorizedUser() {
    // given
    // SecurityContextHolder에 다른 사용자 정보로 변경
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(anotherUser.getUuid().toString(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Long diaryIdToDelete = testDiary.getId();

    // when & then
    assertThatThrownBy(() -> diaryService.deleteDiaryById(diaryIdToDelete))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 다이어리를 삭제할 권한이 없습니다.");

    // 다이어리가 실제로 삭제되지 않았는지 확인
    assertThat(diaryRepository.findById(diaryIdToDelete)).isPresent();
  }

  @DisplayName("이미지 저장 성공: 일기 작성자 본인이 이미지를 추가하는 경우")
  @Test
  void saveDiaryImage_success() {
    // Given
    Long diaryId = testDiary.getId();
    MockMultipartFile mockFile =
        new MockMultipartFile(
            "file", "test-image.jpg", "image/jpeg", "test image content".getBytes());
    String uploadedImageUrl = "https://s3-bucket/diary-images/test-image.jpg";

    // imageUploader의 Mocking 동작을 설정
    given(imageUploader.upload(any(), any())).willReturn(uploadedImageUrl);

    // When
    DiaryResponse result = diaryService.saveDiaryImage(diaryId, mockFile);

    // Then
    assertThat(result.getImageUrl()).isEqualTo(uploadedImageUrl);

    Optional<Diary> updatedDiaryOptional = diaryRepository.findById(diaryId);
    assertThat(updatedDiaryOptional).isPresent();
    assertThat(updatedDiaryOptional.get().getDiaryImage().getImageUrl())
        .isEqualTo(uploadedImageUrl);

    verify(imageUploader).upload(any(), any());
  }

  @DisplayName("이미지 저장 실패: 다른 사용자가 이미지를 추가하려는 경우")
  @Test
  void saveDiaryImage_unauthorizedUser_throwsException() {
    // Given
    // SecurityContextHolder에 다른 사용자 정보로 변경
    Authentication anotherAuth =
        new UsernamePasswordAuthenticationToken(anotherUser.getUuid().toString(), null);
    SecurityContextHolder.getContext().setAuthentication(anotherAuth);

    Long diaryId = testDiary.getId();
    MockMultipartFile mockFile =
        new MockMultipartFile(
            "file", "test-image.jpg", "image/jpeg", "test image content".getBytes());
    String uploadedImageUrl = "https://s3-bucket/diary-images/test-image.jpg";

    // Mocking 동작 설정 (업로드까지는 시도)
    given(imageUploader.upload(any(), any())).willReturn(uploadedImageUrl);

    // When & Then
    assertThatThrownBy(() -> diaryService.saveDiaryImage(diaryId, mockFile))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 다이어리에 이미지를 추가할 권한이 없습니다.");

    // 다이어리가 실제로 업데이트되지 않았는지 확인
    Optional<Diary> unchangedDiary = diaryRepository.findById(diaryId);
    assertThat(unchangedDiary).isPresent();
    assertThat(unchangedDiary.get().getDiaryImage()).isNull();
  }

  @DisplayName("이미지 삭제 성공: 일기 작성자 본인이 삭제하는 경우")
  @Test
  void deleteDiaryImage_success() {
    // 다이어리에 속한 이미지 생성 및 저장
    testDiaryImage =
        diaryImageRepository.save(
            DiaryImage.builder().imageUrl("http://test.com/image.jpg").diary(testDiary).build());
    // given
    Long diaryId = testDiary.getId();
    Long imageId = testDiaryImage.getId();

    // imageUploader.delete() 호출 시 아무것도 하지 않도록 설정 (void 메서드 Mocking)
    doNothing().when(imageUploader).delete(any(String.class));

    // when
    diaryService.deleteDiaryImage(diaryId, imageId);

    // then
    // 1. 데이터베이스에서 DiaryImage 엔티티가 삭제되었는지 확인
    Optional<DiaryImage> deletedImage = diaryImageRepository.findById(imageId);
    assertThat(deletedImage).isNotPresent();

    // 2. imageUploader.delete()가 올바른 URL로 호출되었는지 확인
    verify(imageUploader).delete(testDiaryImage.getImageUrl());
  }

  @DisplayName("이미지 삭제 실패: 다른 사용자가 삭제하려는 경우")
  @Test
  void deleteDiaryImage_fail_unauthorizedUser() {
    // given
    // 다이어리에 속한 이미지 생성 및 저장
    testDiaryImage =
        diaryImageRepository.save(
            DiaryImage.builder().imageUrl("http://test.com/image.jpg").diary(testDiary).build());
    // SecurityContextHolder에 다른 사용자 정보로 변경
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(anotherUser.getUuid().toString(), null);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Long diaryId = testDiary.getId();
    Long imageId = testDiaryImage.getId();

    // when & then
    assertThatThrownBy(() -> diaryService.deleteDiaryImage(diaryId, imageId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 다이어리에 이미지를 삭제할 권한이 없습니다.");

    // 1. imageUploader.delete()가 호출되지 않았는지 확인
    verify(imageUploader, never()).delete(any(String.class));
    // 2. 데이터베이스에 엔티티가 여전히 존재하는지 확인
    assertThat(diaryImageRepository.findById(imageId)).isPresent();
  }
}
