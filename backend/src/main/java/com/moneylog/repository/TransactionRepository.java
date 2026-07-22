package com.moneylog.repository;

import com.moneylog.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO(1-6/1-7): 필터/페이징/집계 쿼리 정의 (yearMonth, type, categoryId 등)
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
