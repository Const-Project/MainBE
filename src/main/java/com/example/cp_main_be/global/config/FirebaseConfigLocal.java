package com.example.cp_main_be.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

@Profile("local") // local 프로필일 때만 이 설정이 활성화됨
@Configuration
public class FirebaseConfigLocal {

  @PostConstruct
  public void initialize() {
    try {
      ClassPathResource resource =
          new ClassPathResource("napulnapul-d9572-firebase-adminsdk-fbsvc-0a2757f074.json");
      InputStream serviceAccount = resource.getInputStream();
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
      }
    } catch (Exception e) {
      throw new RuntimeException("Firebase (Local) 초기화에 실패했습니다.", e);
    }
  }
}
