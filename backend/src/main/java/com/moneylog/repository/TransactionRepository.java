package com.moneylog.repository;

import com.moneylog.domain.Transaction;
import com.moneylog.domain.type.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 거래 조회/수정/삭제는 항상 소유자(userId) 기준으로 필터한다 (인가 원칙)
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId); // 카테고리 삭제 시 RESTRICT 판단용

    // 필터(기간/타입/카테고리)는 전부 선택값이다.
    // 파생 쿼리 메서드로는 "값이 없으면 조건 무시"가 표현이 안 되므로(= null 비교가 되어버림)
    // JPQL에서 (:param IS NULL OR ...) 패턴으로 선택적 필터를 명시적으로 처리한다.
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId "
            + "AND (:from IS NULL OR t.transactionDate >= :from) "
            + "AND (:to IS NULL OR t.transactionDate <= :to) "
            + "AND (:type IS NULL OR t.type = :type) "
            + "AND (:categoryId IS NULL OR t.category.id = :categoryId) "
            + "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> search(@Param("userId") Long userId,
                              @Param("from") LocalDate from,
                              @Param("to") LocalDate to,
                              @Param("type") TransactionType type,
                              @Param("categoryId") Long categoryId,
                              Pageable pageable);

    // 월별 통계(F-05): 타입별 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
            + "WHERE t.user.id = :userId AND t.type = :type "
            + "AND t.transactionDate BETWEEN :from AND :to")
    Long sumAmount(@Param("userId") Long userId,
                   @Param("type") TransactionType type,
                   @Param("from") LocalDate from,
                   @Param("to") LocalDate to);

    // 월별 통계(F-05): 카테고리별 지출 합계 -> [categoryName, total] 배열 목록
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t "
            + "WHERE t.user.id = :userId AND t.type = :type "
            + "AND t.transactionDate BETWEEN :from AND :to "
            + "GROUP BY t.category.name")
    List<Object[]> sumGroupByCategory(@Param("userId") Long userId,
                                       @Param("type") TransactionType type,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);

    // 도전(F-13) 검색: 키워드/기간/금액범위
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId "
            + "AND (:keyword IS NULL OR t.description LIKE CONCAT('%', :keyword, '%')) "
            + "AND (:from IS NULL OR t.transactionDate >= :from) "
            + "AND (:to IS NULL OR t.transactionDate <= :to) "
            + "AND (:minAmount IS NULL OR t.amount >= :minAmount) "
            + "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) "
            + "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> searchByKeyword(@Param("userId") Long userId,
                                       @Param("keyword") String keyword,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to,
                                       @Param("minAmount") Long minAmount,
                                       @Param("maxAmount") Long maxAmount,
                                       Pageable pageable);

    // 도전(F-13) CSV 내보내기용: 기간 내 전체 내역(페이징 없이)
    List<Transaction> findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            Long userId, LocalDate from, LocalDate to);
}
