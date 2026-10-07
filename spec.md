# Technical Specification Document (spec.md)
## Project: Infy_Bank360 — Cloud-Native Enterprise Digital Banking Platform

---

| Specification Metadata | Details |
| :--- | :--- |
| **System Name** | Infy_Bank360 Platform |
| **Role / Author** | Lead Solutions Architect |
| **Base Package** | `com.infy` |
| **Document Version** | `1.0.0-RELEASE` |
| **Target Frameworks** | Java 21, Spring Boot 3.3.x, Spring Cloud 2023.x, Angular 17, Bootstrap 5.3 |
| **Infrastructure** | Spring Cloud Consul (Discovery & Config), Spring Cloud Gateway, Resilience4j, MySQL 8.0 |

---

## 1. Architectural Topology & System Landscape

### 1.1 Architecture Overview
Infy_Bank360 is built as an enterprise-grade, distributed microservices ecosystem running on **Java 21** and **Spring Boot 3**. The architecture enforces strict domain isolation, database-per-service pattern, declarative inter-service communication via **OpenFeign**, client-side load balancing and fault isolation via **Resilience4j**, centralized discovery and key-value configuration via **Spring Cloud Consul**, and edge routing with security enforcement via **Spring Cloud Gateway**.

```
                                  +---------------------------------------+
                                  |      Angular 17 + Bootstrap 5 UI      |
                                  |   [Customer Portal | Ops Dashboard]   |
                                  +-------------------+-------------------+
                                                      |
                                                      | HTTPS / REST (JWT Bearer)
                                                      v
                                  +---------------------------------------+
                                  |     api-gateway (Spring Cloud: 8080)   |
                                  +-------------------+-------------------+
                                                      |
                     +--------------------------------+-------------------------------+
                     |                                |                               |
                     v                                v                               v
           Consul Discovery & Config (Port 8500) <----+----> Centralized Health Checks
                     |
       +-------------+-------------+------------------+-----------------+------------------+
       |                           |                  |                 |                  |
       v                           v                  v                 v                  v
+--------------+            +--------------+   +--------------+  +--------------+   +--------------+
| auth-service |            | customer-svc |   | account-svc  |  | txn-service  |   | loan-service |
|  Port: 8081  |            |  Port: 8082  |   |  Port: 8083  |  |  Port: 8084  |   |  Port: 8085  |
| infy_auth_db |            | infy_cust_db |   | infy_acct_db |  | infy_txn_db  |   | infy_loan_db |
+--------------+            +--------------+   +--------------+  +--------------+   +--------------+
                                   ^                  ^                 |                  |
                                   |                  +--------+--------+                  |
                                   |                           | OpenFeign                 |
                                   +---------------------------+---------------------------+
                                   |                           |
                                   v                           v
                            +--------------+            +--------------------------+
                            | service-req  |            | audit-notification-svc   |
                            |  Port: 8086  |            |        Port: 8087        |
                            | infy_req_db  |            |       infy_audit_db      |
                            +--------------+            +--------------------------+
```

---

## 2. Microservices Breakdown Matrix

The platform is partitioned into **8 services** (including the API Gateway and Service Registry/Consul integration), strictly observing single-responsibility and domain-driven design principles.

| # | Service Name | Service ID (`spring.application.name`) | Port | Database Schema | Core Responsibilities | Upstream & External Dependencies |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **`service-registry`** | `consul` / HashiCorp Consul Agent | `8500` | In-Memory / File KV Store | Dynamic service discovery registry, distributed key-value configuration (`/config/`), heartbeat health verification. | All microservices register via `spring-cloud-starter-consul-discovery`. |
| **2** | **`api-gateway`** | `api-gateway` | `8080` | Stateless (No DB) | Edge routing, reactive rate limiting, CORS resolution, JWT token validation and role claim relay via downstream HTTP headers (`X-User-Id`, `X-User-Roles`). | Consul Registry, Downstream microservices (8081–8087). |
| **3** | **`auth-service`** | `auth-service` | `8081` | `infy_auth_db` | User identity credentials, BCrypt password hashing, Spring Security 6 authentication manager, JWT generation and signing. | Consul Config/Discovery, MySQL. |
| **4** | **`customer-service`** | `customer-service` | `8082` | `infy_customer_db` | Customer profile (CIF), KYC document upload and storage metadata, onboarding verification lifecycle (`PENDING_KYC` to `KYC_VERIFIED`). | Consul, MySQL, `auth-service` (User sync), `audit-notification-service`. |
| **5** | **`account-service`** | `account-service` | `8083` | `infy_account_db` | Savings/Checking accounts, 12-digit account number generation, available balance inquiries, state management (`ACTIVE`, `FROZEN`, `CLOSED`), atomic debit/credit operations. | Consul, MySQL, `customer-service` (KYC check via OpenFeign), `audit-notification-service`. |
| **6** | **`transaction-service`** | `transaction-service` | `8084` | `infy_transaction_db` | Intra-bank transfers, utility bill payments, transaction amount validation (`amount > 0`), promotional discount calculation (`amount > 500`), double-entry transaction ledger. | Consul, MySQL, `account-service` (OpenFeign balance check & debit/credit), `audit-notification-service`. |
| **7** | **`loan-service`** | `loan-service` | `8085` | `infy_loan_db` | Loan catalog, mathematical EMI computation engine, loan application submission, officer credit underwriting, loan disbursement. | Consul, MySQL, `account-service` (OpenFeign disbursement credit), `customer-service`. |
| **8** | **`service-request-service`** | `service-request-service` | `8086` | `infy_service_request_db` | Helpdesk ticketing, dispute management, categorization, assignment lifecycle (`OPEN` -> `IN_PROGRESS` -> `RESOLVED` -> `CLOSED`), employee audit remarks. | Consul, MySQL, `customer-service`, `audit-notification-service`. |
| **9** | **`audit-notification-service`** | `audit-notification-service` | `8087` | `infy_audit_db` | Compliance fraud surveillance (transactions $\ge \$10,000$), regulatory immutable audit trail, customer event notifications (push/in-app). | Consul, MySQL, invoked by all business services via OpenFeign / async events. |

