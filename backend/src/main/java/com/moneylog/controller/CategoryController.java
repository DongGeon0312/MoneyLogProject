package com.moneylog.controller;

import com.moneylog.common.ApiResponse;
import com.moneylog.dto.request.CategoryRequest;
import com.moneylog.dto.response.CategoryResponse;
import com.moneylog.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// docs/api-spec.md 1절 "카테고리" 참고. userId는 JWT에서 인증된 로그인 사용자(SecurityContext)로부터 주입된다.
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories(@AuthenticationPrincipal Long userId) {
        return ApiResponse.success("카테고리 목록을 조회했습니다.", categoryService.getCategories(userId));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@AuthenticationPrincipal Long userId,
                                                 @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success("카테고리가 생성되었습니다.", categoryService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@AuthenticationPrincipal Long userId, @PathVariable Long id,
                                                 @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.success("카테고리가 수정되었습니다.", categoryService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        categoryService.delete(userId, id);
        return ApiResponse.success("카테고리가 삭제되었습니다.", null);
    }
}
