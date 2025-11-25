-- Add isFieldInspection column to facility table
-- This field indicates if the facility is a field inspection point for shrimp sensory testing

ALTER TABLE facility ADD COLUMN isFieldInspection BIT(1) DEFAULT 0;