---

## 3. Acceptance Criteria (AC) & Detailed Business Rules by Service

### 3.1 `api-gateway`
- **AC-GW-01 (Dynamic Route Resolution):** The gateway must dynamically resolve service locations from Spring Cloud Consul using load-balanced URI schemas: `lb://<SERVICE-ID>`.
- **AC-GW-02 (Stateless JWT Verification):** The gateway global filter intercepts all non-public requests (`/api/v1/auth/**` is public), validates the `Authorization: Bearer <token>` header, verifies the cryptographic signature (HMAC-SHA256), and checks token expiration.
- **AC-GW-03 (Header Propagation):** Upon successful validation, the gateway strips the client's internal headers and injects trusted downstream headers:
  - `X-User-Id`: Subject identifier (CIF or username).
  - `X-User-Roles`: Comma-delimited authority list (e.g., `ROLE_CUSTOMER`, `ROLE_EMPLOYEE`).
- **AC-GW-04 (CORS Policy):** Gateway preflight `OPTIONS` requests must permit origins configured for the Angular client (`http://localhost:4200`), supporting standard HTTP verbs (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`) and exposed headers.
- **AC-GW-05 (Gateway Circuit Breaker):** Routes must be wrapped with a Resilience4j GatewayFilter fallback route (`/fallback/downstream-unavailable`) returning HTTP 503.

### 3.2 `auth-service`
- **AC-AUTH-01 (Credential Storage):** Passwords must never be stored in plaintext. They must be salted and hashed using `BCryptPasswordEncoder` with strength factor 10.
- **AC-AUTH-02 (User Registration):** Endpoints accept registration payloads, verify email and national ID uniqueness against `infy_auth_db`. Replicas throw custom `InfyBankException("ERR_USER_EXISTS", ...)` resulting in HTTP 409 Conflict.
- **AC-AUTH-03 (Token Issuance):** Successful authentication yields a signed JWT containing standard claims: `sub` (username/CIF), `roles` (`ROLE_CUSTOMER`, `ROLE_EMPLOYEE`, `ROLE_COMPLIANCE`), `iat` (issued-at), `exp` (expiration set to 86,400 seconds / 24 hours).

### 3.3 `customer-service`
- **AC-CUST-01 (Customer Profile Creation):** When a user registers, an associated customer profile is created with onboarding status `PENDING_KYC`.
- **AC-CUST-02 (KYC Document Submission):** Customers upload Proof of Identity (Passport, Driving License, National ID) and Proof of Address.
  - Allowed file formats: `image/jpeg`, `image/png`, `application/pdf`.
  - File size strictly bounded: Max 5,242,880 bytes (5MB).
  - State transitions to `SUBMITTED_FOR_VERIFICATION`.
- **AC-CUST-03 (KYC Verification Triage):**
  - Employees (`ROLE_EMPLOYEE`) can view pending queues.
  - Upon Approval: Status transitions to `KYC_VERIFIED`. Account creation capability is unlocked.
  - Upon Rejection: A non-blank rejection reason is mandatory; status transitions to `KYC_REJECTED`.
- **AC-CUST-04 (Profile Invariant):** Profile modifications are blocked if the customer is in `KYC_REJECTED` status until re-submission.

### 3.4 `account-service`
- **AC-ACCT-01 (Account Provisioning Precondition):** Account provisioning requires verification from `customer-service` via OpenFeign. If `kycStatus != KYC_VERIFIED`, provisioning fails with HTTP 400 Bad Request (`ERR_KYC_NOT_VERIFIED`).
- **AC-ACCT-02 (Account Number Generation):** Generates a cryptographically random, collision-resistant 12-digit numeric account number (`^[0-9]{12}$`).
- **AC-ACCT-03 (Initial Deposit Minimum):** Minimum opening deposit must be $\ge \$100.00$. Amounts $< \$100.00$ are rejected with `InfyBankException` (HTTP 400).
- **AC-ACCT-04 (Account Status Lifecycle):**
  - Permitted states: `ACTIVE`, `FROZEN`, `CLOSED`.
  - State changes to `FROZEN` can only be initiated by `ROLE_COMPLIANCE`.
- **AC-ACCT-05 (Atomic Balance Operations):**
  - `debit`: Checks `status == ACTIVE` and `availableBalance >= debitAmount`. If `status == FROZEN`, strictly reject with HTTP 403 (`ERR_ACCOUNT_FROZEN`). If `availableBalance < debitAmount`, throw `InsufficientBalanceException` (HTTP 400).
  - `credit`: Checks `status != CLOSED`. Credits ledger balance and updates available balance.
  - Concurrency Control: Uses JPA `@Version` optimistic locking on the `Account` entity to prevent race conditions during concurrent debits.

### 3.5 `transaction-service`
- **AC-TXN-01 (Non-Positive Amount Rejection - BR-01):**
  - Any transaction payload containing `amount <= 0.00` (zero or negative numbers) must be **immediately rejected**.
  - System throws custom exception `InfyBankException("ERR_INVALID_TRANSACTION_AMOUNT", "Transaction amount must be strictly greater than zero.")`.
  - Mapped to **HTTP 400 Bad Request**. No database transaction or inter-service call is executed.
- **AC-TXN-02 (Promotional Discount Business Engine - BR-03 & BR-04):**
  - **Threshold Rule:** Evaluates eligible purchases and utility bill payments:
    - **Condition `amount > 500.00`:** Apply a 5% promotional discount.
      $$\text{discount} = \text{amount} \times 0.05 \quad (\text{rounded to 2 decimal places, HALF\_UP})$$
      $$\text{netAmountDebited} = \text{amount} - \text{discount}$$
    - **Condition `0.00 < amount <= 500.00`:** Apply **zero discount**.
      $$\text{discount} = 0.00$$
      $$\text{netAmountDebited} = \text{amount}$$
  - The transaction record in `infy_transaction_db` must persist `grossAmount`, `discountApplied`, and `netAmountDebited`.
- **AC-TXN-03 (Fund Transfer Workflow & Distributed Consistency):**
  - Intra-bank transfer requires valid `sourceAccountNumber` and `destinationAccountNumber`.
  - Verification check: `sourceAccountNumber != destinationAccountNumber`; otherwise throw HTTP 400 (`ERR_SELF_TRANSFER_PROHIBITED`).
  - Calls `account-service` via OpenFeign:
    1. Debits `netAmountDebited` from source account.
    2. Credits full transfer amount to destination account.
  - If the credit call fails after debit, an automated compensating credit is initiated, or wrapped in a distributed saga pattern to guarantee ledger consistency.
- **AC-TXN-04 (Utility Bill Payment - US-10):**
  - Supports utility categories: `ELECTRICITY`, `MOBILE_POSTPAID`, `WATER`, `INTERNET`.
  - Evaluates discount logic; debits `netAmountDebited` from customer's account; generates a unique receipt alphanumeric reference (`TXN-BILL-XXXXX`).
- **AC-TXN-05 (Compliance High-Value Surveillance - BR-07):**
  - If `amount >= 10,000.00`, flag `complianceSurveillance = true` and publish an audit event to `audit-notification-service` via OpenFeign.

### 3.6 `loan-service`
- **AC-LOAN-01 (Mathematical EMI Calculation Formula):**
  - The EMI calculator computes monthly installments using the standard reducing balance equation:
    $$\text{EMI} = \frac{P \times r \times (1+r)^n}{(1+r)^n - 1}$$
    Where:
    - $P$ = Principal loan amount
    - $r$ = Monthly interest rate ($\text{Annual Rate} / 12 / 100$)
    - $n$ = Loan tenure in months ($12 \le n \le 120$)
  - Result rounded to 2 decimal places using `RoundingMode.HALF_UP`.
- **AC-LOAN-02 (Underwriting Debt-to-Income Constraint):**
  - During application review, the loan officer validates declared income.
  - Rule: If computed $\text{Monthly EMI} > (\text{Declared Monthly Income} \times 0.50)$, the system flags a warning `HIGH_DTI_RATIO` and requires officer justification.
- **AC-LOAN-03 (Disbursement Execution):**
  - Status lifecycle: `APPLIED` $\to$ `UNDER_REVIEW` $\to$ `APPROVED` / `REJECTED` $\to$ `DISBURSED`.
  - Upon triggering "Disburse", `loan-service` invokes `account-service` via OpenFeign to credit the borrower's designated active account.

### 3.7 `service-request-service`
- **AC-REQ-01 (Support Ticket Creation):**
  - Allows customers to raise tickets under classifications: `TRANSACTION_DISPUTE`, `CARD_REQUEST`, `ACCOUNT_ISSUE`, `GENERAL_QUERY`.
  - Auto-assigns unique sequential tracking identifier: `TCK-` + padded integer (e.g., `TCK-10001`). Initial status is `OPEN`.
- **AC-REQ-02 (Employee Triage & Resolution):**
  - Bank officers (`ROLE_EMPLOYEE`) assign tickets to themselves, moving status to `IN_PROGRESS`.
  - Entering resolution notes moves status to `RESOLVED`.
- **AC-REQ-03 (Customer Closure Acceptance):**
  - Only the customer who raised the ticket can execute "Accept & Close", transitioning state to `CLOSED`.

### 3.8 `audit-notification-service`
- **AC-AUD-01 (Immutable Audit Logging):**
  - Persists system events: `userId`, `userRole`, `action` (`LOGIN`, `KYC_VERIFICATION`, `ACCOUNT_STATE_CHANGE`, `FUND_TRANSFER`, `LOAN_APPROVAL`), `resourceId`, `clientIpAddress`, and `timestamp`.
  - Records are strictly append-only; updates and hard deletes are rejected by database triggers.
- **AC-AUD-02 (Anti-Money Laundering Surveillance Feed):**
  - Transactions $\ge \$10,000.00$ are indexed in a high-priority compliance table.
  - Compliance officers (`ROLE_COMPLIANCE`) have dedicated read/flag actions.
- **AC-AUD-03 (Customer Real-Time Alerts):**
  - Stores notifications for transaction debits, bill discounts, and loan disbursements with read/unread statuses.

---

## 4. Strict Input Validation & Edge Case Specifications

### 4.1 JSR-380 Bean Validation Matrix
All microservice endpoints enforce strict declarative Bean Validation annotations on incoming DTO request bodies. Requests violating constraints are intercepted by the Global Exception Handler before entering service business logic.

| DTO Name | Field Name | Validation Annotations | Constraint Description / Failure Message |
| :--- | :--- | :--- | :--- |
| **`TransferRequestDTO`** | `sourceAccountNumber` | `@NotBlank`<br>`@Pattern(regexp = "^[0-9]{12}$")` | Must not be empty; must be an exact 12-digit numeric string. |
| | `destinationAccountNumber` | `@NotBlank`<br>`@Pattern(regexp = "^[0-9]{12}$")` | Must not be empty; must be an exact 12-digit numeric string. |
| | `amount` | `@NotNull`<br>`@Positive` | Required; must be strictly greater than zero (`0.00`). |
| | `remarks` | `@Size(max = 255)` | Optional; maximum allowable length is 255 characters. |
| | `channel` | `@NotNull`<br>`@Pattern(regexp = "^(ONLINE\|MOBILE\|UTILITY)$")` | Must match one of the allowed payment channels. |
| **`BillPaymentRequestDTO`**| `accountNumber` | `@NotBlank`<br>`@Pattern(regexp = "^[0-9]{12}$")` | Exact 12-digit numeric source account string. |
| | `billerCategory` | `@NotNull`<br>`@Pattern(regexp = "^(ELECTRICITY\|MOBILE_POSTPAID\|WATER\|INTERNET)$")` | Must be a recognized platform biller category. |
| | `billReferenceNumber` | `@NotBlank`<br>`@Size(min = 5, max = 30)` | Biller utility consumer account / invoice reference. |
| | `amount` | `@NotNull`<br>`@Positive` | Transaction bill amount strictly $> 0.00$. |
| **`CustomerRegistrationDTO`**| `fullName` | `@NotBlank`<br>`@Size(min = 2, max = 100)` | Name must be between 2 and 100 characters. |
| | `email` | `@NotBlank`<br>`@Email`<br>`@Size(max = 120)` | Must be a syntactically valid RFC 5322 email address. |
| | `mobileNumber` | `@NotBlank`<br>`@Pattern(regexp = "^[6-9][0-9]{9}$")` | Must be a valid 10-digit mobile number starting with 6-9. |
| | `password` | `@NotBlank`<br>`@Size(min = 8, max = 64)`<br>`@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$")` | Min 8 chars; must include 1 uppercase, 1 lowercase, 1 number, and 1 special symbol. |
| | `nationalId` | `@NotBlank`<br>`@Pattern(regexp = "^[A-Z0-9]{8,16}$")` | Must be alphanumeric, length between 8 and 16 characters. |
| **`AccountCreateDTO`** | `customerId` | `@NotNull`<br>`@Positive` | Valid internal positive integer customer CIF identifier. |
| | `accountType` | `@NotNull`<br>`@Pattern(regexp = "^(SAVINGS\|CHECKING)$")` | Account type must be either `SAVINGS` or `CHECKING`. |
| | `initialDeposit` | `@NotNull`<br>`@Min(value = 100)` | Opening balance deposit must be at least $100.00. |
| **`LoanApplicationDTO`** | `customerId` | `@NotNull`<br>`@Positive` | Customer CIF identifier. |
| | `loanType` | `@NotNull`<br>`@Pattern(regexp = "^(PERSONAL\|HOME\|VEHICLE)$")` | Loan catalog type. |
| | `principalAmount` | `@NotNull`<br>`@Min(value = 1000)` | Minimum principal requested is $1,000.00. |
| | `tenureMonths` | `@NotNull`<br>`@Min(12)`<br>`@Max(120)` | Tenure must be between 12 months and 120 months. |
| | `declaredMonthlyIncome` | `@NotNull`<br>`@Min(value = 500)` | Declared income must be at least $500.00. |
| **`ServiceRequestDTO`** | `customerId` | `@NotNull`<br>`@Positive` | Customer CIF identifier. |
| | `category` | `@NotNull`<br>`@Pattern(regexp = "^(TRANSACTION_DISPUTE\|CARD_REQUEST\|ACCOUNT_ISSUE\|GENERAL_QUERY)$")` | Categorization of support ticket. |
| | `subject` | `@NotBlank`<br>`@Size(min = 5, max = 100)` | Ticket title between 5 and 100 characters. |
| | `description` | `@NotBlank`<br>`@Size(min = 10, max = 1000)` | Ticket description between 10 and 1000 characters. |

---

### 4.2 Program Logic: Amount Validation & Promotional Discount Engine
The system enforces monetary integrity using `BigDecimal` for zero-loss arithmetic. The exact algorithmic flow and implementation contract is specified below:

```java
package com.infy.transaction.service.impl;

import com.infy.common.exception.InfyBankException;
import com.infy.transaction.dto.DiscountCalculationResult;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PromotionAndValidationServiceImpl {

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
        // Strict Business Rule: Reject null, zero, and negative amounts
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InfyBankException(
                ERR_INVALID_AMOUNT,
                "Transaction amount must be strictly greater than zero."
            );
        }

        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        // Promotional Discount Rule: Applicable strictly when amount > 500
        if (amount.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = amount.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal netAmount = amount.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new DiscountCalculationResult(amount, discount, netAmount);
    }
}
```

#### Detailed Boundary Value & Edge Case Analysis Matrix

| Scenario / Input Value | Evaluation Path | Business Logic & Rule Trigger | Resulting Discount | Net Amount Debited | Expected HTTP Status / Exception |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `null` | Validation Failure | Null check fails | N/A | N/A | **HTTP 400 Bad Request**<br>`InfyBankException("ERR_INVALID_TRANSACTION_AMOUNT", ...)` |
| `-500.00` | Validation Failure | `amount.compareTo(0) < 0` | N/A | N/A | **HTTP 400 Bad Request**<br>`InfyBankException("ERR_INVALID_TRANSACTION_AMOUNT", ...)` |
| `-0.01` | Validation Failure | `amount.compareTo(0) < 0` | N/A | N/A | **HTTP 400 Bad Request**<br>`InfyBankException("ERR_INVALID_TRANSACTION_AMOUNT", ...)` |
| `0.00` | Validation Failure | `amount.compareTo(0) == 0` | N/A | N/A | **HTTP 400 Bad Request**<br>`InfyBankException("ERR_INVALID_TRANSACTION_AMOUNT", ...)` |
| `0.01` | Valid / Sub-threshold | $0.01 \le 500.00 \to$ BR-03 | `$0.00` | `$0.01` | **HTTP 200 OK** / Transaction executes |
| `500.00` | Boundary Exact | $500.00 \ngtr 500.00 \to$ BR-03 (No discount) | `$0.00` | `$500.00` | **HTTP 200 OK** / Full amount debited |
| `500.01` | Boundary Over | $500.01 > 500.00 \to$ BR-04 ($500.01 \times 0.05$) | `$25.00` | `$475.01` | **HTTP 200 OK** / 5% discount applied |
| `1,000.00` | Valid Over-threshold | $1,000.00 > 500.00 \to$ BR-04 ($1000 \times 0.05$) | `$50.00` | `$950.00` | **HTTP 200 OK** / 5% discount applied |
| `10,000.00` | AML High Value Boundary | $10,000.00 > 500.00$ & $\ge 10,000.00 \to$ BR-07 | `$500.00` | `$9,500.00` | **HTTP 200 OK** + Compliance AML Flagged |
| Same Source & Target | Transfer Validation | `srcAccount.equals(destAccount)` | N/A | N/A | **HTTP 400 Bad Request** (`ERR_SELF_TRANSFER_PROHIBITED`) |
| Account `FROZEN` | Lifecycle Check | Account status is `FROZEN` | N/A | N/A | **HTTP 403 Forbidden** (`ERR_ACCOUNT_FROZEN`) |
| Insufficient Funds | Balance Check | `availableBalance < netAmount` | N/A | N/A | **HTTP 400 Bad Request** (`ERR_INSUFFICIENT_FUNDS`) |

---

## 5. Inter-Service Communication Contracts (OpenFeign & Resilience4j)

All inter-service invocations utilize declarative **Spring Cloud OpenFeign** clients located under the base package `com.infy.feign`. Services do not hardcode URLs; routes are dynamically resolved via Spring Cloud Consul Discovery (`name = "<service-id>"`).

### 5.1 OpenFeign Declarative Client Interfaces

#### 5.1.1 Account Service Feign Client (`com.infy.feign.account.AccountFeignClient`)
```java
package com.infy.feign.account;

import com.infy.common.dto.ApiResponse;
import com.infy.feign.account.dto.AccountBalanceResponseDTO;
import com.infy.feign.account.dto.AccountDebitRequestDTO;
import com.infy.feign.account.dto.AccountCreditRequestDTO;
import com.infy.feign.account.fallback.AccountFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@FeignClient(
    name = "account-service",
    fallbackFactory = AccountFeignClientFallbackFactory.class
)
public interface AccountFeignClient {

    @GetMapping("/api/v1/accounts/{accountNumber}/balance")
    ApiResponse<AccountBalanceResponseDTO> getAccountBalance(
        @PathVariable("accountNumber") String accountNumber
    );

    @PostMapping("/api/v1/accounts/debit")
    ApiResponse<AccountBalanceResponseDTO> executeDebit(
        @Valid @RequestBody AccountDebitRequestDTO debitRequest
    );

    @PostMapping("/api/v1/accounts/credit")
    ApiResponse<AccountBalanceResponseDTO> executeCredit(
        @Valid @RequestBody AccountCreditRequestDTO creditRequest
    );

    @GetMapping("/api/v1/accounts/{accountNumber}/status")
    ApiResponse<String> getAccountStatus(
        @PathVariable("accountNumber") String accountNumber
    );
}
```

#### 5.1.2 Customer Service Feign Client (`com.infy.feign.customer.CustomerFeignClient`)
```java
package com.infy.feign.customer;

import com.infy.common.dto.ApiResponse;
import com.infy.feign.customer.dto.CustomerProfileDTO;
import com.infy.feign.customer.fallback.CustomerFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "customer-service",
    fallbackFactory = CustomerFeignClientFallbackFactory.class
)
public interface CustomerFeignClient {

    @GetMapping("/api/v1/customers/{customerId}")
    ApiResponse<CustomerProfileDTO> getCustomerProfile(
        @PathVariable("customerId") Long customerId
    );

    @GetMapping("/api/v1/customers/{customerId}/kyc-status")
    ApiResponse<String> getCustomerKycStatus(
        @PathVariable("customerId") Long customerId
    );
}
```

#### 5.1.3 Audit & Notification Feign Client (`com.infy.feign.audit.AuditNotificationFeignClient`)
```java
package com.infy.feign.audit;

import com.infy.common.dto.ApiResponse;
import com.infy.feign.audit.dto.AuditEventDTO;
import com.infy.feign.audit.dto.NotificationDTO;
import com.infy.feign.audit.fallback.AuditNotificationFeignClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "audit-notification-service",
    fallbackFactory = AuditNotificationFeignClientFallbackFactory.class
)
public interface AuditNotificationFeignClient {

    @PostMapping("/api/v1/audit/events")
    ApiResponse<Void> recordAuditEvent(@RequestBody AuditEventDTO auditEvent);

    @PostMapping("/api/v1/notifications/send")
    ApiResponse<Void> dispatchNotification(@RequestBody NotificationDTO notification);
}
```

---

### 5.2 Resilience4j Circuit Breaker & Fallback Architecture

Each Feign client is protected by Resilience4j. The circuit breaker transitions through three deterministic states:

```
                  +-----------------------------------+
                  |                                   |
                  v                                   |
           +--------------+   Failure Rate >= 50%    +--------------+
           |              +------------------------->+              |
           |    CLOSED    |                          |     OPEN     |
           |              +<-------------------------+              |
           +-------+------+   Wait Duration Passed   +-------+------+
                   ^          (waitDuration = 10s)           |
                   |                                         |
                   |          Success Rate >= 50%            v
                   +----------------------------------+--------------+
                                                      |  HALF_OPEN   |
                                                      +--------------+
```

1. **CLOSED:** All network requests are routed to downstream services. Call metrics (success/failure) are tracked in a sliding window.
2. **OPEN:** When the failure rate equals or exceeds the threshold (50%), the circuit transitions to OPEN. Downstream invocations are short-circuited instantly without network delay, and fallback logic executes.
3. **HALF_OPEN:** After the configured `waitDurationInOpenState` (10,000ms), the circuit enters HALF-OPEN. It permits a limited trial number of calls (`permittedNumberOfCallsInHalfOpenState = 3`). If these succeed, the circuit returns to CLOSED; if any fail, it trips back to OPEN.

#### 5.2.1 Resilience4j Configuration (`application.yml`)
```yaml
resilience4j:
  circuitbreaker:
    instances:
      accountService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50.0
        slowCallRateThreshold: 70.0
        slowCallDurationThreshold: 2000ms
        waitDurationInOpenState: 10000ms
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        recordExceptions:
          - feign.FeignException
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignoreExceptions:
          - com.infy.common.exception.InfyBankException
      customerService:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        failureRateThreshold: 50.0
        waitDurationInOpenState: 8000ms
        permittedNumberOfCallsInHalfOpenState: 3
  retry:
    instances:
      accountService:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
  timelimiter:
    instances:
      accountService:
        timeoutDuration: 3000ms
```

#### 5.2.2 Fallback Factory Implementation Example
```java
package com.infy.feign.account.fallback;

import com.infy.common.dto.ApiResponse;
import com.infy.common.exception.InfyBankException;
import com.infy.feign.account.AccountFeignClient;
import com.infy.feign.account.dto.AccountBalanceResponseDTO;
import com.infy.feign.account.dto.AccountDebitRequestDTO;
import com.infy.feign.account.dto.AccountCreditRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountFeignClientFallbackFactory implements FallbackFactory<AccountFeignClient> {

    private static final Logger log = LoggerFactory.getLogger(AccountFeignClientFallbackFactory.class);

    @Override
    public AccountFeignClient create(Throwable cause) {
        return new AccountFeignClient() {
            @Override
            public ApiResponse<AccountBalanceResponseDTO> getAccountBalance(String accountNumber) {
                log.error("Account-service balance inquiry failed for account: {}. Cause: {}", accountNumber, cause.getMessage());
                throw new InfyBankException(
                    "ERR_DOWNSTREAM_UNAVAILABLE",
                    "Account service is currently unavailable. Please try again later."
                );
            }

            @Override
            public ApiResponse<AccountBalanceResponseDTO> executeDebit(AccountDebitRequestDTO debitRequest) {
                log.error("Account-service debit failed for account: {}. Circuit OPEN/Degraded. Cause: {}",
                    debitRequest.getAccountNumber(), cause.getMessage());
                throw new InfyBankException(
                    "ERR_DEBIT_SERVICE_UNAVAILABLE",
                    "Unable to debit funds due to temporary system unavailability. Transfer aborted safely."
                );
            }

            @Override
            public ApiResponse<AccountBalanceResponseDTO> executeCredit(AccountCreditRequestDTO creditRequest) {
                log.error("Account-service credit failed for account: {}. Cause: {}",
                    creditRequest.getAccountNumber(), cause.getMessage());
                throw new InfyBankException(
                    "ERR_CREDIT_SERVICE_UNAVAILABLE",
                    "Unable to credit funds due to temporary system unavailability. Compensating action triggered."
                );
            }

            @Override
            public ApiResponse<String> getAccountStatus(String accountNumber) {
                return ApiResponse.success("UNKNOWN_DEGRADED");
            }
        };
    }
}
```

---

## 6. Uniform REST Envelope & Error Handling Specification

### 6.1 Common REST Success Envelope
All successful API endpoints return a standardized JSON structure wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {
    "transactionId": "TXN-894729104",
    "grossAmount": 600.00,
    "discountApplied": 30.00,
    "netAmountDebited": 570.00,
    "status": "COMPLETED"
  },
  "timestamp": "2026-10-07T18:30:00.000Z"
}
```

### 6.2 Common ErrorInfo Structure (Mandatory)
All rejected requests, validation errors, and runtime exceptions return the standardized `ErrorInfo` envelope:

```json
{
  "errorCode": "ERR_INVALID_TRANSACTION_AMOUNT",
  "errorMessage": "Transaction amount must be strictly greater than zero.",
  "timestamp": "2026-10-07T18:30:00.000Z"
}
```

### 6.3 Standard HTTP Status Code Mappings

| HTTP Status Code | Standard Usage in Infy_Bank360 | Example Platform Scenarios |
| :--- | :--- | :--- |
| **`200 OK`** | Successful read, update, or calculation queries. | Balance inquiry, statement fetch, EMI calculation result. |
| **`201 CREATED`** | Successful provisioning of new business entities. | Customer registered, account opened, fund transfer executed, loan applied. |
| **`400 BAD REQUEST`** | Validation violation, business rule rejections, arithmetic failures. | `amount <= 0`, discount threshold failures, insufficient balance, invalid account number format. |
| **`403 FORBIDDEN`** | Authenticated user lacks RBAC authority or entity lifecycle prohibits action. | Debit attempt against a `FROZEN` account, customer accessing another customer's CIF details. |
| **`404 NOT FOUND`** | Requested resource does not exist in the database. | Account number not found, customer profile not found, ticket ID not found. |
| **`409 CONFLICT`** | Unique constraint or state conflict violation. | Duplicate email registration, duplicate national ID, concurrent entity update conflict. |
| **`500 INTERNAL SERVER ERROR`** | Unexpected runtime errors, unhandled database driver failure. | Database connection pool exhaustion, unhandled `NullPointerException`. |
| **`503 SERVICE UNAVAILABLE`** | Circuit breaker open, downstream microservice timeout or offline. | Resilience4j short-circuit on `account-service` or `customer-service`. |

---

### 6.4 Core Exception Classes & Global Controller Advice

#### 6.4.1 Custom Base Exception: `InfyBankException`
```java
package com.infy.common.exception;

