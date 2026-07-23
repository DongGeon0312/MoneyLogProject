package com.moneylog.controller;

import com.moneylog.common.ApiResponse;
import com.moneylog.dto.response.MonthlyStatisticsResponse;
import com.moneylog.service.StatisticsService;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// docs/api-spec.md 1절 "통계" 참고
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/monthly")
    public ApiResponse<MonthlyStatisticsResponse> getMonthly(@AuthenticationPrincipal Long userId,
                                                              @RequestParam String yearMonth) {
        return ApiResponse.success(
                "월별 통계를 조회했습니다.",
                statisticsService.getMonthly(userId, YearMonth.parse(yearMonth)));
    }
}
