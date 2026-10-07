# Software Requirements Specification (SRS)
## Project: Infy_Bank360 — Greenfield Digital Banking Platform

---

| Project Metadata | Specification Details |
| :--- | :--- |
| **Project Name** | Infy_Bank360 |
| **Evaluation Track** | Infosys DSE Stream (FA3 Capstone Greenfield Evaluation) |
| **Team Size & Timeline** | 4 Developers \| 2-Day Implementation Sprint |
| **Technology Stack** | Java 21, Spring Boot 3, Spring Security 6 (JWT), Maven, Spring Cloud, Angular, Bootstrap 5 |
| **Architecture Scope** | 8 Microservices (including API Gateway) + Unified Angular Web Portal |
| **Document Version** | 1.1.0 (Evaluation Ready) |

---

## 1. Executive Summary & Architecture Topology

### 1.1 Executive Summary
**Infy_Bank360** is a full-stack, cloud-native digital banking platform engineered from scratch to serve both retail banking customers and bank employees through a responsive, unified web application.

- **Customer Capabilities:** Account balance oversight, instant fund transfers, utility bill payments with promotional discounts, loan applications, and service desk support.
- **Employee Capabilities:** Customer KYC verification, account provisioning, loan underwriting, support ticket resolution, transaction monitoring, and compliance oversight.

### 1.2 Microservices Architecture (8 Services Including API Gateway)

The backend is composed of **8 focused microservices** built with Java 21 and Spring Boot 3, orchestrated via Maven multi-modules:

| # | Microservice Name | Primary Responsibility & Endpoints | Port |
| :- | :--- | :--- | :--- |
| **1** | **`api-gateway`** | **Spring Cloud Gateway:** Central entry point, CORS handling, JWT token relay/validation, and dynamic routing to downstream services. | `8080` |
| **2** | **`auth-service`** | **Identity & Security:** User credential management, BCrypt password hashing, Spring Security 6, JWT token generation with role claims (`ROLE_CUSTOMER`, `ROLE_EMPLOYEE`, `ROLE_COMPLIANCE`). | `8081` |
| **3** | **`customer-service`** | **Customer & KYC Management:** Customer profile details, document uploads (ID proof/address proof), employee verification queue, and KYC status updates. | `8082` |
| **4** | **`account-service`** | **Account Lifecycle & Balances:** Account generation (Savings/Checking), real-time balance inquiries, and lifecycle state changes (`ACTIVE`, `FROZEN`, `CLOSED`). | `8083` |
| **5** | **`transaction-service`** | **Payments & Transactions:** Intra-bank transfers, utility bill payments, transaction amount validation (`amount > 0`), promotional discount engine (`amount > 500`), and ledger history. | `8084` |
| **6** | **`loan-service`** | **Loan Origination:** Loan catalog, EMI calculator, application submission, employee credit review/approval, and loan disbursement requests. | `8085` |
| **7** | **`support-service`** | **Customer Service Desk:** Support ticket raising, categorization (disputes, inquiries), status triage (`OPEN`, `RESOLVED`), and employee resolution comments. | `8086` |
| **8** | **`audit-notification-service`** | **Audit & Compliance Alerts:** System audit logging, compliance flagging for high-value transactions ($> 10,000$), account freeze alerts, and in-app event notifications. | `8087` |

```
                       +------------------------------------------------+
                       |           Angular 17 + Bootstrap 5 UI          |
                       |  [Retail Customer View]  [Employee Ops Portal] |
                       +-----------------------+------------------------+
                                               | HTTP / REST (JWT Auth)
                                               v
                       +------------------------------------------------+
                       |     1. api-gateway (Spring Cloud Gateway: 8080)|
                       +-----------------------+------------------------+
                                               |
         +-----------------+-------------------+-----------------+-----------------+
         |                 |                   |                 |                 |
         v                 v                   v                 v                 v
+-----------------+ +-----------------+ +-----------------+ +-----------------+ +-----------------+
| 2. auth-service | | 3. customer-svc | | 4. account-svc  | | 5. txn-service  | | 6. loan-service |
| (JWT/Security)  | |  (Profiles/KYC) | |(Balances/State) | |(Transfers/Promo)| |(EMI/Approvals)  |
+-----------------+ +-----------------+ +-----------------+ +-----------------+ +-----------------+
         |                                                                                 |
         +-------------------------------------+-------------------------------------------+
                                               |
                               +---------------+---------------+
                               |                               |
                               v                               v
                      +-----------------+             +--------------------------+
                      | 7. support-svc  |             | 8. audit-notification-svc|
                      | (Service Desk)  |             |  (Audit, Compliance & Log)
                      +-----------------+             +--------------------------+
```

