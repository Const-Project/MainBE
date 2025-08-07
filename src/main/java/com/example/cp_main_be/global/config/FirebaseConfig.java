package com.example.cp_main_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct; // 혹은 javax.annotation.PostConstruct
import java.io.InputStream;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void initialize() {
    try {
      // 1. resources 폴더의 키 파일을 InputStream으로 읽어옵니다.
      InputStream serviceAccount =
          getClass()
              .getClassLoader()
              .getResourceAsStream("napulnapul-d9572-firebase-adminsdk-fbsvc-0a2757f074.json");

      // 2. FirebaseOptions를 빌드합니다.
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      // 3. FirebaseApp이 이미 초기화되었는지 확인 후 초기화합니다.
      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
