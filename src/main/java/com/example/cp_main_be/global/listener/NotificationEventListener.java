package com.example.cp_main_be.global.listener;

import com.example.cp_main_be.domain.garden.garden.service.GardenService;
import com.example.cp_main_be.domain.member.notification.domain.NotificationType;
import com.example.cp_main_be.domain.member.notification.service.NotificationService;
import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.mission.diary.domain.Diary;
import com.example.cp_main_be.domain.mission.diary.domain.repository.DiaryRepository;
import com.example.cp_main_be.domain.mission.wishTree.WishTreeService;
import com.example.cp_main_be.domain.reports.domain.Reports;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import com.example.cp_main_be.domain.social.comment.domain.Comment;
import com.example.cp_main_be.domain.social.follow.domain.Follow;
import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import com.example.cp_main_be.global.event.*;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
  private final NotificationService notificationService;
  private final DiaryRepository diaryRepository;
  private final AvatarPostRepository avatarPostRepository;
  private final WishTreeService wishTreeService;
  private final GardenService gardenService;

  @EventListener
  @Transactional
  public void handleCommentCreatedEvent(CommentCreatedEvent event) {
    Comment comment = event.getComment();
    User writer = comment.getWriter();

    User receiver = null;
    String redirectUrl = null;
    NotificationType notificationType = null;

    // 1. 댓글이 '일기(Diary)'에 달렸는지 확인
    if (comment.getDiary() != null) {
      Diary diary = comment.getDiary();
      receiver = diary.getUser(); // 알림 받을 사람 = 일기 작성자
      redirectUrl = "/diaries/" + diary.getId();
      notificationType = NotificationType.FEED_COMMENT; // 알림 타입 설정
      log.info("Diary comment notification process started for diary ID: {}", diary.getId());
    }
    // 2. 댓글이 '아바타 포스트(AvatarPost)'에 달렸는지 확인
    else if (comment.getAvatarPost() != null) {
      AvatarPost avatarPost = comment.getAvatarPost();
      receiver = avatarPost.getUser(); // 알림 받을 사람 = 포스트 작성자
      redirectUrl = "/avatar-posts/" + avatarPost.getId();
      notificationType = NotificationType.AVATAR_POST_COMMENT; // 알림 타입 설정
      log.info(
          "AvatarPost comment notification process started for post ID: {}", avatarPost.getId());
    }

    // 3. 알림 수신자가 있고, 자기 자신의 게시물에 댓글을 단 게 아닐 경우에만 알림 전송
    if (receiver != null && !receiver.getId().equals(writer.getId())) {
      notificationService.send(receiver, writer, notificationType, redirectUrl, null);
      log.info(
          "Notification sent to user ID: {} from user ID: {}", receiver.getId(), writer.getId());
    } else if (receiver != null) {
      log.info("Notification skipped: User commented on their own post.");
    } else {
      log.warn(
          "Notification skipped: Could not determine the receiver for comment ID: {}",
          comment.getId());
    }
  }

  @EventListener
  @Transactional
  public void handleLikeCreatedEvent(LikeCreatedEvent event) {
    User sender = event.getWriter();
    User receiver = event.getOwner();
    String targetType = event.getTargetType();
    Long targetId = event.getTargetId();

    String url;
    if (Objects.equals(targetType, "DIARY")) {
      url = "/diaries/" + targetId;
    } else {
      url = "/avatar-posts/" + targetId;
    }

    if (!sender.getId().equals(receiver.getId())) {
      notificationService.send(receiver, sender, NotificationType.FEED_LIKE, url, null);
    }
  }

  @EventListener
  @Transactional
  public void handleGuestbookCreatedEvent(GuestbookCreatedEvent event) {
    Guestbook guestBook = event.getGuestbook();
    User sender = guestBook.getWriter();
    User receiver = guestBook.getOwner(); // Assuming Guestbook is on a Garden
    String url = "/users/" + receiver.getId();

    if (!sender.getId().equals(receiver.getId())) {
      notificationService.send(receiver, sender, NotificationType.GUESTBOOK, url, null);
    }
  }

  @EventListener
  @Transactional
  public void handleFollowedEvent(FollowedEvent event) {
    Follow follow = event.getFollow();
    User sender = follow.getFollower();
    User receiver = follow.getFollowing();
    String url = "/users/" + sender.getId();

    notificationService.send(receiver, sender, NotificationType.FOLLOW, url, null);
  }

  @EventListener
  @Transactional
  public void handleWateredByFriendEvent(WateredByFriendEvent event) {
    User sender = event.getSender();
    User receiver = event.getReceiver();
    String url = "/garden";

    wishTreeService.addPointsToWishTree(sender.getId(), 2L);

    notificationService.send(receiver, sender, NotificationType.WATERING_BY_FRIEND, url, null);
  }

  @EventListener
  @Transactional
  public void handleReportProcessedEvent(ReportProcessedEvent event) {
    Reports report = event.getReport();
    User receiver = report.getUser();
    if (receiver == null) {
      log.warn(
          "Report processed notification skipped: missing reporter. reportId={}", report.getId());
      return;
    }
    String url = "/reports/" + report.getId();
    notificationService.send(receiver, null, NotificationType.REPORT_PROCESSED, url, null);
  }
}
