package com.example.cp_main_be.domain.avatar.avatar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarSchemaMigration implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void run(ApplicationArguments args) {
    try {
      jdbcTemplate.execute("ALTER TABLE avatar ALTER COLUMN master_id DROP NOT NULL");
      log.info("[AVATAR_SCHEMA] master_id nullable migration applied");
    } catch (Exception e) {
      log.warn(
          "[AVATAR_SCHEMA] master_id nullable migration skipped errorType={}, message={}",
          e.getClass().getSimpleName(),
          e.getMessage());
    }
  }
}
