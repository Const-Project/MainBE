package com.example.cp_main_be.domain.social.guestbook.domain.repository;

import com.example.cp_main_be.domain.member.user.domain.User;
import com.example.cp_main_be.domain.social.guestbook.domain.Guestbook;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {
  Optional<Guestbook> findByWriterAndOwnerAndCreatedAtBetween(
      User writer, User owner, LocalDateTime startOfDay, LocalDateTime endOfDay);


  List<Guestbook> findAllByOwner(User user);
}
