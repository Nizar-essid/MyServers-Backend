-- Fix zero date values in applications table
-- This script updates any existing records with zero dates to have proper timestamps

UPDATE applications
SET created_at = NOW(), updated_at = NOW()
WHERE created_at = '0000-00-00 00:00:00' OR updated_at = '0000-00-00 00:00:00'
   OR created_at IS NULL OR updated_at IS NULL;

-- Also update any other tables that might have similar issues
UPDATE user_groups
SET date_creation = NOW(), latest_update = NOW()
WHERE date_creation = '0000-00-00 00:00:00' OR latest_update = '0000-00-00 00:00:00'
   OR date_creation IS NULL OR latest_update IS NULL;

UPDATE group_prices
SET date_creation = NOW(), latest_update = NOW()
WHERE date_creation = '0000-00-00 00:00:00' OR latest_update = '0000-00-00 00:00:00'
   OR date_creation IS NULL OR latest_update IS NULL;

UPDATE user_prices
SET date_creation = NOW(), latest_update = NOW()
WHERE date_creation = '0000-00-00 00:00:00' OR latest_update = '0000-00-00 00:00:00'
   OR date_creation IS NULL OR latest_update IS NULL;

UPDATE user_balances
SET last_updated = NOW()
WHERE last_updated = '0000-00-00 00:00:00' OR last_updated IS NULL;

UPDATE user_group_assignments
SET assigned_date = NOW()
WHERE assigned_date = '0000-00-00 00:00:00' OR assigned_date IS NULL;

UPDATE balance_change_history
SET change_date = NOW()
WHERE change_date = '0000-00-00 00:00:00' OR change_date IS NULL;
