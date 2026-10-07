# Design Document & Implementation Plan (plan.md)
## Project: Infy_Bank360 — Greenfield Digital Banking Platform

---

| Document Metadata | Details |
| :--- | :--- |
| **Document Name** | Implementation Plan & Component Design (`plan.md`) |
| **System** | Infy_Bank360 |
| **Author / Role** | Technical Lead |
| **Base Package** | `com.infy` |
| **Layered Packages** | `com.infy.entity`, `com.infy.dto`, `com.infy.repository`, `com.infy.service`, `com.infy.api`, `com.infy.exception`, `com.infy.utility` |
| **Tech Stack** | Java 21, Spring Boot 3, Spring Cloud Consul, Spring Cloud Gateway, Hibernate/JPA, MySQL 8.0 |

---

## 1. Entity-Relationship Design (Clean & Minimal)

The domain data model consists of **5 core entities** designed with minimal essential fields to keep the architecture clean, high-performing, and decoupled.

### 1.1 ER Diagram (Cardinality & Key Mappings)

```
       +----------------------------+
       |          Customer          |
       +----------------------------+
       | PK customerId (Long)       |
       |    email (String)          |
       |    fullName (String)       |
       |    mobileNumber (String)   |
       |    nationalId (String)     |
       |    kycStatus (String)      |
       +--------------+-------------+
                      |
        1-to-N        |        1-to-N                     1-to-N
   +------------------+------------------+----------------------------------+
   |                                     |                                  |
   v                                     v                                  v
+----------------------------+  +----------------------------+  +----------------------------+
|          Account           |  |      LoanApplication       |  |       ServiceRequest       |
+----------------------------+  +----------------------------+  +----------------------------+
| PK accountNumber (String)  |  | PK loanId (Long)           |  | PK serviceRequestId (Long) |
| FK customerId (Long)       |  | FK customerId (Long)       |  |    trackingId (String)     |
|    accountType (String)    |  |    loanType (String)       |  | FK customerId (Long)       |
|    balance (BigDecimal)    |  |    principalAmount (BigDec)|  |    category (String)       |
|    status (String)         |  |    tenureMonths (Integer)  |  |    description (String)    |
+--------------+-------------+  |    monthlyEmi (BigDecimal) |  |    status (String)         |
               |                |    status (String)         |  +----------------------------+
               | 1-to-N         +----------------------------+
               v
+------------------------------------+
|         TransactionRecord          |
+------------------------------------+
| PK transactionId (Long)            |
| FK sourceAccountNumber (String)    |
|    destinationAccountNumber(String)|
|    grossAmount (BigDecimal)        |
|    discount (BigDecimal)           |
|    netAmount (BigDecimal)          |
|    transactionType (String)        |
|    transactionDate (LocalDateTime) |
|    status (String)                 |
+------------------------------------+
```

---

### 1.2 Entity Specifications & Minimal Field Definitions

#### 1.2.1 `Customer` Entity (`com.infy.entity.Customer`)
Represents customer identity, personal credentials, and onboarding KYC status.

```java
package com.infy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(nullable = false, unique = true, length = 30)
    private String nationalId;

    @Column(nullable = false, length = 30)
    private String kycStatus; // PENDING_KYC, SUBMITTED_FOR_VERIFICATION, KYC_VERIFIED, KYC_REJECTED

    // Constructors, Getters, and Setters
    public Customer() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
}
```

#### 1.2.2 `Account` Entity (`com.infy.entity.Account`)
Represents customer deposit accounts with real-time balance and operational lifecycle.

```java
package com.infy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(length = 12, nullable = false, unique = true)
    private String accountNumber; // 12-digit unique identifier

    @Column(nullable = false)
    private Long customerId; // FK -> Customer.customerId

    @Column(nullable = false, length = 20)
    private String accountType; // SAVINGS, CHECKING

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, FROZEN, CLOSED

    // Constructors, Getters, and Setters
    public Account() {}

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 1.2.3 `TransactionRecord` Entity (`com.infy.entity.TransactionRecord`)
Represents financial ledger entries, discount audit, and payment events.

```java
package com.infy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false, length = 12)
    private String sourceAccountNumber; // FK -> Account.accountNumber

    @Column(length = 12)
    private String destinationAccountNumber; // Nullable for utility bill payments

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(nullable = false, length = 30)
    private String transactionType; // TRANSFER, UTILITY_BILL

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false, length = 20)
    private String status; // SUCCESS, FAILED

    // Constructors, Getters, and Setters
    public TransactionRecord() {}

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 1.2.4 `LoanApplication` Entity (`com.infy.entity.LoanApplication`)
Represents customer credit applications, EMI calculations, and underwriting status.

