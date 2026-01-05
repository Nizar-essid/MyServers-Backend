-- Add cost column to code table
-- This column stores the purchase price (cost) at which the admin acquired the code
-- It is nullable to maintain backward compatibility with existing codes

ALTER TABLE code ADD COLUMN IF NOT EXISTS cost FLOAT NULL;

-- Add a comment to explain the column
ALTER TABLE code MODIFY COLUMN cost FLOAT NULL COMMENT 'Purchase price/cost at which admin acquired the code';


