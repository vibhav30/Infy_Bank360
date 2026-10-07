package com.infy.utility;

import com.infy.exception.InfyBankException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility Component: DiscountCalculator
 * Calculates promotional discounts according to BR-03 and BR-04.
 * Reference: spec.md (AC-TXN-02) & plan.md (Section 4.2.3)
 */
@Component
public class DiscountCalculator {

    private static final BigDecimal THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    /**
     * Strictly validates that amount > 0 and computes 5% discount if amount > 500.00.
     *
     * @param amount Gross transaction amount
     * @return Discount amount (rounded HALF_UP to 2 decimal places)
     * @throws InfyBankException if amount is null, zero, or negative
     */
    public BigDecimal calculateDiscount(BigDecimal amount) throws InfyBankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException(
                "ERR_INVALID_TRANSACTION_AMOUNT",
                "Transaction amount must be strictly greater than zero."
            );
        }

        if (amount.compareTo(THRESHOLD) > 0) {
            return amount.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
