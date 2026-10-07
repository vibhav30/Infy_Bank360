package com.infy.bank360.transaction.service;

import com.infy.bank360.common.exception.InfyBankException;
import com.infy.bank360.transaction.dto.DiscountCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Promotion & Validation Service Unit Tests (BR-01, BR-02, BR-03, BR-04)")
class PromotionAndValidationServiceTest {

    private PromotionAndValidationService service;

    @BeforeEach
    void setUp() {
        service = new PromotionAndValidationService();
    }

    @Test
    @DisplayName("BR-01: Null amount must throw InfyBankException with ERR_INVALID_TRANSACTION_AMOUNT")
    void testNullAmountThrowsException() {
        InfyBankException ex = assertThrows(InfyBankException.class, () -> service.evaluateAmountAndDiscount(null));
        assertEquals("ERR_INVALID_TRANSACTION_AMOUNT", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("BR-01: Zero amount (0.00) must throw InfyBankException")
    void testZeroAmountThrowsException() {
        InfyBankException ex = assertThrows(InfyBankException.class, () -> service.evaluateAmountAndDiscount(new BigDecimal("0.00")));
        assertEquals("ERR_INVALID_TRANSACTION_AMOUNT", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("BR-01: Negative amount (-50.00) must throw InfyBankException")
    void testNegativeAmountThrowsException() {
        InfyBankException ex = assertThrows(InfyBankException.class, () -> service.evaluateAmountAndDiscount(new BigDecimal("-50.00")));
        assertEquals("ERR_INVALID_TRANSACTION_AMOUNT", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("BR-03: Sub-threshold amount (100.00 <= 500) receives 0.00 discount")
    void testSubThresholdAmountReceivesZeroDiscount() {
        BigDecimal amount = new BigDecimal("100.00");
        DiscountCalculationResult result = service.evaluateAmountAndDiscount(amount);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getGrossAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscount()));
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getNetAmount()));
    }

    @Test
    @DisplayName("BR-03: Exact boundary amount (500.00) receives 0.00 discount")
    void testBoundaryExactAmountReceivesZeroDiscount() {
        BigDecimal amount = new BigDecimal("500.00");
        DiscountCalculationResult result = service.evaluateAmountAndDiscount(amount);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("500.00").compareTo(result.getGrossAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscount()));
        assertEquals(0, new BigDecimal("500.00").compareTo(result.getNetAmount()));
    }

    @Test
    @DisplayName("BR-04: Over-threshold amount (1000.00 > 500) receives 5% discount (50.00) and net is 950.00")
    void testOverThresholdAmountReceivesFivePercentDiscount() {
        BigDecimal amount = new BigDecimal("1000.00");
        DiscountCalculationResult result = service.evaluateAmountAndDiscount(amount);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.getGrossAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(result.getDiscount()));
        assertEquals(0, new BigDecimal("950.00").compareTo(result.getNetAmount()));
    }

    @Test
    @DisplayName("BR-04: Boundary over (500.01) receives 5% discount (25.00) and net is 475.01")
    void testBoundaryOverAmountReceivesDiscount() {
        BigDecimal amount = new BigDecimal("500.01");
        DiscountCalculationResult result = service.evaluateAmountAndDiscount(amount);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("25.00").compareTo(result.getDiscount()));
        assertEquals(0, new BigDecimal("475.01").compareTo(result.getNetAmount()));
    }
}
