package com.example.cp_main_be.domain.social.guestbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.member.user.domain.repository.UserRepository;
import com.example.cp_main_be.domain.member.user.service.UserService;
import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import com.example.cp_main_be.domain.social.guestbook.domain.repository.GuestbookRepository;
import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookRequest;
import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookResponse;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
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

  @Test
  @DisplayName("방명록 목록 조회 성공 - 방명록이 존재할 경우")
  void getGuestbookList_WhenGuestbooksExist_ShouldReturnResponseList() {
    // given (주어진 상황)
    // User 엔티티의 빌더를 사용하여 테스트용 User 객체 생성
    User owner =
        User.builder()
            .id(1L)
            .uuid(UUID.randomUUID())
            .username("방명록주인")
            .email("owner@test.com")
            .build();

    User writer =
        User.builder()
            .id(2L)
            .uuid(UUID.randomUUID())
            .username("글쓴이")
            .email("writer@test.com")
            .build();

    // 테스트용 Guestbook 객체 생성
    Guestbook guestbook1 =
        Guestbook.builder()
            .id(101L)
            .owner(owner)
            .writer(writer)
            .content("안녕하세요. 첫 번째 방명록입니다.")
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();

    Guestbook guestbook2 =
        Guestbook.builder()
            .id(102L)
            .owner(owner)
            .writer(writer)
            .content("두 번째 방명록입니다. 잘 보고 가요.")
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();

    List<Guestbook> mockGuestbookList = List.of(guestbook1, guestbook2);

    // Repository Mocking: findAllByOwner가 호출되면 준비된 리스트를 반환하도록 설정
    when(guestbookRepository.findAllByOwner(owner)).thenReturn(mockGuestbookList);

    // when (메서드 실행)
    List<GuestbookResponse> result = guestbookService.getGuestbookList(owner);

    // then (결과 검증)
    assertThat(result).isNotNull(); // 결과는 null이 아니어야 함
    assertThat(result).hasSize(2); // 결과 리스트의 크기는 2여야 함

    // DTO의 내용이 엔티티의 정보와 일치하는지 검증
    GuestbookResponse firstResponse = result.get(0);
    assertThat(firstResponse.getAuthor()).isEqualTo(writer.getUsername()); // "글쓴이"
    assertThat(firstResponse.getContent()).isEqualTo(guestbook1.getContent());
    assertThat(firstResponse.getCreatedAt()).isEqualTo(guestbook1.getCreatedAt());

    // repository의 특정 메소드가 정확히 1번 호출되었는지 검증
    verify(guestbookRepository).findAllByOwner(owner);
  }

  @Test
  @DisplayName("방명록 목록 조회 - 방명록이 존재하지 않을 경우 빈 리스트 반환")
  void getGuestbookList_WhenNoGuestbooksExist_ShouldReturnNull() {
    // given (주어진 상황)
    User owner = User.builder().id(1L).username("방명록주인").build();

    // Repository Mocking: findAllByOwner가 호출되면 빈 리스트를 반환하도록 설정
    when(guestbookRepository.findAllByOwner(owner)).thenReturn(Collections.emptyList());

    // when (메서드 실행)
    List<GuestbookResponse> result = guestbookService.getGuestbookList(owner);

    // then (결과 검증)
    assertThat(result).isEqualTo(new ArrayList<>()); //

    verify(guestbookRepository).findAllByOwner(owner);
  }
}
