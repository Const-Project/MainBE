package com.example.cp_main_be.domain.social.guestbook.service;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GuestbookService {

  private final GuestbookRepository guestbookRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final UserService userService;

  public void createGuestbook(Long writerId, Long ownerId, GuestbookRequest request) {
    User writer =
        userRepository
            .findById(writerId)
            .orElseThrow(() -> new UserNotFoundException("작성자 사용자를 찾을 수 없습니다."));
    User owner =
        userRepository
            .findById(ownerId)
            .orElseThrow(() -> new UserNotFoundException("방명록 소유자 사용자를 찾을 수 없습니다."));

    // 1일 1회 제한 로직
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

    if (guestbookRepository
        .findByWriterAndOwnerAndCreatedAtBetween(writer, owner, startOfDay, endOfDay)
        .isPresent()) {
      throw new RuntimeException("해당 사용자에게는 하루에 한 번만 방명록을 작성할 수 있습니다."); // TODO: Custom Exception
    }

    Guestbook guestbook =
        Guestbook.builder().writer(writer).owner(owner).content(request.getContent()).build();
    guestbookRepository.save(guestbook);

    final int GUESTBOOK_POINT = 6;
    userService.addExperience(userService.getCurrentUser().getId(), GUESTBOOK_POINT);

    // 자기 자신에게는 알림을 보내지 않음
    if (!writerId.equals(ownerId)) {
      notificationService.send(owner, writer, NotificationType.GUESTBOOK, "/guestbooks/" + ownerId);
    }
  }

  public List<GuestbookResponse> getGuestbookList(User user) {

    List<Guestbook> guestbookList = guestbookRepository.findAllByOwner(user);

    // 비어있는 리스트에 stream()을 호출해도 예외가 발생하지 않고 비어있는 stream이 반환됩니다.
    return guestbookList.stream()
        .map(
            guestbook ->
                GuestbookResponse.builder()
                    .author(guestbook.getWriter().getUsername())
                    .createdAt(guestbook.getCreatedAt())
                    .content(guestbook.getContent())
                    .build())
        .toList();
  }
}
