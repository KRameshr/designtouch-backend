-- Widen stat_item.label while preserving existing values and constraints.
ALTER TABLE stat_item
    ALTER COLUMN label TYPE VARCHAR(150)
    USING label::VARCHAR(150);
