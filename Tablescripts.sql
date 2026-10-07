-- =============================================================================
-- Infy_Bank360 Platform — Relational Database Master Script
-- File Name: Tablescripts.sql
-- Target Database: MySQL 8.0+
-- References: spec.md (Technical Specification) & plan.md (Implementation Plan)
-- =============================================================================

-- =============================================================================
-- 1. Database Initialization
-- =============================================================================
DROP DATABASE IF EXISTS infy_bank360_db;
CREATE DATABASE infy_bank360_db;
USE infy_bank360_db;

-- =============================================================================
-- 2. Drop Tables in Reverse Foreign Key Order
-- =============================================================================
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS high_value_transactions;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS loan_application;
DROP TABLE IF EXISTS service_request;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS users;

-- =============================================================================
-- 3. Table DDL Definitions with Constraints
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Entity: Customer (com.infy.entity.Customer)
-- Represents retail banking customer identity, personal KYC, and onboarding profile.
-- -----------------------------------------------------------------------------
CREATE TABLE customer (
    customer_id BIGINT AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL,
    mobile_number VARCHAR(15) NOT NULL,
    national_id VARCHAR(30) NOT NULL,
    kyc_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_KYC',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_customer PRIMARY KEY (customer_id),
    CONSTRAINT uq_customer_email UNIQUE (email),
    CONSTRAINT uq_customer_mobile_number UNIQUE (mobile_number),
    CONSTRAINT uq_customer_national_id UNIQUE (national_id),
    CONSTRAINT chk_customer_kyc_status CHECK (
        kyc_status IN ('PENDING_KYC', 'SUBMITTED_FOR_VERIFICATION', 'KYC_VERIFIED', 'KYC_REJECTED')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_customer_kyc_status ON customer (kyc_status);

-- -----------------------------------------------------------------------------
-- Entity: Account (com.infy.entity.Account)
-- Represents customer deposit accounts (Savings/Checking) with real-time balance.
-- -----------------------------------------------------------------------------
CREATE TABLE account (
    account_id BIGINT AUTO_INCREMENT,
    account_number VARCHAR(12) NOT NULL,
    customer_id BIGINT NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_account PRIMARY KEY (account_id),
    CONSTRAINT uq_account_number UNIQUE (account_number),
    CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) 
        REFERENCES customer (customer_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    CONSTRAINT chk_account_balance CHECK (balance >= 0.00),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_account_customer_id ON account (customer_id);
CREATE INDEX idx_account_status ON account (status);

-- -----------------------------------------------------------------------------
-- Entity: TransactionRecord (com.infy.entity.TransactionRecord)
-- Represents double-entry ledger entries, bill payments, and promotional discount audits.
-- -----------------------------------------------------------------------------
CREATE TABLE transaction_record (
    transaction_id BIGINT AUTO_INCREMENT,
    source_account_number VARCHAR(12) NOT NULL,
    destination_account_number VARCHAR(12) DEFAULT NULL,
    gross_amount DECIMAL(15, 2) NOT NULL,
    discount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    net_amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',

    CONSTRAINT pk_transaction_record PRIMARY KEY (transaction_id),
    CONSTRAINT fk_txn_source_account FOREIGN KEY (source_account_number) 
        REFERENCES account (account_number) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_txn_destination_account FOREIGN KEY (destination_account_number) 
        REFERENCES account (account_number) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_txn_gross_amount CHECK (gross_amount > 0.00),
    CONSTRAINT chk_txn_discount CHECK (discount >= 0.00),
    CONSTRAINT chk_txn_net_amount CHECK (net_amount > 0.00 AND net_amount <= gross_amount),
    CONSTRAINT chk_txn_type CHECK (transaction_type IN ('TRANSFER', 'UTILITY_BILL', 'DEPOSIT')),
    CONSTRAINT chk_txn_status CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING', 'COMPLIANCE_FLAGGED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_txn_source_account ON transaction_record (source_account_number);
CREATE INDEX idx_txn_dest_account ON transaction_record (destination_account_number);
CREATE INDEX idx_txn_date ON transaction_record (transaction_date);
CREATE INDEX idx_txn_status ON transaction_record (status);

-- -----------------------------------------------------------------------------
-- Entity: LoanApplication (com.infy.entity.LoanApplication)
-- Represents customer loan applications, monthly EMI computations, and underwriting.
-- -----------------------------------------------------------------------------
CREATE TABLE loan_application (
    loan_id BIGINT AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    loan_type VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    tenure_months INT NOT NULL,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    monthly_emi DECIMAL(15, 2) NOT NULL,
    monthly_income DECIMAL(15, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'APPLIED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_loan_application PRIMARY KEY (loan_id),
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) 
        REFERENCES customer (customer_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_loan_type CHECK (loan_type IN ('PERSONAL', 'HOME', 'VEHICLE')),
    CONSTRAINT chk_loan_principal CHECK (principal_amount >= 1000.00),
    CONSTRAINT chk_loan_tenure CHECK (tenure_months BETWEEN 12 AND 120),
    CONSTRAINT chk_loan_interest_rate CHECK (annual_interest_rate > 0.00),
    CONSTRAINT chk_loan_monthly_emi CHECK (monthly_emi > 0.00),
    CONSTRAINT chk_loan_monthly_income CHECK (monthly_income >= 500.00),
    CONSTRAINT chk_loan_status CHECK (status IN ('APPLIED', 'APPROVED', 'REJECTED', 'DISBURSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_loan_customer_id ON loan_application (customer_id);
CREATE INDEX idx_loan_status ON loan_application (status);

-- -----------------------------------------------------------------------------
-- Entity: ServiceRequest (com.infy.entity.ServiceRequest)
-- Represents customer support tickets, disputes, and service request tracking.
-- -----------------------------------------------------------------------------
CREATE TABLE service_request (
    service_request_id BIGINT AUTO_INCREMENT,
    tracking_id VARCHAR(30) NOT NULL,
    customer_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_service_request PRIMARY KEY (service_request_id),
    CONSTRAINT uq_service_request_tracking_id UNIQUE (tracking_id),
    CONSTRAINT fk_service_request_customer FOREIGN KEY (customer_id) 
        REFERENCES customer (customer_id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_service_request_category CHECK (
        category IN ('TRANSACTION_DISPUTE', 'CARD_REQUEST', 'ACCOUNT_ISSUE', 'GENERAL_QUERY')
    ),
    CONSTRAINT chk_service_request_description CHECK (CHAR_LENGTH(description) >= 10),
    CONSTRAINT chk_service_request_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_service_request_customer_id ON service_request (customer_id);
CREATE INDEX idx_service_request_status ON service_request (status);

-- -----------------------------------------------------------------------------
-- Entity: User (com.infy.bank360.auth.entity.User)
-- Represents user authentication credentials and RBAC roles.
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT,
    username VARCHAR(60) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CUSTOMER',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_CUSTOMER', 'ROLE_EMPLOYEE', 'ROLE_COMPLIANCE', 'ROLE_SYSTEM')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'PENDING_KYC', 'BLOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role ON users (role);

-- -----------------------------------------------------------------------------
-- Entity: AuditLog (com.infy.bank360.audit.entity.AuditLog)
-- Represents regulatory immutable system audit log entries.
-- -----------------------------------------------------------------------------
CREATE TABLE audit_logs (
    audit_id BIGINT AUTO_INCREMENT,
    user_id VARCHAR(60) NOT NULL,
    user_role VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_id VARCHAR(60),
    details VARCHAR(1000),
    client_ip_address VARCHAR(45),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (audit_id),
    CONSTRAINT chk_audit_action CHECK (action IN ('LOGIN', 'KYC_VERIFICATION', 'ACCOUNT_STATE_CHANGE', 'FUND_TRANSFER', 'LOAN_APPROVAL', 'SECURITY_EVENT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp);

-- -----------------------------------------------------------------------------
-- Entity: HighValueTransaction (com.infy.bank360.audit.entity.HighValueTransaction)
-- Represents compliance AML transaction surveillance flags.
-- -----------------------------------------------------------------------------
CREATE TABLE high_value_transactions (
    flag_id BIGINT AUTO_INCREMENT,
    transaction_id BIGINT,
    source_account VARCHAR(12) NOT NULL,
    destination_account VARCHAR(12),
    amount DECIMAL(15, 2) NOT NULL,
    flag_reason VARCHAR(255),
    flagged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    compliance_notes VARCHAR(500),

    CONSTRAINT pk_high_value_txns PRIMARY KEY (flag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hvt_flagged_at ON high_value_transactions (flagged_at);
CREATE INDEX idx_hvt_reviewed ON high_value_transactions (reviewed);

-- -----------------------------------------------------------------------------
-- Entity: Notification (com.infy.bank360.audit.entity.Notification)
-- Represents event-driven customer transaction and lifecycle alerts.
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT chk_notif_type CHECK (type IN ('DEBIT', 'CREDIT', 'DISCOUNT', 'LOAN', 'ALERT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_notifications_customer_id ON notifications (customer_id);
CREATE INDEX idx_notifications_is_read ON notifications (is_read);

-- =============================================================================
-- 4. Seed Data Insertion (6 realistic sample records per table)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Seed: customer (6 records across KYC lifecycles: VERIFIED, PENDING, SUBMITTED, REJECTED)
-- -----------------------------------------------------------------------------
INSERT INTO customer (customer_id, full_name, email, mobile_number, national_id, kyc_status) VALUES
(1, 'Rajesh Sharma', 'rajesh.sharma@infybank.com', '9876543210', 'IND10982345', 'KYC_VERIFIED'),
(2, 'Priya Patel', 'priya.patel@infybank.com', '9812345678', 'IND20873456', 'KYC_VERIFIED'),
(3, 'Amit Vikram Verma', 'amit.verma@infybank.com', '9723456789', 'IND30764567', 'KYC_VERIFIED'),
(4, 'Sneha Kulkarni', 'sneha.kulkarni@infybank.com', '9634567890', 'IND40655678', 'PENDING_KYC'),
(5, 'Arjun Nair', 'arjun.nair@infybank.com', '9545678901', 'IND50546789', 'SUBMITTED_FOR_VERIFICATION'),
(6, 'Vikramaditya Rao', 'vikram.rao@infybank.com', '9456789012', 'IND60437890', 'KYC_REJECTED');

-- -----------------------------------------------------------------------------
-- Seed: account (6 records across Savings and Checking types with realistic opening balances)
-- -----------------------------------------------------------------------------
INSERT INTO account (account_id, account_number, customer_id, account_type, balance, status) VALUES
(1, '100000000001', 1, 'SAVINGS', 1000.00, 'ACTIVE'),
(2, '100000000002', 1, 'CHECKING', 2500.00, 'ACTIVE'),
(3, '100000000003', 2, 'SAVINGS', 15000.00, 'ACTIVE'),
(4, '100000000004', 2, 'CHECKING', 8500.00, 'ACTIVE'),
(5, '100000000005', 3, 'SAVINGS', 32000.75, 'ACTIVE'),
(6, '100000000006', 3, 'SAVINGS', 450.00, 'FROZEN');

-- -----------------------------------------------------------------------------
-- Seed: transaction_record (6 records illustrating 5% discount rule for amount > 500)
-- -----------------------------------------------------------------------------
INSERT INTO transaction_record (transaction_id, source_account_number, destination_account_number, gross_amount, discount, net_amount, transaction_type, transaction_date, status) VALUES
(1, '100000000001', '100000000003', 1200.00, 60.00, 1140.00, 'TRANSFER', '2026-10-01 10:30:00', 'SUCCESS'),
(2, '100000000002', NULL, 650.00, 32.50, 617.50, 'UTILITY_BILL', '2026-10-02 11:45:00', 'SUCCESS'),
(3, '100000000003', '100000000005', 300.00, 0.00, 300.00, 'TRANSFER', '2026-10-03 14:15:00', 'SUCCESS'),
(4, '100000000004', NULL, 450.00, 0.00, 450.00, 'UTILITY_BILL', '2026-10-04 16:00:00', 'SUCCESS'),
(5, '100000000005', '100000000001', 2000.00, 100.00, 1900.00, 'TRANSFER', '2026-10-05 09:20:00', 'SUCCESS'),
(6, '100000000001', '100000000004', 1500.00, 75.00, 1425.00, 'TRANSFER', '2026-10-06 18:00:00', 'FAILED');

-- -----------------------------------------------------------------------------
-- Seed: loan_application (6 records across PERSONAL, HOME, VEHICLE with exact calculated EMIs)
-- -----------------------------------------------------------------------------
INSERT INTO loan_application (loan_id, customer_id, loan_type, principal_amount, tenure_months, annual_interest_rate, monthly_emi, monthly_income, status) VALUES
(1, 1, 'PERSONAL', 200000.00, 24, 11.50, 9368.00, 65000.00, 'APPROVED'),
(2, 2, 'HOME', 1500000.00, 120, 8.50, 18600.35, 120000.00, 'APPLIED'),
(3, 3, 'VEHICLE', 450000.00, 36, 9.75, 14472.10, 80000.00, 'DISBURSED'),
(4, 1, 'PERSONAL', 50000.00, 12, 12.00, 4442.44, 65000.00, 'REJECTED'),
(5, 2, 'VEHICLE', 300000.00, 24, 9.50, 13774.20, 120000.00, 'APPLIED'),
(6, 3, 'HOME', 2500000.00, 84, 8.25, 39250.00, 150000.00, 'APPROVED');

-- -----------------------------------------------------------------------------
-- Seed: service_request (6 records across dispute categories and statuses)
-- -----------------------------------------------------------------------------
INSERT INTO service_request (service_request_id, tracking_id, customer_id, category, description, status) VALUES
(1, 'TCK-2026-10001', 1, 'TRANSACTION_DISPUTE', 'Dispute regarding duplicate debit on utility bill payment on 2026-10-02.', 'IN_PROGRESS'),
(2, 'TCK-2026-10002', 2, 'CARD_REQUEST', 'Requesting issuance of an international contactless platinum debit card.', 'OPEN'),
(3, 'TCK-2026-10003', 3, 'ACCOUNT_ISSUE', 'Account status unexpectedly set to frozen after online funds transfer attempt.', 'RESOLVED'),
(4, 'TCK-2026-10004', 1, 'GENERAL_QUERY', 'Inquiry regarding annual interest rate calculation formula for recurring deposits.', 'CLOSED'),
(5, 'TCK-2026-10005', 2, 'TRANSACTION_DISPUTE', 'Fund transfer to beneficiary failed but amount was deducted from savings account balance.', 'OPEN'),
(6, 'TCK-2026-10006', 3, 'ACCOUNT_ISSUE', 'Request to update primary communication address and linking with current branch.', 'RESOLVED');

-- -----------------------------------------------------------------------------
-- Seed: users (6 records across roles: CUSTOMER, EMPLOYEE, COMPLIANCE)
-- -----------------------------------------------------------------------------
INSERT INTO users (user_id, username, email, password, role, status) VALUES
(1, 'rajesh.sharma', 'rajesh.sharma@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_CUSTOMER', 'ACTIVE'),
(2, 'priya.patel', 'priya.patel@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_CUSTOMER', 'ACTIVE'),
(3, 'amit.verma', 'amit.verma@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_CUSTOMER', 'ACTIVE'),
(4, 'onboarding.officer', 'officer1@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_EMPLOYEE', 'ACTIVE'),
(5, 'loan.officer', 'officer2@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_EMPLOYEE', 'ACTIVE'),
(6, 'compliance.lead', 'compliance@infybank.com', '$2a$10$wN31r3zE8eR5O7oG9Y8NKeWzJz7pYv/d7f7wG.zVn7D9Jk6.1z4.W', 'ROLE_COMPLIANCE', 'ACTIVE');

-- -----------------------------------------------------------------------------
-- Seed: audit_logs (6 immutable audit trail records)
-- -----------------------------------------------------------------------------
INSERT INTO audit_logs (audit_id, user_id, user_role, action, resource_id, details, client_ip_address) VALUES
(1, 'rajesh.sharma', 'ROLE_CUSTOMER', 'LOGIN', 'USR-1', 'Successful customer portal login', '192.168.1.10'),
(2, 'onboarding.officer', 'ROLE_EMPLOYEE', 'KYC_VERIFICATION', 'CUST-1', 'KYC verified and national ID documents approved', '192.168.1.50'),
(3, 'rajesh.sharma', 'ROLE_CUSTOMER', 'FUND_TRANSFER', 'TXN-1', 'Intra-bank transfer of $1200.00 with 5% promo discount', '192.168.1.10'),
(4, 'compliance.lead', 'ROLE_COMPLIANCE', 'ACCOUNT_STATE_CHANGE', '100000000006', 'Suspicious activity detected; account status set to FROZEN', '192.168.1.99'),
(5, 'loan.officer', 'ROLE_EMPLOYEE', 'LOAN_APPROVAL', 'LOAN-1', 'Personal loan application approved following DTI check', '192.168.1.55'),
(6, 'system', 'ROLE_SYSTEM', 'SECURITY_EVENT', 'SYS-AUTH', 'Automated token validation cycle completed', '127.0.0.1');

-- -----------------------------------------------------------------------------
-- Seed: high_value_transactions (6 compliance surveillance records)
-- -----------------------------------------------------------------------------
INSERT INTO high_value_transactions (flag_id, transaction_id, source_account, destination_account, amount, flag_reason, reviewed, compliance_notes) VALUES
(1, 101, '100000000003', '100000000005', 15000.00, 'Transaction amount $15,000.00 exceeded $10,000 threshold', TRUE, 'Verified legitimate business wire transfer'),
(2, 102, '100000000005', '100000000001', 25000.00, 'Transaction amount $25,000.00 exceeded $10,000 threshold', FALSE, 'Pending document verification'),
(3, 103, '100000000004', '100000000002', 12500.00, 'Transaction amount $12,500.00 exceeded $10,000 threshold', TRUE, 'Approved corporate retainer payment'),
(4, 104, '100000000001', '100000000003', 10500.00, 'Transaction amount $10,500.00 exceeded $10,000 threshold', FALSE, 'Review initiated by compliance officer'),
(5, 105, '100000000005', '100000000004', 32000.00, 'Transaction amount $32,000.00 exceeded $10,000 threshold', TRUE, 'Capital expenditure invoice cleared'),
(6, 106, '100000000002', '100000000006', 11000.00, 'Transaction amount $11,000.00 exceeded $10,000 threshold to frozen target', TRUE, 'Blocked transfer attempt under AML flag');

-- -----------------------------------------------------------------------------
-- Seed: notifications (6 event notifications)
-- -----------------------------------------------------------------------------
INSERT INTO notifications (notification_id, customer_id, title, message, type, is_read) VALUES
(1, 1, 'Transfer Completed', 'Successfully transferred $1,140.00 to account 100000000003. $60.00 promotional discount applied!', 'DISCOUNT', TRUE),
(2, 1, 'Utility Bill Payment', 'Electricity bill of $617.50 settled with $32.50 discount applied.', 'DISCOUNT', FALSE),
(3, 2, 'KYC Verified', 'Congratulations! Your identity documents have been approved. You can now open accounts.', 'ALERT', TRUE),
(4, 1, 'Loan Approved', 'Your Personal Loan application (#1) for $200,000.00 has been approved!', 'LOAN', FALSE),
(5, 3, 'Account Status Update', 'Notice: Account 100000000006 has been set to FROZEN pending compliance verification.', 'ALERT', FALSE),
(6, 2, 'Debit Alert', 'Account 100000000003 debited with $300.00 for online funds transfer.', 'DEBIT', TRUE);

-- =============================================================================
-- 5. Verification Queries (SELECT * for all tables)
-- =============================================================================
SELECT * FROM users;
SELECT * FROM customer;
SELECT * FROM account;
SELECT * FROM transaction_record;
SELECT * FROM loan_application;
SELECT * FROM service_request;
SELECT * FROM audit_logs;
SELECT * FROM high_value_transactions;
SELECT * FROM notifications;
