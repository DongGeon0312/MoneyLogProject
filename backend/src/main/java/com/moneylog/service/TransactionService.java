package com.moneylog.service;

import com.moneylog.domain.Category;
import com.moneylog.domain.Transaction;
import com.moneylog.domain.User;
import com.moneylog.domain.type.TransactionType;
import com.moneylog.dto.request.TransactionRequest;
import com.moneylog.dto.response.TransactionResponse;
import com.moneylog.exception.CustomException;
import com.moneylog.exception.ErrorCode;
import com.moneylog.repository.CategoryRepository;
import com.moneylog.repository.TransactionRepository;
import com.moneylog.repository.UserRepository;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Page<TransactionResponse> getTransactions(Long userId, YearMonth yearMonth, TransactionType type,
                                                       Long categoryId, Pageable pageable) {
        LocalDate from = (yearMonth != null) ? yearMonth.atDay(1) : null;
        LocalDate to = (yearMonth != null) ? yearMonth.atEndOfMonth() : null;
        return transactionRepository.search(userId, from, to, type, categoryId, pageable)
                .map(TransactionResponse::from);
    }

    public TransactionResponse getOne(Long userId, Long id) {
        return TransactionResponse.from(getOwnedTransactionOrThrow(userId, id));
    }

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        User user = getUserOrThrow(userId);
        Category category = getOwnedCategoryOrThrow(userId, request.getCategoryId());
        validateAmount(request.getAmount());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .type(request.getType())
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();
        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse update(Long userId, Long id, TransactionRequest request) {
        Transaction transaction = getOwnedTransactionOrThrow(userId, id);
        Category category = getOwnedCategoryOrThrow(userId, request.getCategoryId());
        validateAmount(request.getAmount());

        transaction.update(category, request.getType(), request.getAmount(),
                request.getDescription(), request.getTransactionDate());
        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Transaction transaction = getOwnedTransactionOrThrow(userId, id);
        transactionRepository.delete(transaction);
    }

    // 도전(F-13): 키워드/기간/금액범위 검색
    public Page<TransactionResponse> search(Long userId, String keyword, LocalDate from, LocalDate to,
                                             Long minAmount, Long maxAmount, Pageable pageable) {
        return transactionRepository.searchByKeyword(userId, keyword, from, to, minAmount, maxAmount, pageable)
                .map(TransactionResponse::from);
    }

    // 도전(F-13): CSV 내보내기
    public String exportCsv(Long userId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(userId, from, to);

        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("날짜", "타입", "카테고리", "금액", "설명")
                .build();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Transaction t : transactions) {
                printer.printRecord(t.getTransactionDate(), t.getType(), t.getCategory().getName(),
                        t.getAmount(), t.getDescription());
            }
        } catch (IOException e) {
            throw new IllegalStateException("CSV 생성 중 오류가 발생했습니다.", e);
        }
        return writer.toString();
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.VALIDATION_ERROR, "금액은 0보다 커야 합니다.");
        }
    }

    private Transaction getOwnedTransactionOrThrow(Long userId, Long id) {
        // 본인 데이터 인가(docs/erd.md 4절): 조회는 반드시 id + userId 조합으로만 허용
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
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
