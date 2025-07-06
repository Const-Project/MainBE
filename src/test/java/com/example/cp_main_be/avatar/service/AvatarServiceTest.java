package com.example.cp_main_be.avatar.service;

import com.example.cp_main_be.avatar.domain.Avatar;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AvatarServiceTest {

    @Autowired
    private AvatarService avatarService;

    @Test
    void 모든_아바타_불러오기() {
        //given
        Avatar avatar1 = new Avatar();
        avatar1.setImageUrl("http://example.com");

        Avatar avatar2 = new Avatar();
        avatar2.setImageUrl("http://example2.com");

        avatarService.save(avatar1);
        avatarService.save(avatar2);
        //when
        List<Avatar> avatars = avatarService.getAllAvatar();

        // then
        assertEquals(2, avatars.size(), "아바타 리스트의 사이즈는 2여야 합니다.");
        assertEquals("http://example.com", avatars.get(0).getImageUrl());

    }
}