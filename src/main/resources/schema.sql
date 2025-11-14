CREATE TABLE IF NOT EXISTS beneficiaries (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) NOT NULL,
    account_number VARCHAR(50),
    beneficiary_name VARCHAR(255) NOT NULL,
    beneficiary_account_number VARCHAR(50) NOT NULL,
    beneficiary_bank_code VARCHAR(20) NOT NULL,
    beneficiary_bank_name VARCHAR(255),
    beneficiary_type VARCHAR(20) NOT NULL DEFAULT 'DOMESTIC',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_customer_beneficiary_account UNIQUE (customer_id, beneficiary_account_number, status)
);

CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer_id ON beneficiaries(customer_id);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_customer_account ON beneficiaries(customer_id, account_number);
CREATE INDEX IF NOT EXISTS idx_beneficiaries_status ON beneficiaries(status);
