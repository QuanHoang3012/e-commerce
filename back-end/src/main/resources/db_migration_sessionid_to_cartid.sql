-- ============================================================
-- Migration Script: session_id → cart_id
-- Database: MySQL / PostgreSQL Compatible
-- Date: 2026-01-14
-- ============================================================

-- 1. RENAME COLUMN: session_id → cart_id
-- ============================================================

-- MySQL
ALTER TABLE inventory_reservations 
CHANGE COLUMN session_id cart_id VARCHAR(255) NOT NULL;

-- PostgreSQL (comment out MySQL above and use this)
-- ALTER TABLE inventory_reservations 
-- RENAME COLUMN session_id TO cart_id;

-- 2. CREATE NEW INDEXES
-- ============================================================

-- Index for cart_id + status (most common query)
CREATE INDEX idx_cart_id_status 
ON inventory_reservations(cart_id, status);

-- Index for variant_id + status (for calculating reserved quantity)
CREATE INDEX idx_variant_status 
ON inventory_reservations(variant_id, status);

-- 3. DROP OLD INDEXES (if exists)
-- ============================================================

-- MySQL
DROP INDEX IF EXISTS idx_session_id_status ON inventory_reservations;

-- PostgreSQL
-- DROP INDEX IF EXISTS idx_session_id_status;

-- 4. VERIFY CHANGES
-- ============================================================

-- Show table structure
DESC inventory_reservations; -- MySQL
-- \d inventory_reservations  -- PostgreSQL

-- Show all indexes
SHOW INDEX FROM inventory_reservations; -- MySQL
-- \di inventory_reservations;           -- PostgreSQL

-- ============================================================
-- ROLLBACK (if needed)
-- ============================================================

/*
-- MySQL
ALTER TABLE inventory_reservations 
CHANGE COLUMN cart_id session_id VARCHAR(255) NOT NULL;

-- PostgreSQL
-- ALTER TABLE inventory_reservations 
-- RENAME COLUMN cart_id TO session_id;

DROP INDEX IF EXISTS idx_cart_id_status ON inventory_reservations;
DROP INDEX IF EXISTS idx_variant_status ON inventory_reservations;

CREATE INDEX idx_session_id_status 
ON inventory_reservations(session_id, status);
*/
