# Project Work Breakdown & Implementation Schedule (task.md)
## Project: Infy_Bank360 — Greenfield Digital Banking Platform

---

| Sprint Metadata | Details |
| :--- | :--- |
| **Document Name** | Work Breakdown Schedule (`task.md`) |
| **System** | Infy_Bank360 |
| **Author / Role** | Technical Project Manager (TPM) |
| **Sprint Duration** | 2-Day Implementation Sprint (16 Active Working Hours) |
| **Evaluation Track** | Infosys DSE Stream (FA3 Capstone Greenfield Evaluation) |
| **Team Size** | 4 Full-Stack Software Engineers |
| **Base Package** | `com.infy` |

---

## 1. Team Allocation & 2-Day Sprint Roadmap

### 1.1 Team Roles & Ownership Matrix
- **Dev 1 (Backend Lead & Infra):** Project scaffolding, Consul configuration, Spring Cloud Gateway, `transaction-service`, and promotional discount engine.
- **Dev 2 (Backend Specialist — Core Banking):** `customer-service` (KYC), `account-service` (Lifecycle, balance, debit/credit), and optimistic locking.
- **Dev 3 (Backend Specialist — Loans & Operations):** `loan-service` (EMI engine, underwriting), `service-request-service` (Tickets), and audit feeds.
- **Dev 4 (Frontend Specialist — Full Stack):** Angular 17 workspace, Bootstrap 5 UI, Reactive Forms, Gateway routing integration, and End-to-End wireup.

### 1.2 2-Day Hourly Sprint Schedule

```
+---------------------------------------------------------------------------------------------------+
| DAY 1: INFRASTRUCTURE, PERSISTENCE & CORE DOMAIN SERVICES                                         |
+-------------------+-------------------------------------------------------------------------------+
| Hours 00:00-04:00 | Phase 1: Database Setup & Seed Data | Phase 2: Maven Multi-Module, Consul, Gateway|
| Hours 04:00-08:00 | Phase 3: Entities, DTOs & Repositories | Phase 4 (Part 1): Services & Validation  |
+-------------------+-------------------------------------------------------------------------------+
| DAY 2: INTER-SERVICE WIRING, CONTROLLERS, ANGULAR UI & EVALUATION                                 |
+-------------------+-------------------------------------------------------------------------------+
| Hours 08:00-12:00 | Phase 4 (Part 2): Feign & Resilience4j | Phase 5: REST APIs & Global Exception   |
| Hours 12:00-14:00 | Phase 6: Angular 17 + Bootstrap 5 UI Components & Reactive Forms              |
| Hours 14:00-16:00 | Phase 7: Integration Testing, Actuator Health Checks & FA3 Demo Certification |
+-------------------+-------------------------------------------------------------------------------+
```

---

## Phase 1: Database Setup & Schema Creation

Focus: Initializing relational databases in MySQL 8.0, executing schema DDL scripts, establishing foreign key constraints, and loading mock seed data for the 2-day sprint.

