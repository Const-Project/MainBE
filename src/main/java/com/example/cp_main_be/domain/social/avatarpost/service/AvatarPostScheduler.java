package com.example.cp_main_be.domain.social.avatarpost.service;

import com.example.cp_main_be.domain.avatar.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.avatar.domain.repository.AvatarRepository;
import com.example.cp_main_be.domain.social.avatarpost.domain.AvatarPost;
import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvatarPostScheduler {

  private final AvatarRepository avatarRepository;
  private final AvatarPostRepository avatarPostRepository;

  // 매 시간 정각에 실행 (cron 표현식 예시)
  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void generateRandomAvatarPost() {
    // 1. 랜덤하게 사용자 소유의 아바타를 하나 선택
    // (실제로는 더 정교한 로직 필요: 예-활동적인 유저, 최근 성장한 아바타 등)
    Avatar randomAvatar = avatarRepository.findRandomAvatar();

    if (randomAvatar != null) {
      // 2. 시스템이 보여줄 문구 생성
      String caption =
          String.format(
              "%s님의 %s 아바타가 피드에 등장했어요! 구경해보세요!",
              randomAvatar.getUser().getUsername(), randomAvatar.getNickname());

      // 3. 새로운 AvatarPost 생성 및 저장
      AvatarPost newPost = AvatarPost.builder().avatar(randomAvatar).caption(caption).build();
      avatarPostRepository.save(newPost);
    }
  }
}
