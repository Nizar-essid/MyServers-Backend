-- Script de mise à jour de la base de données pour ajouter le statut CANCELLED
-- Exécutez ce script dans votre base de données MySQL

USE myservers1;

-- 1. Mettre à jour les enums pour supprimer PENDING et DEDUCT
ALTER TABLE balance_change_history MODIFY COLUMN payment_status ENUM('PAID', 'UNPAID', 'CANCELLED') NOT NULL;
ALTER TABLE balance_change_history MODIFY COLUMN change_type ENUM('ADD', 'SET') NOT NULL;

-- 2. Ajouter la colonne cancellation_reason si elle n'existe pas
ALTER TABLE balance_change_history ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;

-- 3. Rendre admin_id nullable si ce n'est pas déjà le cas
ALTER TABLE balance_change_history MODIFY COLUMN admin_id INTEGER NULL;

-- 4. Vérifier la structure mise à jour
DESCRIBE balance_change_history;