- [x] **TASK-1.1: Generate Master Database Creation Script (`Tablescripts.sql`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** None
  - **Completion Criteria:** A single, idempotent SQL script (`Tablescripts.sql`) successfully creates schemas and tables: `customers`, `accounts`, `transactions`, `loan_applications`, `service_requests`, and `audit_logs` with proper data types (`DECIMAL(15,2)` for monetary values), primary keys, and unique indexes (`email`, `national_id`, `account_number`, `tracking_id`).

- [x] **TASK-1.2: Prepare Seed Data Insertion Script (`SeedData.sql`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-1.1
  - **Completion Criteria:** Insert scripts generate at least 4 test customers (1 `PENDING_KYC`, 2 `KYC_VERIFIED`, 1 `KYC_REJECTED`), 3 initial accounts (Savings and Checking with opening balances of $1,000.00 and $2,500.00), 2 sample loan applications, and 2 service tickets.

- [x] **TASK-1.3: Configure HikariCP Database Connection Pools**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-1.1
  - **Completion Criteria:** JPA datasource properties configured in `application.yml` files with `maximum-pool-size: 10`, `minimum-idle: 5`, and dialect `org.hibernate.dialect.MySQLDialect`. Schema creation tested locally with clean connection acquirement.

---

## Phase 2: Maven Infrastructure, Spring Cloud Consul Config, & API Gateway Setup

Focus: Establishing multi-module Maven structure, starting HashiCorp Consul for service discovery/centralized config, and configuring Spring Cloud Gateway route locators.

- [x] **TASK-2.1: Initialize Root Maven Multi-Module Project (`pom.xml`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** None
  - **Completion Criteria:** Root `pom.xml` builds cleanly with Java 21, Spring Boot `3.3.x`, and Spring Cloud `2023.x` dependency management. Submodules declared: `infy-common`, `api-gateway`, `auth-service`, `customer-service`, `account-service`, `transaction-service`, `loan-service`, `service-request-service`, `audit-notification-service`.

- [x] **TASK-2.2: Implement Shared Core Library (`infy-common`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-2.1
  - **Completion Criteria:** Package `com.infy.bank360.common` compiled with `ErrorInfo` record (`errorCode`, `errorMessage`, `timestamp`), `ApiResponse<T>` wrapper, and base custom exception `InfyBankException`. Shared as a local Maven dependency to all submodules.

- [x] **TASK-2.3: Setup & Verify HashiCorp Consul Service Registry & KV Store**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-2.1
  - **Completion Criteria:** Local Consul agent runs on port `8500`. Microservices register with `spring.cloud.consul.discovery.enabled=true` and report heartbeat health via `/actuator/health`. KV configuration verified at `/config/application/data`.

- [x] **TASK-2.4: Configure Spring Cloud Gateway Edge Router (`api-gateway`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-2.2, TASK-2.3
  - **Completion Criteria:** Gateway runs on port `8080`. Routes configured for all 8 microservices using dynamic `lb://` URI locator. Global CORS configured to allow `http://localhost:4200` with headers and credentials.

---

## Phase 3: Domain Model, DTOs (`com.infy.dto`), and Repository Layer (`com.infy.repository`)

Focus: Building JPA persistence models, mapping DTOs with JSR-380 bean validations, and generating Spring Data JPA repository interfaces.

- [x] **TASK-3.1: Implement JPA Entities in `com.infy.entity`**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 2 & Dev 3
  - **Dependencies:** TASK-1.1, TASK-2.1
  - **Completion Criteria:** Minimal entity classes created and unit-tested:
    - Dev 2: `Customer` (`customerId`, `email`, `nationalId`, `kycStatus`) and `Account` (`accountNumber`, `balance`, `status`, `accountType`).
    - Dev 3: `TransactionRecord` (`grossAmount`, `discount`, `netAmount`, `transactionType`), `LoanApplication` (`monthlyEmi`, `annualInterestRate`), and `ServiceRequest` (`trackingId`, `category`).

- [x] **TASK-3.2: Implement DTOs with JSR-380 Bean Validation Annotations (`com.infy.dto`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 2 & Dev 3
  - **Dependencies:** TASK-3.1
  - **Completion Criteria:** DTO classes created with strict validation:
    - `CustomerDTO`: `@NotBlank`, `@Email`, `@Pattern(regexp = "^[6-9][0-9]{9}$")`.
    - `AccountDTO`: `@Pattern(regexp = "^[0-9]{12}$")`, `@DecimalMin("100.00")`.
    - `TransactionDTO`: `@Positive` for amount, `@NotBlank` for account numbers.
    - `LoanApplicationDTO`: `@Min(12)`, `@Max(120)` tenure, `@DecimalMin("1000.00")`.
    - `ServiceRequestDTO`: `@NotBlank`, `@Size(min = 10, max = 1000)`.

- [x] **TASK-3.3: Implement Spring Data JPA Repositories in `com.infy.repository`**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2 & Dev 3
  - **Dependencies:** TASK-3.1
  - **Completion Criteria:** Repository interfaces created with custom query methods:
    - `CustomerRepository`: `findByEmail(String email)`, `findByNationalId(String nationalId)`.
    - `AccountRepository`: `findByCustomerId(Long customerId)`, `existsByAccountNumber(String acc)`.
    - `TransactionRecordRepository`: `findBySourceAccountNumber(String acc)`.
    - `LoanApplicationRepository`: `findByCustomerId(Long customerId)`.
    - `ServiceRequestRepository`: `findByTrackingId(String trackingId)`.

---

## Phase 4: Business Services, Constructor Injection, Validation, OpenFeign & Resilience4j

Focus: Implementing service business logic, mandatory constructor injection, program rules for discount (> 500) and negative amount rejection, OpenFeign clients, and Resilience4j circuit breakers.

- [x] **TASK-4.1: Implement Customer & KYC Management Service (`CustomerServiceImpl`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-3.2, TASK-3.3
  - **Completion Criteria:** Service uses 100% constructor injection. Methods support registration, document upload status update, employee verification (`KYC_VERIFIED`), and rejection with mandatory reason (`KYC_REJECTED`).

- [x] **TASK-4.2: Implement Account Lifecycle & Balance Service (`AccountServiceImpl`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-3.2, TASK-3.3
  - **Completion Criteria:** Enforces KYC verification check prior to opening accounts. Generates unique 12-digit account numbers. Implements atomic `debit` and `credit` methods with optimistic locking (`@Version`) and status check (blocks debits on `FROZEN` accounts with `InfyBankException`).

- [x] **TASK-4.3: Implement Transaction & Promotional Discount Engine (`TransactionServiceImpl`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-3.2, TASK-3.3
  - **Completion Criteria:**
    - Strictly validates: if `amount <= 0.00`, throws `InfyBankException("Transaction amount must be strictly greater than zero.")`.
    - Enforces promotional discount rule: If `amount > 500.00`, applies 5% discount (`netAmount = amount * 0.95`). If `amount <= 500.00`, discount is `$0.00`.
    - Coordinates inter-service debit and credit via `AccountFeignClient`.

- [x] **TASK-4.4: Implement Loan Origination & EMI Engine (`LoanServiceImpl`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 3
  - **Dependencies:** TASK-3.2, TASK-3.3
  - **Completion Criteria:** Implements exact formula $\text{EMI} = \frac{P \times r \times (1+r)^n}{(1+r)^n - 1}$. Validates DTI rule (EMI must not exceed 50% of monthly income). Disburses approved funds by invoking `AccountFeignClient.executeCredit`.

- [x] **TASK-4.5: Implement Service Request Helpdesk Engine (`ServiceRequestServiceImpl`)**
  - **Priority:** P2 (Medium)
  - **Owner:** Dev 3
  - **Dependencies:** TASK-3.2, TASK-3.3
  - **Completion Criteria:** Generates unique tracking ID `TCK-XXXXX`. Implements lifecycle transitions (`OPEN` $\to$ `IN_PROGRESS` $\to$ `RESOLVED` $\to$ `CLOSED`).

- [x] **TASK-4.6: Create Declarative OpenFeign Clients in `com.infy.feign`**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-2.2
  - **Completion Criteria:** Interfaces created under package `com.infy.feign`:
    - `AccountFeignClient` for balance inquiry, credit, and debit operations.
    - `CustomerFeignClient` for KYC verification checks.

- [x] **TASK-4.7: Configure Resilience4j Circuit Breakers & Fallback Factories**
  - **Priority:** P1 (High)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-4.6
  - **Completion Criteria:** Resilience4j configured in `application.yml` (`slidingWindowSize=10`, `failureRateThreshold=50.0`, `waitDurationInOpenState=10000ms`). Fallback factories implemented to return controlled `ErrorInfo` / degrade gracefully when downstream services are offline.

---

## Phase 5: REST Controllers (`/infybank/` Mappings) & Global Exception Handling

Focus: Exposing uniform REST APIs prefixed with `/infybank/`, hooking JSR-380 request validation, and creating centralized `@RestControllerAdvice` error responses.

- [x] **TASK-5.1: Implement Centralized `GlobalExceptionHandler` (`com.infy.exception`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-2.2
  - **Completion Criteria:** Catches `InfyBankException` and returns HTTP 400 Bad Request with standardized `ErrorInfo(errorCode, errorMessage, timestamp)`. Catches `MethodArgumentNotValidException` and bundles field errors into `ErrorInfo`. Catches `CallNotPermittedException` returning HTTP 503.

- [x] **TASK-5.2: Implement `CustomerAPI` Controller (`com.infy.api.CustomerAPI`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-4.1, TASK-5.1
  - **Completion Criteria:** Controller mapped to `@RequestMapping("/infybank/customers")`. Uses 100% constructor injection. Endpoints implemented: `POST /`, `GET /{customerId}`, `PUT /{customerId}/kyc` returning `ResponseEntity<CustomerDTO>`.

- [x] **TASK-5.3: Implement `AccountAPI` Controller (`com.infy.api.AccountAPI`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 2
  - **Dependencies:** TASK-4.2, TASK-5.1
  - **Completion Criteria:** Controller mapped to `@RequestMapping("/infybank/accounts")`. Constructor injection enforced. Endpoints implemented: `POST /` (201 Created), `GET /{accountNumber}`, `GET /customer/{customerId}`, `PUT /{accountNumber}/status`.

- [x] **TASK-5.4: Implement `TransactionAPI` Controller (`com.infy.api.TransactionAPI`)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-4.3, TASK-5.1
  - **Completion Criteria:** Controller mapped to `@RequestMapping("/infybank/transactions")`. Endpoints: `POST /transfer`, `POST /bill-pay`, `GET /{accountNumber}`. Validated with `@Valid`. Verified that negative or zero amount payloads trigger HTTP 400 with `ErrorInfo`.

- [x] **TASK-5.5: Implement `LoanAPI` Controller (`com.infy.api.LoanAPI`)**
  - **Priority:** P1 (High)
  - **Owner:** Dev 3
  - **Dependencies:** TASK-4.4, TASK-5.1
  - **Completion Criteria:** Controller mapped to `@RequestMapping("/infybank/loans")`. Endpoints: `POST /calculate-emi`, `POST /apply`, `GET /{loanId}`, `PUT /{loanId}/status`, `POST /{loanId}/disburse`.

- [x] **TASK-5.6: Implement `ServiceRequestAPI` Controller (`com.infy.api.ServiceRequestAPI`)**
  - **Priority:** P2 (Medium)
  - **Owner:** Dev 3
  - **Dependencies:** TASK-4.5, TASK-5.1
  - **Completion Criteria:** Controller mapped to `@RequestMapping("/infybank/requests")`. Endpoints: `POST /`, `GET /{trackingId}`, `GET /customer/{customerId}`, `PUT /{serviceRequestId}/status`.

---

## Phase 6: Angular + Bootstrap Frontend (UI Components, Services & Gateway Integration)

Focus: Building an interactive, responsive Angular 17 SPA with Bootstrap 5.3, communicating with backend microservices via the API Gateway on port `8080`.

- [x] **TASK-6.1: Initialize Angular 17 Project & Bootstrap 5.3 Styling Setup**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 4
  - **Dependencies:** None
  - **Completion Criteria:** Angular 17 workspace initialized with Standalone Components and Bootstrap 5.3 + Bootstrap Icons. Custom banking stylesheet created (`styles.css` with dark slate `#0f172a`, primary `#2563eb`, and emerald `#10b981`).

- [x] **TASK-6.2: Implement Core Services & HTTP Error Interceptor**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.1
  - **Completion Criteria:** Angular `BankingApiService` created pointing to `http://localhost:8080/infybank`. `HttpErrorInterceptor` catches backend `ErrorInfo` payloads and displays Bootstrap toast alerts with `errorCode` and `errorMessage`.

- [x] **TASK-6.3: Build Customer Account & Balance Dashboard Component**
  - **Priority:** P1 (High)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.2, TASK-5.3
  - **Completion Criteria:** Displays account balance cards (Savings/Checking), real-time account status badge (`ACTIVE` / `FROZEN`), and mini-statement transaction table.

- [x] **TASK-6.4: Build Fund Transfer & Bill Payment Component with Dynamic Discount Preview**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.2, TASK-5.4
  - **Completion Criteria:** Reactive Form with account selector and amount input.
    - If `amount > 500`: UI displays green promo banner: *"5% Discount Applied! Net Amount: $" + (amount * 0.95)*.
    - If `amount <= 0`: UI displays red validation message and disables submit.
    - Successful transfer renders printable transaction receipt reference.

- [x] **TASK-6.5: Build Loan Simulator & Application Component**
  - **Priority:** P1 (High)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.2, TASK-5.5
  - **Completion Criteria:** Interactive sliders for Principal ($1,000–$50,000) and Tenure (12–120 months) dynamically compute monthly EMI. Application form submits data and renders real-time status (`APPLIED` / `APPROVED`).

- [x] **TASK-6.6: Build Customer Service Request / Dispute Component**
  - **Priority:** P2 (Medium)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.2, TASK-5.6
  - **Completion Criteria:** Form allows raising tickets with category dropdown. Real-time ticket tracking by `trackingId` (`TCK-XXXXX`) showing resolution comments.

- [x] **TASK-6.7: Build Employee Operations & KYC Verification Queue Component**
  - **Priority:** P1 (High)
  - **Owner:** Dev 4
  - **Dependencies:** TASK-6.2, TASK-5.2, TASK-5.5
  - **Completion Criteria:** Operational view for bank officers to approve/reject pending KYC applications and review pending loan applications for disbursement.

---

## Phase 7: Integration Testing, Actuator Health Checks, & Final Evaluation Verification

Focus: End-to-end testing across microservices, validating Spring Boot Actuator metrics, and ensuring 100% compliance with FA3 Capstone evaluation requirements.

- [x] **TASK-7.1: Unit & Business Logic Test Suite (BR-01 to BR-07 Verification)**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1 & Dev 2
  - **Dependencies:** Phase 4 & Phase 5
  - **Completion Criteria:** JUnit 5 + Mockito test suite executes:
    - Verifies `amount <= 0` throws `InfyBankException`.
    - Verifies `amount = 500.00` receives $0 discount; `amount = 500.01` receives 5% discount.
    - Verifies EMI formula calculation accuracy.
    - Test coverage exceeds 80% on core service modules.

- [x] **TASK-7.2: Inter-Service Feign & Resilience4j Circuit Breaker Test**
  - **Priority:** P1 (High)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-4.7
  - **Completion Criteria:** Integration test simulates `account-service` stoppage; verifies that `transaction-service` transitions circuit to `OPEN`, invokes fallback, and returns HTTP 503 `ERR_CIRCUIT_BREAKER_OPEN` rather than cascading crash.

- [x] **TASK-7.3: Actuator Health & Consul Discovery Verification**
  - **Priority:** P1 (High)
  - **Owner:** Dev 3
  - **Dependencies:** Phase 2
  - **Completion Criteria:** Invocations to `http://localhost:8500/ui` show all services healthy (GREEN). Each service exposes `/actuator/health` reporting status `UP` with database and disk space details.

- [x] **TASK-7.4: Full End-to-End User Story Walkthrough (US-01 through US-19)**
  - **Priority:** P0 (Blocker)
  - **Owner:** All Team Members (Dev 1, 2, 3, 4)
  - **Dependencies:** Phase 5 & Phase 6
  - **Completion Criteria:** Successful live dry run demonstrating:
    1. Customer Registration & KYC Document upload $\to$ Verification approval.
    2. Account Creation with $100+ deposit.
    3. Fund Transfer with `amount > 500` confirming 5% discount deduction.
    4. Negative amount transfer attempt showing instant rejection (`ErrorInfo`).
    5. Loan EMI calculation, application submission, and fund disbursement.
    6. Raising and closing a support ticket.

- [x] **TASK-7.5: Final FA3 Codebase Packaging & Clean Maven Build**
  - **Priority:** P0 (Blocker)
  - **Owner:** Dev 1
  - **Dependencies:** TASK-7.4
  - **Completion Criteria:** `mvn clean package -DskipTests=false` passes across all modules without errors. Project ready for evaluator inspection.

---

## 2. Sprint Risk Mitigation & Contingency Plan

| Risk ID | Identified Risk Scenario | Probability | Impact | Mitigation Strategy | Owner |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RSK-01** | Consul agent connection failure on developer workstation | Low | High | Fall back temporarily to static Feign URLs (`url = "http://localhost:8083"`) while debugging Consul agent daemon. | Dev 1 |
| **RSK-02** | Distributed debit-credit mismatch if destination credit fails | Medium | High | Wrap transfer in atomic compensating transaction (auto-refund source account on downstream failure). | Dev 1 |
| **RSK-03** | Frontend-Backend CORS or header blockage | Medium | High | Global CORS filter configured in `api-gateway` with allowed headers `*` and origins `http://localhost:4200`. | Dev 4 |
| **RSK-04** | JSR-380 validation bypass | Low | Medium | Strict double-validation: Controller `@Valid` annotations coupled with Service-layer manual assertions. | Dev 2 |

---
*Task Work Breakdown Complete — Ready for Sprint Kickoff.*
