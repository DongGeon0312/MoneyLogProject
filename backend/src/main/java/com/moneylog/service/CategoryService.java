package com.moneylog.service;

import com.moneylog.domain.Category;
import com.moneylog.domain.User;
import com.moneylog.domain.type.TransactionType;
import com.moneylog.dto.request.CategoryRequest;
import com.moneylog.dto.response.CategoryResponse;
import com.moneylog.exception.CustomException;
import com.moneylog.exception.ErrorCode;
import com.moneylog.repository.CategoryRepository;
import com.moneylog.repository.TransactionRepository;
import com.moneylog.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> getCategories(Long userId) {
        return categoryRepository.findAllByUserId(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        User user = getUserOrThrow(userId);
        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getOwnedCategoryOrThrow(userId, categoryId);
        category.update(request.getName(), request.getType());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long categoryId) {
        Category category = getOwnedCategoryOrThrow(userId, categoryId);
        // 삭제 정책(docs/erd.md 6절): 거래가 참조 중인 카테고리는 삭제를 막는다 (RESTRICT)
        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new CustomException(ErrorCode.CATEGORY_IN_USE);
        }
        categoryRepository.delete(category);
    }

    // 회원가입 시 기본 카테고리 자동 시드 (docs/erd.md 2절). 3일차 AuthService.signup에서 호출 예정.
    @Transactional
    public void seedDefaultCategories(User user) {
        List<Category> defaults = List.of(
                Category.builder().user(user).name("식비").type(TransactionType.EXPENSE).build(),
                Category.builder().user(user).name("교통").type(TransactionType.EXPENSE).build(),
                Category.builder().user(user).name("주거").type(TransactionType.EXPENSE).build(),
                Category.builder().user(user).name("문화").type(TransactionType.EXPENSE).build(),
                Category.builder().user(user).name("급여").type(TransactionType.INCOME).build(),
                Category.builder().user(user).name("용돈").type(TransactionType.INCOME).build()
        );
        categoryRepository.saveAll(defaults);
    }

    private Category getOwnedCategoryOrThrow(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }
}
