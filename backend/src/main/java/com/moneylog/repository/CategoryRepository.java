package com.moneylog.repository;

import com.moneylog.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 카테고리 목록/단건 조회는 항상 소유자(userId) 기준으로 필터한다 (인가 원칙)
    List<Category> findAllByUserId(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);
}