---

## 2. User Personas & Role-Based Access Control (RBAC)

1. **Retail Customer (`ROLE_CUSTOMER`):** Self-service banking customer managing personal accounts, payments, loans, and helpdesk tickets.
2. **Bank Onboarding Executive (`ROLE_EMPLOYEE`):** Operational branch officer processing customer registrations, validating KYC documents, approving loan applications, and resolving support tickets.
3. **Compliance Officer (`ROLE_COMPLIANCE`):** Supervisory officer monitoring suspicious high-value transactions, inspecting audit logs, and freezing high-risk accounts.

---

## 3. Agile User Stories (19 User Stories)

```
Distribution across 6 Modules:
- Module 1: Customer Onboarding & KYC (US-01 to US-03)
- Module 2: Account Lifecycle & Balance Management (US-04 to US-06)
- Module 3: Fund Transfers, Bill Payments & Transactions (US-07 to US-10)
- Module 4: Loan Application & Verification (US-11 to US-13)
- Module 5: Customer Service Desk / Support Requests (US-14 to US-16)
- Module 6: Audit, Compliance & Event Notifications (US-17 to US-19)
```

---

### Module 1: Customer Onboarding & KYC Management

#### US-01: Customer Registration & Credential Setup
- **Service:** `auth-service` & `customer-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a new retail customer, I want to register an online banking account with my basic personal details and password, so that I can access the Infy_Bank360 portal.*
- **Acceptance Criteria:**
  - Given valid inputs (Full Name, Email, Mobile, Password, and National ID), when submitted, the system securely hashes the password with BCrypt and creates the user profile.
  - Given an existing email or National ID, the system rejects registration with an HTTP 409 Conflict error.
  - Initial onboarding status is initialized as `PENDING_KYC`.

#### US-02: KYC Document Submission
- **Service:** `customer-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a registered customer, I want to upload my identity and address proof documents, so that the bank can verify my KYC status.*
- **Acceptance Criteria:**
  - Customer can upload proof of identity (Passport, Driving License, National ID) and address proof document.
  - File formats supported: PDF, JPEG, PNG (max file size 5MB).
  - Status updates to `SUBMITTED_FOR_VERIFICATION` and becomes visible in the employee queue.

#### US-03: KYC Verification & Approval / Rejection
- **Service:** `customer-service`
- **Actor:** Bank Onboarding Executive (`ROLE_EMPLOYEE`)
- **User Story:** *As a bank onboarding executive, I want to review pending customer KYC applications and approve or reject them with remarks, so that only authenticated customers gain banking privileges.*
- **Acceptance Criteria:**
  - Executive views a dashboard listing all applications with status `SUBMITTED_FOR_VERIFICATION`.
  - Executive can inspect uploaded document previews and applicant information.
  - When approved, profile status transitions to `KYC_VERIFIED`, enabling account creation.
  - When rejected, executive must provide a mandatory rejection comment, updating status to `KYC_REJECTED`.

---

### Module 2: Account Lifecycle & Balance Management

#### US-04: Account Provisioning & Activation
- **Service:** `account-service`
- **Actor:** Bank Employee (`ROLE_EMPLOYEE`) / Verified Customer
- **User Story:** *As a KYC-verified customer, I want to open a new Savings or Checking account, so that I can store funds and conduct banking transactions.*
- **Acceptance Criteria:**
  - Account can be provisioned only if customer status is `KYC_VERIFIED`.
  - The system auto-generates a unique 12-digit Account Number and sets opening status to `ACTIVE`.
  - Minimum opening balance rules are enforced (e.g., $100 minimum deposit).

