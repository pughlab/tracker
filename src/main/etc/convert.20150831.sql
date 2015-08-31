-- Used to convert the 2.0.0 schema to the 2.0.1 schema
--  * Attribute identifier association
--  * Study-specific roles

SET SESSION SQL_MODE = 'ansi,strict_all_tables,no_auto_value_on_zero';

-- Change the attribute association to use attribute identifiers

ALTER TABLE case_attribute_strings ADD COLUMN attribute_id INTEGER NOT NULL;
ALTER TABLE case_attribute_booleans ADD COLUMN attribute_id INTEGER NOT NULL;
ALTER TABLE case_attribute_dates ADD COLUMN attribute_id INTEGER NOT NULL;

UPDATE case_attribute_strings ca
JOIN cases c ON ca.case_id = c.id
JOIN attributes a ON ca.attribute = a.name AND a.study_id = c.study_id
SET ca.attribute_id = a.id;

UPDATE case_attribute_booleans ca
JOIN cases c ON ca.case_id = c.id
JOIN attributes a ON ca.attribute = a.name AND a.study_id = c.study_id
SET ca.attribute_id = a.id;

UPDATE case_attribute_dates ca
JOIN cases c ON ca.case_id = c.id
JOIN attributes a ON ca.attribute = a.name AND a.study_id = c.study_id
SET ca.attribute_id = a.id;

ALTER TABLE case_attribute_strings DROP COLUMN attribute;
ALTER TABLE case_attribute_booleans DROP COLUMN attribute;
ALTER TABLE case_attribute_dates DROP COLUMN attribute;

CREATE TABLE IF NOT EXISTS case_attribute_numbers (
  id INTEGER AUTO_INCREMENT PRIMARY KEY, 
  case_id INTEGER NOT NULL, 
  attribute_id INTEGER NOT NULL,
  "value" DOUBLE, 
  not_available BOOLEAN DEFAULT 0 NOT NULL, 
  notes VARCHAR(2048)
);

CREATE INDEX cas_case_values_strings ON case_attribute_strings(case_id, attribute_id, "value"(32));
CREATE INDEX cas_case_values_booleans ON case_attribute_booleans(case_id, attribute_id, "value");
CREATE INDEX cas_case_values_dates ON case_attribute_dates(case_id, attribute_id, "value");
CREATE INDEX cas_case_values_numbers ON case_attribute_numbers(case_id, attribute_id, "value");

-- It'd be nice to map the roles, but I'm thinking it's more or less impossible
-- without Perl or some such, and it'd be easier to do it manually. 

ALTER TABLE roles ADD COLUMN study_id INTEGER NOT NULL;
UPDATE roles SET study_id = 0;

-- Let's allow zeros into auto_increment, just for this
SET SESSION SQL_MODE = 'ansi,strict_all_tables,no_auto_value_on_zero';

INSERT INTO studies (id, name, description) VALUES (0, 'ADMIN', 'Admin study');
