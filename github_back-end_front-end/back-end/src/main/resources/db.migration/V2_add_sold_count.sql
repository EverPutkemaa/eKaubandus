-- Migration: Add sold_count column to products table
ALTER TABLE products ADD COLUMN sold_count INTEGER DEFAULT 0;