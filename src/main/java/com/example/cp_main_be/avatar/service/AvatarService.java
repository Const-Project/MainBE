package com.example.cp_main_be.avatar.service;

import com.example.cp_main_be.avatar.domain.Avatar;
import com.example.cp_main_be.avatar.domain.repository.AvatarRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvatarService {

  private final AvatarRepository avatarRepository;

  public void save(Avatar avatar) {
    avatarRepository.save(avatar);
  }

  public List<Avatar> getAllAvatar() {
    return avatarRepository.findAll();
  }
}
