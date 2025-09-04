-- Fix notifications table type column length to accommodate longer enum values
-- This migration ensures the 'type' column can store all NotificationType enum values

ALTER TABLE notifications MODIFY COLUMN type VARCHAR(50) NOT NULL;
