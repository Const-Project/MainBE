package com.example.cp_main_be.domain.social.guestbook.domain.repository;

import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import com.example.cp_main_be.domain.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {
  Optional<Guestbook> findByWriterAndOwnerAndCreatedAtBetween(
      User writer, User owner, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
