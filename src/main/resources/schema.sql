-- Alter text columns that were created as VARCHAR(255) by Hibernate
ALTER TABLE schools ALTER COLUMN logo TYPE text;
ALTER TABLE schools ALTER COLUMN motto TYPE text;
ALTER TABLE users ALTER COLUMN avatar TYPE text;
ALTER TABLE students ALTER COLUMN photo TYPE text;
ALTER TABLE teachers ALTER COLUMN photo TYPE text;
ALTER TABLE enrollments ALTER COLUMN photo TYPE text;
