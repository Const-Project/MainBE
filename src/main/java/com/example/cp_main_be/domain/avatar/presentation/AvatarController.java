package com.example.cp_main_be.avatar.presentation;


import com.example.cp_main_be.avatar.dto.response.AvatarResponse;
import com.example.cp_main_be.avatar.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping("/register/avatars")
    public ResponseEntity<AvatarResponse> selectableAvatars() {
        AvatarResponse avatarResponse = new AvatarResponse(avatarService.getAllAvatar());
        return ResponseEntity.ok(avatarResponse);
    }
}
