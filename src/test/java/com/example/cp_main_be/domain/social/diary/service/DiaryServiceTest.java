package com.example.cp_main_be.domain.social.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.mock.web.MockMultipartFile;
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
  private DiaryImage testDiaryImage;

  @BeforeEach
  void setUp() {
    // 테스트에서 사용할 공통 객체들을 미리 생성합니다.
    ownerUser = User.builder().id(1L).username("owner").uuid(UUID.randomUUID()).build();
    anotherUser = User.builder().id(2L).username("another").uuid(UUID.randomUUID()).build();

    testDiary =
        Diary.builder()
            .id(10L)
            .title("Test Diary Title")
            .content("Test Diary Content")
            .user(ownerUser)
            .build();

    testDiaryImage =
        DiaryImage.builder().id(100L).imageUrl("https://s3.com/image.jpg").diary(testDiary).build();

    // SecurityContextHolder Mocking 설정
    SecurityContextHolder.setContext(securityContext);
  }

  private void mockSecurityContext(User user) {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user.getUuid().toString());
  }

  @Nested
  @DisplayName("일기 수정 (updateDiary)")
  class UpdateDiaryTest {

    @Test
    @DisplayName("성공 - 소유자가 일기를 수정한다")
    void updateDiary_Success() {
      // given
      mockSecurityContext(ownerUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));

      DiaryWriteRequest request =
          DiaryWriteRequest.builder()
              .title("Updated Title")
              .content("Updated Content")
              .isPublic(false)
              .build();

      // when
      Diary updatedDiary = diaryService.updateDiary(testDiary.getId(), request);

      // then
      assertThat(updatedDiary.getTitle()).isEqualTo("Updated Title");
      assertThat(updatedDiary.getContent()).isEqualTo("Updated Content");
      assertThat(updatedDiary.isPublic()).isFalse();
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 수정을 시도하면 예외가 발생한다")
    void updateDiary_Fail_NotOwner() {
      // given
      mockSecurityContext(anotherUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(anotherUser.getUuid())).willReturn(Optional.of(anotherUser));

      DiaryWriteRequest request = DiaryWriteRequest.builder().build();

      // when & then
      assertThatThrownBy(() -> diaryService.updateDiary(testDiary.getId(), request))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 다이어리를 수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 다이어리를 수정하려하면 예외가 발생한다")
    void updateDiary_Fail_DiaryNotFound() {
      // given
      mockSecurityContext(ownerUser);
      given(diaryRepository.findById(anyLong())).willReturn(Optional.empty());

      DiaryWriteRequest request = DiaryWriteRequest.builder().build();

      // when & then
      assertThatThrownBy(() -> diaryService.updateDiary(999L, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("해당 다이어리가 존재하지 않습니다.");
    }
  }

  @Nested
  @DisplayName("일기 삭제 (deleteDiaryById)")
  class DeleteDiaryTest {

    @Test
    @DisplayName("성공 - 소유자가 일기를 삭제한다")
    void deleteDiary_Success() {
      // given
      mockSecurityContext(ownerUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));

      // when
      diaryService.deleteDiaryById(testDiary.getId());

      // then
      verify(diaryRepository).delete(testDiary);
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 삭제를 시도하면 예외가 발생한다")
    void deleteDiary_Fail_NotOwner() {
      // given
      mockSecurityContext(anotherUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(anotherUser.getUuid())).willReturn(Optional.of(anotherUser));

      // when & then
      assertThatThrownBy(() -> diaryService.deleteDiaryById(testDiary.getId()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("해당 다이어리를 삭제할 권한이 없습니다.");

      verify(diaryRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("이미지 저장 (saveDiaryImage)")
  class SaveImageTest {

    @Test
    @DisplayName("성공 - 소유자가 이미지를 저장한다")
    void saveImage_Success() {
      // given
      mockSecurityContext(ownerUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));
      given(imageUploader.upload(any(), anyString())).willReturn("new_image_url");

      MockMultipartFile file =
          new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes());

      // when
      DiaryResponse response = diaryService.saveDiaryImage(testDiary.getId(), file);

      // then
      ArgumentCaptor<DiaryImage> imageCaptor = ArgumentCaptor.forClass(DiaryImage.class);
      verify(diaryImageRepository).save(imageCaptor.capture());

      assertThat(imageCaptor.getValue().getImageUrl()).isEqualTo("new_image_url");
      assertThat(response.getImageUrl()).isEqualTo("new_image_url");
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
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));
      given(diaryImageRepository.findById(testDiaryImage.getId()))
          .willReturn(Optional.of(testDiaryImage));
      doNothing().when(imageUploader).delete(anyString());

      // when
      diaryService.deleteDiaryImage(testDiary.getId(), testDiaryImage.getId());

      // then
      verify(imageUploader).delete(testDiaryImage.getImageUrl());
      verify(diaryImageRepository).deleteById(testDiaryImage.getId());
    }

    @Test
    @DisplayName("실패 - 다른 사용자가 삭제를 시도하면 예외가 발생한다")
    void deleteImage_Fail_NotOwner() {
      // given
      mockSecurityContext(anotherUser);
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(anotherUser.getUuid())).willReturn(Optional.of(anotherUser));

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
      given(diaryRepository.findById(testDiary.getId())).willReturn(Optional.of(testDiary));
      given(userRepository.findByUuid(ownerUser.getUuid())).willReturn(Optional.of(ownerUser));

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