#### US-05: Real-time Balance Inquiry & Account Statement
- **Service:** `account-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As an account holder, I want to view my current available balance and recent transaction history, so that I can monitor my financial standing.*
- **Acceptance Criteria:**
  - Displays real-time Available Balance and Book Balance with sub-300ms latency.
  - Customer can view a mini-statement of the last 10 transactions or query history by date range.
  - Access is restricted so customers can only access accounts owned by their authenticated CIF/UserId.

#### US-06: Account Freeze & Lifecycle State Governance
- **Service:** `account-service`
- **Actor:** Compliance Officer (`ROLE_COMPLIANCE`)
- **User Story:** *As a compliance officer, I want to freeze or suspend an account under suspicion of fraud, so that unauthorized outgoing fund movements are prevented.*
- **Acceptance Criteria:**
  - Officer can update account status between `ACTIVE`, `FROZEN`, and `CLOSED`.
  - When status is `FROZEN`, any debit or outgoing transfer attempt must be blocked with an HTTP 403 Forbidden error (`ERR_ACCOUNT_FROZEN`).
  - Account state changes are recorded in the audit log with the officer's ID and justification.

---

### Module 3: Fund Transfers, Bill Payments & Transactions

#### US-07: Intra-Bank & Inter-Bank Fund Transfers
- **Service:** `transaction-service` (calls `account-service`)
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As an account holder, I want to transfer money to another account, so that I can pay family or settle payments.*
- **Acceptance Criteria:**
  - Customer specifies Source Account, Destination Account, Amount, and Remarks.
  - System verifies source account status is `ACTIVE` and has sufficient balance.
  - Operation debits sender and credits receiver within an atomic `@Transactional` boundary.

#### US-08: Transaction Amount Positive Validation
- **Service:** `transaction-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`) / System
- **User Story:** *As the banking platform, I must strictly validate that all transaction amounts are strictly greater than zero, so that financial ledger corruption and negative transfers are prohibited.*
- **Acceptance Criteria:**
  - If transaction `amount <= 0` (e.g., `0.00`, `-50.00`), the request is **rejected immediately with HTTP 400 Bad Request**.
  - Error response contains clear domain message: `"Transaction amount must be strictly greater than zero."`
  - Zero database state modification occurs on validation failure.

#### US-09: Promotional Discount on Eligible Purchases (> 500 Rule)
- **Service:** `transaction-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`) / System
- **User Story:** *As a retail customer making a purchase or eligible bill payment, I want to receive a promotional discount when my spend exceeds 500, so that I save money on qualifying transactions.*
- **Acceptance Criteria:**
  - **Threshold Rule:** Promotional discount is applicable **only when `amount > 500`**.
  - **Sub-Threshold Rule:** If `amount <= 500`, **discount is exactly $0.00**; customer pays full amount.
  - **Discount Calculation:** When `amount > 500`, calculate 5% discount (`discount = amount * 0.05`).
  - **Net Debit:** Customer is debited `netAmount = amount - discount`.
  - Transaction ledger explicitly records `grossAmount`, `discountApplied`, and `netAmountDebited`.

#### US-10: Utility Bill Payments
- **Service:** `transaction-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a customer, I want to pay utility bills (Electricity, Mobile, Water, Internet) from my bank balance, so that I can settle recurring expenses conveniently.*
- **Acceptance Criteria:**
  - Customer selects utility biller, enters bill reference number and amount.
  - Amount rules (US-08) and discount rules (US-09) apply automatically.
  - Upon successful debit, generate an instant printable payment receipt with unique `TransactionReference`.

---

### Module 4: Loan Application & Verification

#### US-11: Loan Product Catalog & EMI Calculator
- **Service:** `loan-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a customer, I want to simulate monthly EMI payments for Personal, Home, or Vehicle loans, so that I can assess affordability before applying.*
- **Acceptance Criteria:**
  - Interactive calculator computes monthly EMI based on Principal, Tenure (12–120 months), and Annual Interest Rate.
  - Formula: $\text{EMI} = \frac{P \times r \times (1+r)^n}{(1+r)^n - 1}$.

#### US-12: Digital Loan Application Submission
- **Service:** `loan-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a customer, I want to submit an online loan application with declared income, so that the bank can review my credit eligibility.*
- **Acceptance Criteria:**
  - Customer inputs: Loan Type, Requested Principal, Tenure, Purpose, and Declared Monthly Income.
  - System generates a loan application record with status `APPLIED`.
  - Customer can track real-time application status on their dashboard.

#### US-13: Loan Underwriting & Approval Workflow
- **Service:** `loan-service` (calls `account-service`)
- **Actor:** Bank Loan Officer (`ROLE_EMPLOYEE`)
- **User Story:** *As a loan officer, I want to review pending loan applications and approve or reject them, and disburse funds to approved borrowers.*
- **Acceptance Criteria:**
  - Officer reviews applicant's income and proposed EMI.
  - Rule check: Estimated EMI must not exceed 50% of declared monthly income.
  - Officer can mark status as `APPROVED` or `REJECTED` with remarks.
  - Upon clicking **Disburse**, loan amount is credited directly to customer's active account via `account-service`.

