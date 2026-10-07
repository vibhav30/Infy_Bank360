package com.infy.exception;

/**
 * Base custom checked exception for business rule violations across the Infy_Bank360 platform.
 * Extends java.lang.Exception in accordance with enterprise guidelines.
 * Reference: spec.md (AC-AUTH-02, AC-TXN-01) & plan.md (Section 2.2)
 */
public class InfyBankException extends Exception {

    private static final long serialVersionUID = 1L;
    private final String errorCode;

    public InfyBankException(String message) {
        super(message);
        this.errorCode = "ERR_BUSINESS_VALIDATION";
    }

    public InfyBankException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public InfyBankException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
