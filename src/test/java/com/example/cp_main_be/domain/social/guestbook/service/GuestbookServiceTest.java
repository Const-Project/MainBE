package com.example.cp_main_be.domain.social.guestbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import com.example.cp_main_be.domain.social.guestbook.domain.repository.GuestbookRepository;
import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookRequest;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuestbookServiceTest {

  @InjectMocks private GuestbookService guestbookService;

  @Mock private GuestbookRepository guestbookRepository;

  @Mock private UserRepository userRepository;

  @Mock private NotificationService notificationService;

  @Mock private UserService userService;

  @Test
  @DisplayName("방명록 작성 성공")
  void createGuestbook_success() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    GuestbookRequest request = new GuestbookRequest("안녕하세요!");

    User writer = User.builder().id(writerId).build();
    User owner = User.builder().id(ownerId).build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(
            guestbookRepository.findByWriterAndOwnerAndCreatedAtBetween(
                any(User.class),
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
        .willReturn(Optional.empty());
    given(userService.getCurrentUser()).willReturn(writer);

    // when
    guestbookService.createGuestbook(writerId, ownerId, request);

    // then
    // 1. guestbookRepository.save가 호출되었는지 검증
    ArgumentCaptor<Guestbook> guestbookCaptor = ArgumentCaptor.forClass(Guestbook.class);
    verify(guestbookRepository).save(guestbookCaptor.capture());
    Guestbook savedGuestbook = guestbookCaptor.getValue();
    assertThat(savedGuestbook.getWriter()).isEqualTo(writer);
    assertThat(savedGuestbook.getOwner()).isEqualTo(owner);
    assertThat(savedGuestbook.getContent()).isEqualTo(request.getContent());

    // 2. 경험치 추가 메서드가 호출되었는지 검증
    verify(userService).addExperience(writerId, 6);

    // 3. 알림 전송 메서드가 호출되었는지 검증
    verify(notificationService)
        .send(owner, writer, NotificationType.GUESTBOOK, "/guestbooks/" + ownerId);
  }

  @Test
  @DisplayName("자신에게 방명록 작성 시 알림 미전송")
  void createGuestbook_forSelf_doesNotSendNotification() {
    // given
    Long userId = 1L;
    GuestbookRequest request = new GuestbookRequest("오늘도 화이팅!");

    User user = User.builder().id(userId).build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(guestbookRepository.findByWriterAndOwnerAndCreatedAtBetween(any(), any(), any(), any()))
        .willReturn(Optional.empty());
    given(userService.getCurrentUser()).willReturn(user);

    // when
    guestbookService.createGuestbook(userId, userId, request);

    // then
    verify(guestbookRepository).save(any(Guestbook.class));
    verify(userService).addExperience(userId, 6);
    // 알림 서비스는 호출되지 않아야 함
    verify(notificationService, never()).send(any(), any(), any(), any());
  }

  @Test
  @DisplayName("방명록 작성 실패 - 1일 1회 제한")
  void createGuestbook_fails_dueToDailyLimit() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    GuestbookRequest request = new GuestbookRequest("또 왔어요!");

    User writer = User.builder().id(writerId).build();
    User owner = User.builder().id(ownerId).build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    // 이미 오늘 작성한 기록이 있다고 가정
    given(guestbookRepository.findByWriterAndOwnerAndCreatedAtBetween(any(), any(), any(), any()))
        .willReturn(Optional.of(Guestbook.builder().build()));

    // when & then
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> {
              guestbookService.createGuestbook(writerId, ownerId, request);
            });

    assertThat(exception.getMessage()).isEqualTo("해당 사용자에게는 하루에 한 번만 방명록을 작성할 수 있습니다.");
    verify(guestbookRepository, never()).save(any()); // save는 호출되면 안 됨
  }

  @Test
  @DisplayName("방명록 작성 실패 - 작성자를 찾을 수 없음")
  void createGuestbook_fails_whenWriterNotFound() {
    // given
    Long writerId = 99L; // 존재하지 않는 ID
    Long ownerId = 1L;
    GuestbookRequest request = new GuestbookRequest("안녕하세요");

    given(userRepository.findById(writerId)).willReturn(Optional.empty());

    // when & then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          guestbookService.createGuestbook(writerId, ownerId, request);
        });
  }

  @Test
  @DisplayName("방명록 작성 실패 - 소유자를 찾을 수 없음")
  void createGuestbook_fails_whenOwnerNotFound() {
    // given
    Long writerId = 1L;
    Long ownerId = 99L; // 존재하지 않는 ID
    GuestbookRequest request = new GuestbookRequest("안녕하세요");

    User writer = User.builder().id(writerId).build();
    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.empty());

    // when & then
    assertThrows(
        UserNotFoundException.class,
        () -> {
          guestbookService.createGuestbook(writerId, ownerId, request);
        });
  }
}