---

### Module 5: Customer Service Desk / Support Requests

#### US-14: Raise Support / Service Request Ticket
- **Service:** `support-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As an account holder, I want to raise a service request ticket for transaction issues or inquiries, so that bank staff can assist me.*
- **Acceptance Criteria:**
  - Categories: `TRANSACTION_DISPUTE`, `CARD_REQUEST`, `ACCOUNT_ISSUE`, `GENERAL_QUERY`.
  - Customer inputs: Subject, Description, and optional Account/Transaction reference.
  - Ticket is created with status `OPEN` and assigned a unique Tracking ID (e.g., `TCK-1001`).

#### US-15: Support Ticket Assignment & Resolution
- **Service:** `support-service`
- **Actor:** Bank Employee (`ROLE_EMPLOYEE`)
- **User Story:** *As a support executive, I want to view unassigned support tickets and submit resolution notes, so that customer issues are addressed promptly.*
- **Acceptance Criteria:**
  - Support dashboard displays tickets sorted by status (`OPEN`, `IN_PROGRESS`, `RESOLVED`).
  - Employee assigns ticket to themselves (`IN_PROGRESS`), enters resolution comments, and marks `RESOLVED`.

#### US-16: Customer Ticket Tracking & Closure
- **Service:** `support-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a customer, I want to see the resolution provided on my support ticket and mark it closed, so that I confirm the issue is settled.*
- **Acceptance Criteria:**
  - Customer views employee resolution comments.
  - Customer clicks **Accept & Close**, updating ticket status to `CLOSED`.

---

### Module 6: Audit, Compliance & Event Notifications

#### US-17: High-Value & Suspicious Transaction Monitoring
- **Service:** `audit-notification-service`
- **Actor:** Compliance Officer (`ROLE_COMPLIANCE`)
- **User Story:** *As a compliance officer, I want a dedicated feed highlighting all transactions exceeding $10,000, so that I can detect and investigate potential financial anomalies.*
- **Acceptance Criteria:**
  - Automatic flag triggered for any single transaction with $\text{Amount} \ge \$10,000$.
  - Compliance dashboard highlights flagged records with sender, receiver, timestamp, and amount.
  - Compliance officer has a one-click action to navigate to the account and trigger an account freeze if suspicious.

#### US-18: Regulatory Audit Trail Inspection
- **Service:** `audit-notification-service`
- **Actor:** Compliance Officer (`ROLE_COMPLIANCE`)
- **User Story:** *As a compliance officer, I want to review an immutable audit log of critical system operations, so that regulatory compliance is verified.*
- **Acceptance Criteria:**
  - Audit logs record: User ID, Role, Action (`LOGIN`, `KYC_APPROVAL`, `ACCOUNT_FREEZE`, `TRANSFER`, `LOAN_APPROVAL`), Timestamp, and IP/Status.
  - Read-only interface with search and date-range filtering.

#### US-19: Event-Driven Customer Transaction Notifications
- **Service:** `audit-notification-service`
- **Actor:** Retail Customer (`ROLE_CUSTOMER`)
- **User Story:** *As a customer, I want to receive real-time notifications for account debits, credits, discounts, and loan approvals, so that I stay informed about my account activity.*
- **Acceptance Criteria:**
  - Real-time in-app notification badge and alert list triggered upon:
    - Fund transfer debit or credit.
    - Bill payment with discount applied (e.g., *"Bill paid! $25.00 discount applied on your purchase"*).
    - Loan approval/disbursement.
  - Notifications display timestamp, summary message, and read/unread status.

---

## 4. Non-Functional Requirements (NFRs)

### 4.1 Security & Access Control (NFR-SEC)
- **NFR-SEC-01 (Stateless Authentication):** Spring Security 6 with stateless JWT tokens issued by `auth-service`.
- **NFR-SEC-02 (Role-Based Authorization):** Strict role guards enforced on Gateway and microservice endpoints:
  - Customer endpoints $\to$ `hasRole('CUSTOMER')`
  - Employee endpoints $\to$ `hasRole('EMPLOYEE')`
  - Compliance endpoints $\to$ `hasRole('COMPLIANCE')`
