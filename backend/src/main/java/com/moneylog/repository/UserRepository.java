package com.moneylog.repository;

import com.moneylog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO(1-6/1-8): 조회 메서드 정의 (findByEmail 등)
public interface UserRepository extends JpaRepository<User, Long> {
}
