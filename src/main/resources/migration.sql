-- Migration script to add CANCELLED status to balance_change_history table
-- Run this script to update existing database

-- Add CANCELLED to payment_status enum
ALTER TABLE balance_change_history MODIFY COLUMN payment_status ENUM('PAID', 'UNPAID', 'PENDING', 'CANCELLED') NOT NULL;

-- Add cancellation_reason column if it doesn't exist
ALTER TABLE balance_change_history ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- Make admin_id nullable if it's not already
ALTER TABLE balance_change_history MODIFY COLUMN admin_id INTEGER NULL;
