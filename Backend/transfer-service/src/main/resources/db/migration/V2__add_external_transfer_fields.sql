-- Add new transfer type enum values and external transfer fields
-- Migration for adding SWIFT, RTGS, PESALINK, MPESA support

-- Add new columns for external transfer types
ALTER TABLE transfers 
ADD COLUMN swift_code VARCHAR(11),
ADD COLUMN recipient_bank_name VARCHAR(100),
ADD COLUMN recipient_bank_address VARCHAR(200),
ADD COLUMN intermediary_bank_swift VARCHAR(11),
ADD COLUMN sort_code VARCHAR(6),
ADD COLUMN pesalink_bank_code VARCHAR(3),
ADD COLUMN mpesa_number VARCHAR(15);

-- Create indexes for better query performance
CREATE INDEX idx_swift_code ON transfers(swift_code);
CREATE INDEX idx_pesalink_bank_code ON transfers(pesalink_bank_code);
CREATE INDEX idx_mpesa_number ON transfers(mpesa_number);
CREATE INDEX idx_transfer_type_status ON transfers(transfer_type, status);

-- Add comments for documentation
COMMENT ON COLUMN transfers.swift_code IS 'SWIFT/BIC code for international transfers';
COMMENT ON COLUMN transfers.recipient_bank_name IS 'Name of recipient bank for SWIFT transfers';
COMMENT ON COLUMN transfers.recipient_bank_address IS 'Address of recipient bank for SWIFT transfers';
COMMENT ON COLUMN transfers.intermediary_bank_swift IS 'Intermediary bank SWIFT code if required';
COMMENT ON COLUMN transfers.sort_code IS 'Sort code for RTGS transfers';
COMMENT ON COLUMN transfers.pesalink_bank_code IS 'Bank code for PesaLink transfers';
COMMENT ON COLUMN transfers.mpesa_number IS 'M-Pesa number for mobile money transfers';
