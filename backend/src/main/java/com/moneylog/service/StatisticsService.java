package com.moneylog.service;

import com.moneylog.domain.type.TransactionType;
import com.moneylog.dto.response.MonthlyStatisticsResponse;
import com.moneylog.dto.response.MonthlyStatisticsResponse.CategoryStat;
import com.moneylog.repository.TransactionRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final TransactionRepository transactionRepository;

    public MonthlyStatisticsResponse getMonthly(Long userId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        Long income = transactionRepository.sumAmount(userId, TransactionType.INCOME, from, to);
        Long expense = transactionRepository.sumAmount(userId, TransactionType.EXPENSE, from, to);

        List<CategoryStat> byCategory = transactionRepository
                .sumGroupByCategory(userId, TransactionType.EXPENSE, from, to)
                .stream()
                .map(row -> new CategoryStat((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        return new MonthlyStatisticsResponse(income, expense, byCategory);
    }
}
