package com.example.cp_main_be.domain.social.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.cp_main_be.domain.avatar.image.ImageUploader;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.social.diary.domain.Diary;
import com.example.cp_main_be.domain.social.diary.dto.request.CreateDiaryRequest;
import com.example.cp_main_be.domain.social.diary.dto.request.UpdateDiaryRequest;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImage;
import com.example.cp_main_be.domain.social.diaryimage.domain.DiaryImageRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

  @InjectMocks private DiaryService diaryService;

  @Mock private DiaryRepository diaryRepository;
  @Mock private UserRepository userRepository;
  @Mock private ImageUploader imageUploader;
  @Mock private DiaryImageRepository diaryImageRepository;
  @Mock private SecurityContext securityContext;
  @Mock private UsernamePasswordAuthenticationToken authentication;

  private User ownerUser;
  private User anotherUser;
  private Diary testDiary;

  @BeforeEach
  void setUp() {
    ownerUser = User.builder().id(1L).nickname("owner").uuid(UUID.randomUUID()).build();
    anotherUser = User.builder().id(2L).nickname("another").uuid(UUID.randomUUID()).build();
    testDiary =
        Diary.builder().id(10L).title("Test Title").content("Test Content").user(ownerUser).build();

    // SecurityContextHolder Mocking 설정
    SecurityContextHolder.setContext(securityContext);
  }

  private void mockSecurityContext(User user) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user.getUuid().toString());
  }

  @Nested
  @DisplayName("일기 생성 (createDiary)")
  class CreateDiaryTest {

    @Test
    @DisplayName("성공 - 이미지가 없는 일기를 생성한다")
    void createDiary_Success_WithoutImage() {
      // given
      CreateDiaryRequest request = new CreateDiaryRequest("New Title", "New Content", null, true);
      given(diaryRepository.save(any(Diary.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      diaryService.createDiary(ownerUser, request);

      // then
      ArgumentCaptor<Diary> diaryCaptor = ArgumentCaptor.forClass(Diary.class);
      verify(diaryRepository).save(diaryCaptor.capture());
      verify(diaryImageRepository, never()).save(any());

      Diary savedDiary = diaryCaptor.getValue();
      assertThat(savedDiary.getTitle()).isEqualTo("New Title");
      assertThat(savedDiary.getUser()).isEqualTo(ownerUser);
    }

    @Test
    @DisplayName("성공 - 이미지가 있는 일기를 생성한다")
    void createDiary_Success_WithImage() {
      // given
      CreateDiaryRequest request =
          new CreateDiaryRequest("New Title", "New Content", "image_url", true);
      given(diaryRepository.save(any(Diary.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      diaryService.createDiary(ownerUser, request);

      // then
      verify(diaryRepository).save(any(Diary.class));
      verify(diaryImageRepository).save(any(DiaryImage.class));
    }
  }

  @Nested
  @DisplayName("일기 수정 (updateDiary)")
  class UpdateDiaryTest {

    @Test
    @DisplayName("성공 - 소유자가 일기 내용을 수정한다")
    void updateDiary_Success_ContentOnly() {
      // given
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      UpdateDiaryRequest request =
          new UpdateDiaryRequest("Updated Title", "Updated Content", null, false);

      // when
      Diary updatedDiary = diaryService.updateDiary(ownerUser.getId(), testDiary.getId(), request);

      // then
      assertThat(updatedDiary.getTitle()).isEqualTo("Updated Title");
      assertThat(updatedDiary.getContent()).isEqualTo("Updated Content");
      assertThat(updatedDiary.isPublic()).isFalse();
    }

    @Test
    @DisplayName("성공 - 기존 이미지가 없는 일기에 이미지를 추가한다")
    void updateDiary_Success_AddNewImage() {
      // given
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      UpdateDiaryRequest request =
          new UpdateDiaryRequest("Title", "Content", "new_image_url", true);

      // when
      diaryService.updateDiary(ownerUser.getId(), testDiary.getId(), request);

      // then
      ArgumentCaptor<DiaryImage> imageCaptor = ArgumentCaptor.forClass(DiaryImage.class);
      verify(diaryImageRepository).save(imageCaptor.capture());
      assertThat(imageCaptor.getValue().getImageUrl()).isEqualTo("new_image_url");
    }

    @Test
    @DisplayName("성공 - 기존 이미지를 다른 이미지로 변경한다")
    void updateDiary_Success_UpdateExistingImage() {
      // given
      DiaryImage existingImage =
          DiaryImage.builder().id(100L).imageUrl("old_url").diary(testDiary).build();
      testDiary.updateImage(existingImage); // 기존 이미지 설정

      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      UpdateDiaryRequest request =
          new UpdateDiaryRequest("Title", "Content", "updated_image_url", true);

      // when
      diaryService.updateDiary(ownerUser.getId(), testDiary.getId(), request);

      // then
      verify(diaryImageRepository, never()).save(any()); // 새로 저장하지 않음
      verify(diaryImageRepository, never()).delete(any()); // 삭제하지 않음
      assertThat(existingImage.getImageUrl()).isEqualTo("updated_image_url");
    }

    @Test
    @DisplayName("성공 - 기존 이미지를 삭제한다")
    void updateDiary_Success_RemoveImage() {
      // given
      DiaryImage existingImage =
          DiaryImage.builder().id(100L).imageUrl("old_url").diary(testDiary).build();
      testDiary.updateImage(existingImage);

      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      UpdateDiaryRequest request = new UpdateDiaryRequest("Title", "Content", null, true);

      // when
      diaryService.updateDiary(ownerUser.getId(), testDiary.getId(), request);

      // then
      verify(diaryImageRepository).delete(existingImage);
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 수정을 시도하면 예외가 발생한다")
    void updateDiary_Fail_NotOwner() {
      // given
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      UpdateDiaryRequest request = new UpdateDiaryRequest("Title", "Content", null, true);

      // when & then
      assertThatThrownBy(
              () -> diaryService.updateDiary(anotherUser.getId(), testDiary.getId(), request))
          .isInstanceOf(SecurityException.class)
          .hasMessage("일기를 수정할 권한이 없습니다.");
    }
  }

  @Nested
  @DisplayName("일기 삭제 (deleteDiary)")
  class DeleteDiaryTest {

    @Test
    @DisplayName("성공 - 소유자가 일기를 삭제한다")
    void deleteDiary_Success() {
      // given
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));

      // when
      diaryService.deleteDiary(ownerUser.getId(), testDiary.getId());

      // then
      verify(diaryRepository).delete(testDiary);
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 삭제를 시도하면 예외가 발생한다")
    void deleteDiary_Fail_NotOwner() {
      // given
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));

      // when & then
      assertThatThrownBy(() -> diaryService.deleteDiary(anotherUser.getId(), testDiary.getId()))
          .isInstanceOf(SecurityException.class)
          .hasMessage("일기를 삭제할 권한이 없습니다.");

      verify(diaryRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("이미지 삭제 (deleteDiaryImage)")
  class DeleteImageTest {

    @Test
    @DisplayName("성공 - 소유자가 이미지를 삭제한다")
    void deleteImage_Success() {
      // given
      mockSecurityContext(ownerUser);
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));

      DiaryImage diaryImage =
          DiaryImage.builder().id(100L).imageUrl("image_url").diary(testDiary).build();
      given(diaryImageRepository.findById(diaryImage.getId())).willReturn(Optional.of(diaryImage));

      doNothing().when(imageUploader).delete(anyString());

      // when
      diaryService.deleteDiaryImage(testDiary.getId(), diaryImage.getId());

      // then
      verify(imageUploader).delete(diaryImage.getImageUrl());
      verify(diaryImageRepository).deleteById(diaryImage.getId());
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 삭제를 시도하면 예외가 발생한다")
    void deleteImage_Fail_NotOwner() {
      // given
      mockSecurityContext(anotherUser); // 다른 사용자로 로그인
      given(userRepository.findByUuid(anotherUser.getUuid())).willReturn(Optional.of(anotherUser));
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));

      // when & then
      assertThatThrownBy(() -> diaryService.deleteDiaryImage(testDiary.getId(), 100L))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 다이어리에 이미지를 삭제할 권한이 없습니다.");

      verify(imageUploader, never()).delete(anyString());
      verify(diaryImageRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("실패 - 이미지가 해당 다이어리에 속하지 않으면 예외가 발생한다")
    void deleteImage_Fail_ImageNotBelongToDiary() {
      // given
      mockSecurityContext(ownerUser);
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));

      // 다른 다이어리에 속한 이미지 생성
      Diary anotherDiary = Diary.builder().id(99L).build();
      DiaryImage anotherDiaryImage = DiaryImage.builder().id(200L).diary(anotherDiary).build();
      given(diaryImageRepository.findById(anotherDiaryImage.getId()))
          .willReturn(Optional.of(anotherDiaryImage));

      // when & then
      assertThatThrownBy(
              () -> diaryService.deleteDiaryImage(testDiary.getId(), anotherDiaryImage.getId()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("해당 이미지는 다이어리에 속하지 않습니다.");
    }
  }
}