```java
package com.infy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Column(nullable = false)
    private Long customerId; // FK -> Customer.customerId

    @Column(nullable = false, length = 30)
    private String loanType; // PERSONAL, HOME, VEHICLE

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 5, scale = 2)
    private Double annualInterestRate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyEmi;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false, length = 30)
    private String status; // APPLIED, APPROVED, REJECTED, DISBURSED

    // Constructors, Getters, and Setters
    public LoanApplication() {}

    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public Double getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(Double annualInterestRate) { this.annualInterestRate = annualInterestRate; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public void setMonthlyEmi(BigDecimal monthlyEmi) { this.monthlyEmi = monthlyEmi; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 1.2.5 `ServiceRequest` Entity (`com.infy.entity.ServiceRequest`)
Represents customer service desk and issue resolution tickets.

```java
package com.infy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceRequestId;

    @Column(nullable = false, unique = true, length = 30)
    private String trackingId; // e.g., TCK-10001

    @Column(nullable = false)
    private Long customerId; // FK -> Customer.customerId

    @Column(nullable = false, length = 50)
    private String category; // TRANSACTION_DISPUTE, CARD_REQUEST, ACCOUNT_ISSUE, GENERAL_QUERY

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 30)
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    // Constructors, Getters, and Setters
    public ServiceRequest() {}

    public Long getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(Long serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

## 2. DTO Design & Bean Validation Flow

All API input and output models reside in `com.infy.dto` with strict `DTO` suffixes and JSR-380 validation constraints.

### 2.1 DTO Class Definitions

#### 2.1.1 `CustomerDTO` (`com.infy.dto.CustomerDTO`)
```java
package com.infy.dto;

import jakarta.validation.constraints.*;

public class CustomerDTO {

    private Long customerId;

    @NotBlank(message = "Customer full name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit number")
    private String mobileNumber;

    @NotBlank(message = "National ID is mandatory")
    @Pattern(regexp = "^[A-Z0-9]{8,16}$", message = "National ID must be alphanumeric and 8-16 characters")
    private String nationalId;

    private String kycStatus;

    public CustomerDTO() {}

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }
}
```

#### 2.1.2 `AccountDTO` (`com.infy.dto.AccountDTO`)
```java
package com.infy.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AccountDTO {

    @Pattern(regexp = "^[0-9]{12}$", message = "Account number must be exactly 12 digits")
    private String accountNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(SAVINGS|CHECKING)$", message = "Account type must be SAVINGS or CHECKING")
    private String accountType;

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "100.00", message = "Minimum opening deposit must be at least 100.00")
    private BigDecimal balance;

    private String status;

    public AccountDTO() {}

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 2.1.3 `TransactionDTO` (`com.infy.dto.TransactionDTO`)
```java
package com.infy.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private Long transactionId;

    @NotBlank(message = "Source account number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Source account must be exactly 12 digits")
    private String sourceAccountNumber;

    @Pattern(regexp = "^[0-9]{12}$", message = "Destination account must be exactly 12 digits")
    private String destinationAccountNumber;

    @NotNull(message = "Transaction amount cannot be null")
    @Positive(message = "Transaction amount must be strictly greater than zero")
    private BigDecimal amount;

    private BigDecimal discount;
    private BigDecimal netAmount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "^(TRANSFER|UTILITY_BILL)$", message = "Transaction type must be TRANSFER or UTILITY_BILL")
    private String transactionType;

    private LocalDateTime transactionDate;
    private String status;

    public TransactionDTO() {}

    // Getters and Setters
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public void setSourceAccountNumber(String sourceAccountNumber) { this.sourceAccountNumber = sourceAccountNumber; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 2.1.4 `LoanApplicationDTO` (`com.infy.dto.LoanApplicationDTO`)
```java
package com.infy.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LoanApplicationDTO {

    private Long loanId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Loan type is mandatory")
    @Pattern(regexp = "^(PERSONAL|HOME|VEHICLE)$", message = "Loan type must be PERSONAL, HOME, or VEHICLE")
    private String loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan principal is 1000.00")
    private BigDecimal principalAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 12, message = "Minimum tenure is 12 months")
    @Max(value = 120, message = "Maximum tenure is 120 months")
    private Integer tenureMonths;

    private Double annualInterestRate;
    private BigDecimal monthlyEmi;

    @NotNull(message = "Monthly income is mandatory")
    @DecimalMin(value = "500.00", message = "Monthly declared income must be at least 500.00")
    private BigDecimal monthlyIncome;

    private String status;

    public LoanApplicationDTO() {}

    // Getters and Setters
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public Integer getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }
    public Double getAnnualInterestRate() { return annualInterestRate; }
    public void setAnnualInterestRate(Double annualInterestRate) { this.annualInterestRate = annualInterestRate; }
    public BigDecimal getMonthlyEmi() { return monthlyEmi; }
    public void setMonthlyEmi(BigDecimal monthlyEmi) { this.monthlyEmi = monthlyEmi; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

#### 2.1.5 `ServiceRequestDTO` (`com.infy.dto.ServiceRequestDTO`)
```java
package com.infy.dto;

import jakarta.validation.constraints.*;

public class ServiceRequestDTO {

    private Long serviceRequestId;
    private String trackingId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Category is mandatory")
    @Pattern(regexp = "^(TRANSACTION_DISPUTE|CARD_REQUEST|ACCOUNT_ISSUE|GENERAL_QUERY)$",
             message = "Invalid service request category")
    private String category;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private String status;

    public ServiceRequestDTO() {}

    // Getters and Setters
    public Long getServiceRequestId() { return serviceRequestId; }
    public void setServiceRequestId(Long serviceRequestId) { this.serviceRequestId = serviceRequestId; }
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

---

### 2.2 Step-by-Step Validation & Exception Flow

```
+---------------------------------------------------------------------------------------+
| 1. HTTP Ingress Request: JSON Body arriving at @RequestMapping("/infybank/...")        |
+-------------------------------------------+-------------------------------------------+
                                            |
                                            v
+---------------------------------------------------------------------------------------+
| 2. Controller Endpoint: Annotated with @Valid / @Validated                            |
|    public ResponseEntity<T> executeAction(@Valid @RequestBody DTO dto)                |
+-------------------------------------------+-------------------------------------------+
                                            |
                         +------------------+------------------+
                         | JSR-380 Bean Validation             |
                         v                                     v
             [Constraints Violated]                 [Constraints Passed]
                         |                                     |
                         v                                     v
+----------------------------------+        +-------------------------------------------+
| Spring Throws:                   |        | 3. Service Layer Business Logic           |
| MethodArgumentNotValidException  |        |    - Balance Checks & Account State       |
+----------------+-----------------+        |    - Non-Positive Amount Logic            |
                 |                          |    - Discount Calculation (> 500 Rule)    |
                 |                          +---------------------+---------------------+
                 |                                                |
                 |                               +----------------+---------------+
                 |                               | Business Check                 |
                 |                               v                                v
                 |                    [Rule Violated: e.g. <= 0]          [Valid Request]
                 |                               |                                |
                 |                               v                                v
                 |                  +--------------------------+    +-------------------+
                 |                  | Throws:                  |    | Execute DB Txn &  |
                 |                  | InfyBankException        |    | Return 200 / 201  |
                 |                  +------------+-------------+    +-------------------+
                 |                               |
                 +---------------+---------------+
                                 |
                                 v
+---------------------------------------------------------------------------------------+
| 4. GlobalExceptionHandler (@RestControllerAdvice in com.infy.exception)               |
|    - Intercepts MethodArgumentNotValidException or InfyBankException                  |
|    - Maps to HTTP 400 Bad Request                                                     |
|    - Constructs & returns standard ErrorInfo(errorCode, errorMessage, timestamp)      |
+---------------------------------------------------------------------------------------+
```

1. **Controller Interception:**
   Spring's `DispatcherServlet` binds the HTTP request body to the target DTO. The `@Valid` or `@Validated` annotation invokes the Hibernate Validator engine.
2. **Bean Validation Evaluation:**
   Constraints (`@NotNull`, `@NotBlank`, `@Pattern`, `@Positive`, etc.) are checked. If any field fails, Spring stops controller execution and throws `MethodArgumentNotValidException`.
3. **Service Layer Program Logic:**
   If annotations pass, the DTO enters the Service layer. The service applies strict business rule enforcement:
   ```java
   if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
       throw new InfyBankException("Transaction amount must be strictly greater than zero.");
   }
   ```
   If `amount > 500.00`, a 5% discount is calculated:
   $$\text{discount} = \text{amount} \times 0.05$$
   $$\text{netAmount} = \text{amount} - \text{discount}$$
4. **Exception Handling & ErrorInfo Envelope:**
   `GlobalExceptionHandler` catches the custom `InfyBankException` or `MethodArgumentNotValidException` and serializes the unified error structure:
   ```json
   {
     "errorCode": "ERR_INVALID_TRANSACTION_AMOUNT",
     "errorMessage": "Transaction amount must be strictly greater than zero.",
     "timestamp": "2026-10-07T19:00:00.000Z"
   }
   ```

---

## 3. API Endpoints Map (Prefixed with `/infybank/`)

All controller endpoints across the platform are uniformly prefixed with `/infybank` (e.g., `@RequestMapping("/infybank/...")`) and return typed `ResponseEntity<T>` envelopes.

| Controller Class | HTTP Method | Endpoint Path | Request Body | Response Body | HTTP Status | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`CustomerAPI`** | `POST` | `/infybank/customers` | `@Valid CustomerDTO` | `CustomerDTO` | `201 CREATED` | Register a new customer profile. |
| | `GET` | `/infybank/customers/{customerId}` | None | `CustomerDTO` | `200 OK` | Retrieve customer profile by ID. |
| | `PUT` | `/infybank/customers/{customerId}/kyc` | `CustomerDTO` | `CustomerDTO` | `200 OK` | Update KYC verification status. |
| **`AccountAPI`** | `POST` | `/infybank/accounts` | `@Valid AccountDTO` | `AccountDTO` | `201 CREATED` | Open a new Savings/Checking account. |
| | `GET` | `/infybank/accounts/{accountNumber}` | None | `AccountDTO` | `200 OK` | Inquire account balance and details. |
| | `GET` | `/infybank/accounts/customer/{customerId}`| None | `List<AccountDTO>` | `200 OK` | List all accounts owned by a customer. |
| | `PUT` | `/infybank/accounts/{accountNumber}/status`| `AccountDTO` | `AccountDTO` | `200 OK` | Update status (`ACTIVE`, `FROZEN`, `CLOSED`). |
| **`TransactionAPI`**| `POST` | `/infybank/transactions/transfer` | `@Valid TransactionDTO` | `TransactionDTO` | `201 CREATED` | Transfer funds between accounts. |
| | `POST` | `/infybank/transactions/bill-pay` | `@Valid TransactionDTO` | `TransactionDTO` | `201 CREATED` | Pay utility bill with discount logic. |
| | `GET` | `/infybank/transactions/{accountNumber}` | None | `List<TransactionDTO>` | `200 OK` | Retrieve transaction statement for an account. |
| **`LoanAPI`** | `POST` | `/infybank/loans/calculate-emi` | `LoanApplicationDTO` | `LoanApplicationDTO` | `200 OK` | Simulate monthly EMI without saving. |
| | `POST` | `/infybank/loans/apply` | `@Valid LoanApplicationDTO`| `LoanApplicationDTO` | `201 CREATED` | Submit a new loan application. |
| | `GET` | `/infybank/loans/{loanId}` | None | `LoanApplicationDTO` | `200 OK` | Fetch loan application status and details. |
| | `PUT` | `/infybank/loans/{loanId}/status` | `LoanApplicationDTO` | `LoanApplicationDTO` | `200 OK` | Underwrite / Approve / Reject loan. |
| | `POST` | `/infybank/loans/{loanId}/disburse` | None | `LoanApplicationDTO` | `200 OK` | Disburse loan amount to account. |
| **`ServiceRequestAPI`**| `POST`| `/infybank/requests` | `@Valid ServiceRequestDTO` | `ServiceRequestDTO` | `201 CREATED` | Create customer support / dispute ticket. |
| | `GET` | `/infybank/requests/{trackingId}` | None | `ServiceRequestDTO` | `200 OK` | Track ticket status by unique Tracking ID. |
| | `GET` | `/infybank/requests/customer/{customerId}`| None | `List<ServiceRequestDTO>` | `200 OK` | List all tickets raised by a customer. |
| | `PUT` | `/infybank/requests/{serviceRequestId}/status` | `ServiceRequestDTO` | `ServiceRequestDTO` | `200 OK` | Update ticket status (`RESOLVED`, `CLOSED`). |

---

## 4. Component Wiring & Injection Strategy

### 4.1 Strict Constructor Injection Policy
To guarantee immutability, thread safety, explicit dependency contracts, and frictionless unit testing, **Constructor Injection is strictly mandatory** across all Controllers and Services.
- **Rule 1:** `@Autowired` on private fields is strictly forbidden.
- **Rule 2:** All injected collaborators must be declared as `private final`.
- **Rule 3:** Components must expose a public constructor (or use Lombok `@RequiredArgsConstructor`).

### 4.2 Standard Layer Implementation Examples

#### 4.2.1 Controller Layer Wiring (`com.infy.api.TransactionAPI`)
```java
package com.infy.api;

import com.infy.dto.TransactionDTO;
import com.infy.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/infybank/transactions")
@Validated
public class TransactionAPI {

    private final TransactionService transactionService;

    // Explicit Constructor Injection
    public TransactionAPI(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transferFunds(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO result = transactionService.transferFunds(transactionDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PostMapping("/bill-pay")
    public ResponseEntity<TransactionDTO> payUtilityBill(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO result = transactionService.payUtilityBill(transactionDTO);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getStatement(@PathVariable String accountNumber) {
        List<TransactionDTO> statement = transactionService.getTransactionHistory(accountNumber);
        return ResponseEntity.ok(statement);
    }
}
```

#### 4.2.2 Service Layer Wiring & Business Logic (`com.infy.service.TransactionServiceImpl`)
```java
package com.infy.service;

import com.infy.dto.TransactionDTO;
import com.infy.entity.Account;
import com.infy.entity.TransactionRecord;
import com.infy.exception.InfyBankException;
import com.infy.repository.AccountRepository;
import com.infy.repository.TransactionRecordRepository;
import com.infy.utility.DiscountCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRecordRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final DiscountCalculator discountCalculator;

    // Explicit Constructor Injection
    public TransactionServiceImpl(
            TransactionRecordRepository transactionRepository,
            AccountRepository accountRepository,
            DiscountCalculator discountCalculator) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.discountCalculator = discountCalculator;
    }

    @Override
    public TransactionDTO transferFunds(TransactionDTO dto) {
        // 1. Strict Validation: Amount must be > 0
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException("Transaction amount must be strictly greater than zero.");
        }

        // 2. Account checks
        Account sourceAccount = accountRepository.findById(dto.getSourceAccountNumber())
                .orElseThrow(() -> new InfyBankException("Source account does not exist."));

        if (!"ACTIVE".equalsIgnoreCase(sourceAccount.getStatus())) {
            throw new InfyBankException("Source account is not active.");
        }

        // 3. Discount calculation (> 500 Rule)
        BigDecimal discount = discountCalculator.calculateDiscount(dto.getAmount());
        BigDecimal netAmount = dto.getAmount().subtract(discount);

        if (sourceAccount.getBalance().compareTo(netAmount) < 0) {
            throw new InfyBankException("Insufficient available balance for transfer.");
        }

        // 4. Update ledger and persist
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(netAmount));
        accountRepository.save(sourceAccount);

        TransactionRecord record = new TransactionRecord();
        record.setSourceAccountNumber(dto.getSourceAccountNumber());
        record.setDestinationAccountNumber(dto.getDestinationAccountNumber());
        record.setGrossAmount(dto.getAmount());
        record.setDiscount(discount);
        record.setNetAmount(netAmount);
        record.setTransactionType(dto.getTransactionType());
        record.setTransactionDate(LocalDateTime.now());
        record.setStatus("SUCCESS");

        TransactionRecord saved = transactionRepository.save(record);

        dto.setTransactionId(saved.getTransactionId());
        dto.setDiscount(discount);
        dto.setNetAmount(netAmount);
        dto.setTransactionDate(saved.getTransactionDate());
        dto.setStatus(saved.getStatus());

        return dto;
    }
}
```

#### 4.2.3 Utility Component (`com.infy.utility.DiscountCalculator`)
```java
package com.infy.utility;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DiscountCalculator {

    private static final BigDecimal THRESHOLD = new BigDecimal("500.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    /**
     * Calculates discount: 5% if amount > 500.00, otherwise 0.00.
     */
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (amount != null && amount.compareTo(THRESHOLD) > 0) {
            return amount.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
```

---

## 5. Configuration & Infrastructure Plan

### 5.1 Spring Cloud Consul (Service Discovery & Centralized Configuration)

All microservices integrate with HashiCorp Consul running on port `8500`.

#### 5.1.1 Consul Discovery & Config Settings (`application.yml` for Microservices)
```yaml
spring:
  application:
    name: transaction-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        register: true
        instance-id: ${spring.application.name}:${random.value}
        service-name: ${spring.application.name}
        prefer-ip-address: true
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-critical-timeout: 30s
      config:
        enabled: true
        format: YAML
        prefix: config
        default-context: application
        data-key: data
        fail-fast: true

server:
  port: 8084
```

---

### 5.2 Spring Cloud Gateway Route Predicates & Filters

`api-gateway` (Port `8080`) serves as the single unified entry point for all frontend and mobile requests. It routes `/infybank/**` requests dynamically to downstream services registered with Consul using the `lb://` scheme.

#### 5.2.1 Gateway Routing Configuration (`application.yml` for `api-gateway`)
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        # 1. Customer Service Route
        - id: customer-service-route
          uri: lb://customer-service
          predicates:
            - Path=/infybank/customers/**
          filters:
            - AddRequestHeader=X-Forwarded-Prefix, /infybank/customers

        # 2. Account Service Route
        - id: account-service-route
          uri: lb://account-service
          predicates:
            - Path=/infybank/accounts/**
          filters:
            - AddRequestHeader=X-Forwarded-Prefix, /infybank/accounts

        # 3. Transaction Service Route
        - id: transaction-service-route
          uri: lb://transaction-service
          predicates:
            - Path=/infybank/transactions/**
          filters:
            - AddRequestHeader=X-Forwarded-Prefix, /infybank/transactions

        # 4. Loan Service Route
        - id: loan-service-route
          uri: lb://loan-service
          predicates:
            - Path=/infybank/loans/**
          filters:
            - AddRequestHeader=X-Forwarded-Prefix, /infybank/loans

        # 5. Service Request Route
        - id: service-request-route
          uri: lb://service-request-service
          predicates:
            - Path=/infybank/requests/**
          filters:
            - AddRequestHeader=X-Forwarded-Prefix, /infybank/requests

      # Global CORS Configuration for Angular Web Client
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

---

## 6. Implementation Summary & Verification Checklist

| Architecture Milestone | Target Verification Step | Acceptance Status |
| :--- | :--- | :--- |
| **Package Layering** | Standard packages (`com.infy.entity`, `com.infy.dto`, `com.infy.repository`, `com.infy.service`, `com.infy.api`, `com.infy.exception`, `com.infy.utility`) | Standard Enforced |
| **Minimal ER Design** | 5 Entities (`Customer`, `Account`, `TransactionRecord`, `LoanApplication`, `ServiceRequest`) with concise fields | Completed |
| **DTO Matching** | Suffix `DTO` on all transfer objects with JSR-380 annotations | Completed |
| **Validation Flow** | `@Valid` -> `MethodArgumentNotValidException` / `InfyBankException` -> `GlobalExceptionHandler` returning `ErrorInfo` | Verified |
| **Amount Logic** | Amount $\le 0$ strictly rejected; Amount $> 500$ gets 5% discount | Business Rule Enforced |
| **Uniform Paths** | All REST controller paths prefixed with `/infybank/**` | Configured |
| **Wiring Strategy** | 100% Constructor Injection (`final` fields) across controllers and services | Design Verified |
| **Consul & Gateway** | Consul registry on 8500 + Gateway routes via `lb://` for all `/infybank/**` paths | Architecture Ready |

---
*Design Document Complete — Ready for Sprint Implementation.*
