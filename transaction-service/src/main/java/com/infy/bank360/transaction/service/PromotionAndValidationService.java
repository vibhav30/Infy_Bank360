package com.infy.bank360.transaction.service;

import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.transaction.dto.DiscountCalculationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Promotional discount calculation and amount validation engine.
 * Implements business rules BR-01, BR-02, BR-03, BR-04.
 */
@Service
public class PromotionAndValidationService {

    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05"); // 5% Discount
    private static final String ERR_INVALID_AMOUNT = "ERR_INVALID_TRANSACTION_AMOUNT";

    /**
     * Strictly validates that transaction amount is strictly greater than zero,
     * and calculates promotional discount for amounts > 500.
     *
     * @param amount gross transaction amount
     * @return DiscountCalculationResult containing gross, discount, and net amounts
     * @throws InfyBankException if amount is null, zero, or negative
     */
    public DiscountCalculationResult evaluateAmountAndDiscount(BigDecimal amount) {
        // Strict Business Rule (BR-01): Reject null, zero, and negative amounts
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException(
                    ERR_INVALID_AMOUNT,
                    "Transaction amount must be strictly greater than zero."
            );
        }

        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Promotional Discount Rule: Applicable strictly when amount > 500 (BR-04)
        if (amount.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = amount.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal netAmount = amount.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new DiscountCalculationResult(amount, discount, netAmount);
    }
}
