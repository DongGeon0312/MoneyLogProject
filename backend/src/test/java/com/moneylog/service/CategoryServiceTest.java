package com.moneylog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.moneylog.domain.Category;
import com.moneylog.domain.User;
import com.moneylog.domain.type.TransactionType;
import com.moneylog.exception.CustomException;
import com.moneylog.exception.ErrorCode;
import com.moneylog.repository.CategoryRepository;
import com.moneylog.repository.TransactionRepository;
import com.moneylog.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void 거래가_참조중인_카테고리는_삭제할_수_없다() {
        // given
        User user = User.builder().email("a@moneylog.com").password("pw").nickname("nick").build();
        Category category = Category.builder().user(user).name("식비").type(TransactionType.EXPENSE).build();

        when(categoryRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(category));
        when(transactionRepository.existsByCategoryId(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.delete(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_IN_USE);
    }

    @Test
    void 존재하지_않는_카테고리를_수정하면_예외가_발생한다() {
        // given
        when(categoryRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.update(1L, 999L, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }
}
