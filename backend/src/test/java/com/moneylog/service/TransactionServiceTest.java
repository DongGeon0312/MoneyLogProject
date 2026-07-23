package com.moneylog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.moneylog.domain.Category;
import com.moneylog.domain.User;
import com.moneylog.domain.type.TransactionType;
import com.moneylog.dto.request.TransactionRequest;
import com.moneylog.exception.CustomException;
import com.moneylog.exception.ErrorCode;
import com.moneylog.repository.CategoryRepository;
import com.moneylog.repository.TransactionRepository;
import com.moneylog.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void 금액이_0이하이면_등록에_실패한다() {
        // given
        User user = User.builder().email("a@moneylog.com").password("pw").nickname("nick").build();
        Category category = Category.builder().user(user).name("식비").type(TransactionType.EXPENSE).build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(category));

        TransactionRequest request = new TransactionRequest();
        ReflectionTestUtils.setField(request, "type", TransactionType.EXPENSE);
        ReflectionTestUtils.setField(request, "amount", -1000L);
        ReflectionTestUtils.setField(request, "categoryId", 1L);
        ReflectionTestUtils.setField(request, "transactionDate", LocalDate.now());

        // when & then
        assertThatThrownBy(() -> transactionService.create(1L, request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void 존재하지_않는_거래를_조회하면_예외가_발생한다() {
        // given
        when(transactionRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> transactionService.getOne(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSACTION_NOT_FOUND);
    }
}
