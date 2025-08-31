-- Add price_after_discount column to on_demand_requests table
ALTER TABLE on_demand_requests ADD COLUMN price_after_discount FLOAT NULL;

-- Update existing records to set price_after_discount equal to price if it's NULL
UPDATE on_demand_requests SET price_after_discount = price WHERE price_after_discount IS NULL;
