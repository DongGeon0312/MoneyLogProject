package com.moneylog.dto.response;

import java.util.List;
import lombok.Getter;

// docs/api-spec.md: GET /api/statistics/monthly 응답 {income, expense, balance, byCategory}
@Getter
public class MonthlyStatisticsResponse {

    private final Long income;
    private final Long expense;
    private final Long balance;
    private final List<CategoryStat> byCategory;

    public MonthlyStatisticsResponse(Long income, Long expense, List<CategoryStat> byCategory) {
        this.income = income;
        this.expense = expense;
        this.balance = income - expense;
        this.byCategory = byCategory;
    }

    @Getter
    public static class CategoryStat {
        private final String categoryName;
        private final Long total;

        public CategoryStat(String categoryName, Long total) {
            this.categoryName = categoryName;
            this.total = total;
        }
    }
}
