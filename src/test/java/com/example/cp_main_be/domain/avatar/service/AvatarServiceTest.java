package com.example.cp_main_be.domain.avatar.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.cp_main_be.domain.avatar.domain.Avatar;
import com.example.cp_main_be.domain.avatar.domain.repository.AvatarRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

  @Mock private AvatarRepository avatarRepository;

  @InjectMocks private AvatarService avatarService;

  @Test
  @DisplayName("모든 아바타 불러오기")
  void getAllAvatar() {
    // given
    Avatar avatar1 = Avatar.builder().id(1L).imageUrl("http://example.com/avatar1.png").build();
    Avatar avatar2 = Avatar.builder().id(2L).imageUrl("http://example.com/avatar2.png").build();
    List<Avatar> expectedAvatars = Arrays.asList(avatar1, avatar2);

    given(avatarRepository.findAll()).willReturn(expectedAvatars);

    // when
    List<Avatar> actualAvatars = avatarService.getAllAvatar();

    // then
    assertEquals(expectedAvatars.size(), actualAvatars.size());
    assertEquals(expectedAvatars.get(0).getImageUrl(), actualAvatars.get(0).getImageUrl());
    assertEquals(expectedAvatars.get(1).getImageUrl(), actualAvatars.get(1).getImageUrl());
    verify(avatarRepository).findAll();
  }

  @Test
  @DisplayName("아바타 저장")
  void saveAvatar() {
    // given
    Avatar avatar = Avatar.builder().imageUrl("http://example.com/new_avatar.png").build();

    // when
    avatarService.save(avatar);

    // then
    verify(avatarRepository).save(avatar);
  }
}