- **NFR-SEC-03 (Credential Security):** BCrypt password hashing (strength factor 10). Sensitive API payloads validated against injection attacks.

### 4.2 Performance & Latency (NFR-PERF)
- **NFR-PERF-01 (API Latency SLA):** 95% of standard read and transaction endpoints must return within **$< 300\text{ ms}$**.
- **NFR-PERF-02 (Database Optimization):** Database indexes on foreign keys, account numbers, and username fields to ensure rapid retrieval.

### 4.3 Transaction Integrity (NFR-TXN)
- **NFR-TXN-01 (ACID Guarantees):** All financial transfers (debit source + credit destination) executed within Spring `@Transactional` scope. Any failure triggers total rollback.
- **NFR-TXN-02 (Data Validation):** Mandatory Bean Validation (`@Valid`, `@Positive`, `@NotNull`) on incoming DTOs.

### 4.4 Resilience & Fault Tolerance (NFR-RES)
- **NFR-RES-01 (Centralized Error Handling):** `@RestControllerAdvice` across all services returning standardized error JSON (`timestamp`, `status`, `error`, `message`, `path`).
- **NFR-RES-02 (Service Decoupling):** Non-critical service issues (e.g., notification failure) do not block core account or transfer completions.

### 4.5 Maintainability & Scalability (NFR-SCAL)
- **NFR-SCAL-01 (Maven Multi-Module):** Clean modular structure enabling independent building, testing, and deployment of all 8 microservices.
- **NFR-SCAL-02 (Frontend Architecture):** Standalone components in Angular 17 with modular routing and Bootstrap 5 responsive grid.

---

## 5. Traceability Matrix: Microservices to Modules & User Stories

| # | Microservice | Mapped Functional Module | Supported User Stories |
| :- | :--- | :--- | :--- |
| **1** | `api-gateway` | Gateway, Routing & Security Filter | Global Gateway Routing for US-01 through US-19 |
| **2** | `auth-service` | Identity, Security & RBAC | **US-01** (Registration, Login, JWT Issuance) |
| **3** | `customer-service` | Module 1: Customer Onboarding & KYC | **US-01**, **US-02** (Document Upload), **US-03** (KYC Review) |
| **4** | `account-service` | Module 2: Account Lifecycle & Balance | **US-04** (Provisioning), **US-05** (Balance), **US-06** (Freeze) |
| **5** | `transaction-service`| Module 3: Transfers, Bills & Discounts | **US-07** (Transfers), **US-08** (Validation $>0$), **US-09** (Discount $>500$), **US-10** (Bills) |
| **6** | `loan-service` | Module 4: Loan Application & Verification | **US-11** (EMI Calc), **US-12** (Application), **US-13** (Underwriting & Disburse) |
| **7** | `support-service` | Module 5: Customer Service Desk | **US-14** (Ticket Raise), **US-15** (Resolution), **US-16** (Closure) |
| **8** | `audit-notification-service` | Module 6: Audit, Compliance & Alerts | **US-17** (Fraud Surveillance), **US-18** (Audit Logs), **US-19** (Notifications) |

---

## 6. Business Rule Summary (Quick Reference)

| Rule ID | Rule Name | Target Condition | Enforced Behavior |
| :--- | :--- | :--- | :--- |
| **BR-01** | Non-Positive Amount Rejection | `amount <= 0` | **Reject with 400 Bad Request** (`Transaction amount must be strictly greater than zero`). |
| **BR-02** | Positive Amount Validation | `amount > 0` | Proceed to fund and discount evaluation. |
| **BR-03** | Promotional Discount Qualification | `amount <= 500` | **No discount applied** (`discount = 0.00`). Full amount billed. |
| **BR-04** | Promotional Discount Application | `amount > 500` | **Apply 5% discount** (`netAmount = amount * 0.95`). |
| **BR-05** | Sufficient Balance Enforcement | `availableBalance < netAmount` | **Reject with Insufficient Funds error (HTTP 400/402)**. |
| **BR-06** | Frozen Account Protection | `accountStatus == FROZEN` | **Block all outgoing transfers and debits (HTTP 403)**. |
| **BR-07** | High-Value Flagging | `amount >= 10,000` | **Auto-flag transaction in Compliance Monitoring Queue**. |

---
*Document Version: 1.1.0 — Infy_Bank360 Requirements Specification (Infosys FA3)*
