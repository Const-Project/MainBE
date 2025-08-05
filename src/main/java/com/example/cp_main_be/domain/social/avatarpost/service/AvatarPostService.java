package com.example.cp_main_be.domain.social.avatarpost.service;

import com.example.cp_main_be.domain.social.avatarpost.domain.repository.AvatarPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AvatarPostService {

  private final AvatarPostRepository avatarPostRepository;
}