public class InfyBankException extends RuntimeException {
    private final String errorCode;

    public InfyBankException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
    }

    public InfyBankException(String errorMessage) {
        super(errorMessage);
        this.errorCode = "ERR_BUSINESS_RULE_VIOLATION";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

#### 6.4.2 Standard `ErrorInfo` DTO
```java
package com.infy.common.dto;

import java.time.Instant;

public record ErrorInfo(
    String errorCode,
    String errorMessage,
    Instant timestamp
) {
    public ErrorInfo(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, Instant.now());
    }
}
```

#### 6.4.3 Centralized `@RestControllerAdvice`
```java
package com.infy.common.exception;

import com.infy.common.dto.ErrorInfo;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InfyBankException.class)
    public ResponseEntity<ErrorInfo> handleInfyBankException(InfyBankException ex) {
        log.warn("Business rule violation: [{}] {}", ex.getErrorCode(), ex.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleValidationException(MethodArgumentNotValidException ex) {
        String combinedErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));

        log.warn("Input validation failed: {}", combinedErrors);
        ErrorInfo errorInfo = new ErrorInfo("ERR_VALIDATION_FAILED", combinedErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorInfo);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorInfo> handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.error("Circuit breaker is OPEN: {}", ex.getMessage());
        ErrorInfo errorInfo = new ErrorInfo(
            "ERR_CIRCUIT_BREAKER_OPEN",
            "Downstream service is temporarily unavailable. Please retry shortly."
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorInfo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> handleGenericException(Exception ex) {
        log.error("Unhandled platform exception occurred", ex);
        ErrorInfo errorInfo = new ErrorInfo(
            "ERR_INTERNAL_SERVER_ERROR",
            "An unexpected internal error occurred. Please contact customer support."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorInfo);
    }
}
```

---

## 7. Concrete Relational Database Schema Design (MySQL 8.0)

```sql
-- =========================================================================
-- 1. AUTH SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_auth_db;
USE infy_auth_db;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- =========================================================================
-- 2. CUSTOMER SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_customer_db;
USE infy_customer_db;

CREATE TABLE IF NOT EXISTS customer_profiles (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    national_id VARCHAR(30) NOT NULL UNIQUE,
    kyc_status VARCHAR(40) NOT NULL DEFAULT 'PENDING_KYC',
    rejection_reason VARCHAR(255),
    id_proof_url VARCHAR(500),
    address_proof_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cust_national_id (national_id),
    INDEX idx_cust_kyc_status (kyc_status)
) ENGINE=InnoDB;

-- =========================================================================
-- 3. ACCOUNT SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_account_db;
USE infy_account_db;

CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(12) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL, -- SAVINGS, CHECKING
    available_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    book_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, FROZEN, CLOSED
    version INT NOT NULL DEFAULT 0, -- Optimistic locking
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_acct_number (account_number),
    INDEX idx_acct_customer (customer_id),
    INDEX idx_acct_status (status)
) ENGINE=InnoDB;

-- =========================================================================
-- 4. TRANSACTION SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_transaction_db;
USE infy_transaction_db;

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference VARCHAR(50) NOT NULL UNIQUE,
    source_account_number VARCHAR(12) NOT NULL,
    destination_account_number VARCHAR(12),
    transaction_type VARCHAR(30) NOT NULL, -- TRANSFER, UTILITY_BILL, DEPOSIT
    gross_amount DECIMAL(15, 2) NOT NULL,
    discount_applied DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    net_amount_debited DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, COMPLIANCE_FLAGGED
    biller_category VARCHAR(30),
    bill_reference_number VARCHAR(50),
    compliance_surveillance BOOLEAN DEFAULT FALSE,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_txn_ref (transaction_reference),
    INDEX idx_txn_src_acct (source_account_number),
    INDEX idx_txn_created_at (created_at),
    INDEX idx_txn_compliance (compliance_surveillance)
) ENGINE=InnoDB;

-- =========================================================================
-- 5. LOAN SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_loan_db;
USE infy_loan_db;

CREATE TABLE IF NOT EXISTS loan_applications (
    loan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    loan_type VARCHAR(30) NOT NULL, -- PERSONAL, HOME, VEHICLE
    principal_amount DECIMAL(15, 2) NOT NULL,
    tenure_months INT NOT NULL,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    monthly_emi DECIMAL(15, 2) NOT NULL,
    declared_monthly_income DECIMAL(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED', -- APPLIED, APPROVED, REJECTED, DISBURSED
    officer_remarks VARCHAR(500),
    disbursed_account_number VARCHAR(12),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_loan_ref (application_reference),
    INDEX idx_loan_customer (customer_id)
) ENGINE=InnoDB;

-- =========================================================================
-- 6. SERVICE REQUEST SERVICE SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_service_request_db;
USE infy_service_request_db;

CREATE TABLE IF NOT EXISTS service_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_id VARCHAR(30) NOT NULL UNIQUE, -- e.g., TCK-10001
    customer_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assigned_employee_id BIGINT,
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_req_tracking (tracking_id),
    INDEX idx_req_customer (customer_id),
    INDEX idx_req_status (status)
) ENGINE=InnoDB;

-- =========================================================================
-- 7. AUDIT & NOTIFICATION SCHEMA
-- =========================================================================
CREATE DATABASE IF NOT EXISTS infy_audit_db;
USE infy_audit_db;

CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_notif_customer (customer_id)
) ENGINE=InnoDB;
```

---

## 8. Angular 17 & Bootstrap 5 Frontend Specifications

### 8.1 UI Architecture
- **Single Page Application (SPA):** Angular 17 with standalone components, lazy-loaded routing, and strict TypeScript types.
- **Styling Framework:** Bootstrap 5.3 with custom modern CSS variables (dark modern banking slate `#0f172a`, primary accent `#2563eb`, emerald success `#10b981`, ruby danger `#ef4444`).
- **State Management & Guards:**
  - `AuthInterceptor`: Automatically attaches JWT `Authorization: Bearer <token>` to outbound HTTP calls.
  - `AuthGuard` & `RoleGuard`: Blocks unauthorized navigation to employee/compliance routes.
  - `HttpErrorInterceptor`: Parses the standard `ErrorInfo` (`errorCode`, `errorMessage`) and triggers non-blocking Bootstrap toast notifications.

### 8.2 Client-Side Validation Rules
Frontend forms mirror the backend JSR-380 validation rules:
1. **Transfer Amount Field:**
   - Real-time client-side calculation: If value $> 500$, UI dynamically renders an alert badge: `"Qualifies for 5% Promotional Discount! You save: $" + (amount * 0.05).toFixed(2)`.
   - If value $\le 0$ or negative, submit button is disabled and input highlights red: `"Amount must be strictly greater than zero."`
2. **Account Number Inputs:** Regex enforced `^[0-9]{12}$` with length counter.
3. **Loan EMI Simulator:** Interactive slider dynamically renders monthly EMI curve and visual debt-to-income pie chart.

---

## 9. Maven Multi-Module Project Structure

```
infy-bank360/
├── pom.xml                               # Root Aggregator POM (Java 21, Spring Boot 3.3.x, Spring Cloud 2023.x)
├── infy-common/                          # Shared library (com.infy.common)
│   ├── src/main/java/com/infy/common/
│   │   ├── dto/                          # ApiResponse, ErrorInfo
│   │   └── exception/                    # InfyBankException, GlobalExceptionHandler
│   └── pom.xml
├── infy-feign-clients/                   # Declarative Feign contracts (com.infy.feign)
│   ├── src/main/java/com/infy/feign/
│   │   ├── account/                      # AccountFeignClient, DTOs, Fallbacks
│   │   ├── customer/                     # CustomerFeignClient, DTOs, Fallbacks
│   │   └── audit/                        # AuditNotificationFeignClient, DTOs, Fallbacks
│   └── pom.xml
├── api-gateway/                          # Port 8080 (Spring Cloud Gateway, JWT Filter, Consul Discovery)
├── auth-service/                         # Port 8081 (Spring Security 6, JWT, BCrypt)
├── customer-service/                     # Port 8082 (Customer & KYC Management)
├── account-service/                      # Port 8083 (Account Lifecycle & Balance Engine)
├── transaction-service/                  # Port 8084 (Transfers, Bill Payments, Discount Engine)
├── loan-service/                         # Port 8085 (Loan Catalog, EMI Engine & Underwriting)
├── service-request-service/              # Port 8086 (Support Tickets & Helpdesk)
└── audit-notification-service/           # Port 8087 (AML Compliance & Audit Trail)
```

---
*Technical Specification Complete — Approved for Greenfield Sprint Implementation (Infosys FA3).*
