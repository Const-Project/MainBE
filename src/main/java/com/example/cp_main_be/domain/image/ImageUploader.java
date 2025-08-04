package com.example.cp_main_be.domain.image;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {

  String upload(MultipartFile file, String path);
}
