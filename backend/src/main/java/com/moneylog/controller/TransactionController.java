package com.moneylog.controller;

import com.moneylog.common.ApiResponse;
import com.moneylog.common.PageResponse;
import com.moneylog.domain.type.TransactionType;
import com.moneylog.dto.request.TransactionRequest;
import com.moneylog.dto.response.TransactionResponse;
import com.moneylog.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// docs/api-spec.md 1절 "거래내역" 참고. userId는 JWT에서 인증된 로그인 사용자(SecurityContext)로부터 주입된다.
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ApiResponse<Map<String, Object>> getTransactions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        YearMonth parsed = (yearMonth != null) ? YearMonth.parse(yearMonth) : null;
        Page<TransactionResponse> result = transactionService.getTransactions(
                userId, parsed, type, categoryId, PageRequest.of(page, size));

        Map<String, Object> data = new HashMap<>();
        data.put("transactions", result.getContent());

        Map<String, Object> meta = new HashMap<>();
        meta.put("pagination", PageResponse.of(result));

        return ApiResponse.success("거래내역 목록을 조회했습니다.", data, meta);
    }

    @PostMapping
    public ApiResponse<TransactionResponse> create(@AuthenticationPrincipal Long userId,
                                                    @RequestBody @Valid TransactionRequest request) {
        return ApiResponse.success("거래내역이 등록되었습니다.", transactionService.create(userId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getOne(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ApiResponse.success("거래내역을 조회했습니다.", transactionService.getOne(userId, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                                    @RequestBody @Valid TransactionRequest request) {
        return ApiResponse.success("거래내역이 수정되었습니다.", transactionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        transactionService.delete(userId, id);
        return ApiResponse.success("거래내역이 삭제되었습니다.", null);
    }

    // 도전(F-13): 키워드/기간/금액범위 검색
    @GetMapping("/search")
    public ApiResponse<Map<String, Object>> search(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Long minAmount,
            @RequestParam(required = false) Long maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDate fromDate = (from != null) ? LocalDate.parse(from) : null;
        LocalDate toDate = (to != null) ? LocalDate.parse(to) : null;
        Page<TransactionResponse> result = transactionService.search(
                userId, keyword, fromDate, toDate, minAmount, maxAmount, PageRequest.of(page, size));

        Map<String, Object> data = new HashMap<>();
        data.put("transactions", result.getContent());
        Map<String, Object> meta = new HashMap<>();
        meta.put("pagination", PageResponse.of(result));

        return ApiResponse.success("검색 결과를 조회했습니다.", data, meta);
    }

    // 도전(F-13): CSV 내보내기
    @GetMapping("/export")
    public ResponseEntity<String> export(@AuthenticationPrincipal Long userId, @RequestParam String yearMonth) {
        String csv = transactionService.exportCsv(userId, YearMonth.parse(yearMonth));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions-" + yearMonth + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
