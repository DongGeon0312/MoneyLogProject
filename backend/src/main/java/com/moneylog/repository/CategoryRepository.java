package com.moneylog.repository;

import com.moneylog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO(1-6/1-7): 사용자별 조회 메서드 정의
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
