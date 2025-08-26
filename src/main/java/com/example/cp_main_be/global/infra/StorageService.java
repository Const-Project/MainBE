package com.example.cp_main_be.global.infra;

public interface StorageService {

  /**
   * 파일을 스토리지에 업로드하고 접근 URL을 반환합니다.
   *
   * @param fileBytes 업로드할 파일의 byte 배열
   * @param folderPath 스토리지 내에 저장될 폴더 경로 (예: "avatars/")
   * @param originalFileName 원본 파일 이름 (확장자 추출 및 고유 이름 생성에 사용)
   * @return 파일에 접근할 수 있는 전체 URL
   */
  String uploadFile(byte[] fileBytes, String folderPath, String originalFileName);
}
