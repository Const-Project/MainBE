package com.example.cp_main_be.domain.mission.wishTree;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishTreeRepository extends JpaRepository<WishTree, Long> {
  Optional<WishTree> findByUserId(Long userId);
}
