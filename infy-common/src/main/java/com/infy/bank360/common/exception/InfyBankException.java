package com.infy.bank360.common.exception;

import lombok.Getter;

/**
 * Base custom exception for business rule violations across the Infy_Bank360 platform.
 */
@Getter
public class InfyBankException extends RuntimeException {

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
}
