package com.example.cp_main_be.domain.social.guestbook.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import com.example.cp_main_be.domain.social.guestbook.domain.repository.GuestbookRepository;
import com.example.cp_main_be.domain.social.guestbook.dto.request.GuestbookRequest;
import com.example.cp_main_be.domain.user.domain.User;
import com.example.cp_main_be.domain.user.domain.repository.UserRepository;
import com.example.cp_main_be.global.exception.UserNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GuestbookServiceTest {

  @Mock private GuestbookRepository guestbookRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private GuestbookService guestbookService;

  @DisplayName("방명록 작성 성공")
  @Test
  void createGuestbook_success() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    String content = "테스트 방명록";
    GuestbookRequest request = new GuestbookRequest();
    request.setContent(content);

    User writer = User.builder().id(writerId).username("writer").build();
    User owner = User.builder().id(ownerId).username("owner").build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(
            guestbookRepository.findByWriterAndOwnerAndCreatedAtBetween(
                any(User.class),
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
        .willReturn(Optional.empty());

    // when
    guestbookService.createGuestbook(writerId, ownerId, request);

    // then
    verify(guestbookRepository).save(any(Guestbook.class));
  }

  @DisplayName("방명록 작성 실패 - 하루에 한 번만 작성 가능")
  @Test
  void createGuestbook_fail_dailyLimit() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    String content = "테스트 방명록";
    GuestbookRequest request = new GuestbookRequest();
    request.setContent(content);

    User writer = User.builder().id(writerId).username("writer").build();
    User owner = User.builder().id(ownerId).username("owner").build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(
            guestbookRepository.findByWriterAndOwnerAndCreatedAtBetween(
                any(User.class),
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
        .willReturn(Optional.of(Guestbook.builder().build())); // 이미 작성된 방명록이 있다고 가정

    // when & then
    Assertions.assertThrows(
        RuntimeException.class, () -> guestbookService.createGuestbook(writerId, ownerId, request));
    verify(guestbookRepository, org.mockito.Mockito.never()).save(any(Guestbook.class));
  }

  @DisplayName("방명록 작성 실패 - 작성자 없음")
  @Test
  void createGuestbook_fail_writerNotFound() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    GuestbookRequest request = new GuestbookRequest();
    request.setContent("테스트");

    given(userRepository.findById(writerId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> guestbookService.createGuestbook(writerId, ownerId, request));
    verify(guestbookRepository, org.mockito.Mockito.never()).save(any(Guestbook.class));
  }

  @DisplayName("방명록 작성 실패 - 소유자 없음")
  @Test
  void createGuestbook_fail_ownerNotFound() {
    // given
    Long writerId = 1L;
    Long ownerId = 2L;
    GuestbookRequest request = new GuestbookRequest();
    request.setContent("테스트");

    User writer = User.builder().id(writerId).username("writer").build();

    given(userRepository.findById(writerId)).willReturn(Optional.of(writer));
    given(userRepository.findById(ownerId)).willReturn(Optional.empty());

    // when & then
    Assertions.assertThrows(
        UserNotFoundException.class,
        () -> guestbookService.createGuestbook(writerId, ownerId, request));
    verify(guestbookRepository, org.mockito.Mockito.never()).save(any(Guestbook.class));
  }
}
