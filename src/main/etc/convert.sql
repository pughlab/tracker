-- Simple MySQL script needed to convert the node.js tracker data into
-- something compatible with the Java tracker.

BEGIN;

DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS roles;

ALTER TABLE case_attribute_strings DROP COLUMN modified, DROP COLUMN modified_by, DROP COLUMN active;
ALTER TABLE case_attribute_booleans DROP COLUMN modified, DROP COLUMN modified_by, DROP COLUMN active;
ALTER TABLE case_attribute_dates DROP COLUMN modified, DROP COLUMN modified_by, DROP COLUMN active;
ALTER TABLE audit_log MODIFY event_user varchar(128) NOT NULL;

CREATE TABLE roles (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(64) NOT NULL, 
  UNIQUE (name)
);

CREATE TABLE user_roles (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(128) NOT NULL, 
  role_id INTEGER NOT NULL REFERENCES roles(id)
);

CREATE TABLE role_permissions (
  id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  role_id INTEGER NOT NULL REFERENCES roles(id),
  permission VARCHAR(64) NOT NULL
);

-- Add the roles

INSERT INTO roles (name)
SELECT 'ROLE_ADMIN' AS name;

INSERT INTO user_roles (username, role_id)
SELECT u.username AS username, r.id
FROM study_role_users sru
JOIN users u ON u.id = sru.user_id
JOIN study_roles sr ON sru.study_role_id = sr.id
JOIN roles r 
WHERE sr.study_id IS NULL
AND r.name = 'ROLE_ADMIN';

INSERT INTO roles (name) 
SELECT CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', ''))) AS name
FROM study_roles sr JOIN studies s ON sr.study_id = s.id;

-- Add the users for those roles
INSERT INTO user_roles (username, role_id)
SELECT u.username AS username, r.id
FROM study_role_users sru
JOIN users u ON u.id = sru.user_id
JOIN study_roles sr ON sru.study_role_id = sr.id
JOIN studies s ON sr.study_id = s.id
JOIN roles r ON r.name = CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', '')));

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'admin'
FROM roles r
WHERE r.name = 'ROLE_ADMIN';

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, 'study:*:*'
FROM roles r
WHERE r.name = 'ROLE_ADMIN';

-- All permissions on a study
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, CONCAT('study', ':', '*', ':', s.name) as permission
FROM study_role_permissions srp
JOIN study_roles sr ON srp.study_role_id = sr.id
JOIN studies s ON sr.study_id = s.id
JOIN roles r ON r.name = CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', '')))
AND srp.resource_type = 'study'
AND srp.permission IN ('all');

-- Create on a study
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, CONCAT('study', ':', srp.permission, ':', s.name) as permission
FROM study_role_permissions srp
JOIN study_roles sr ON srp.study_role_id = sr.id
JOIN studies s ON sr.study_id = s.id
JOIN roles r ON r.name = CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', '')))
AND srp.resource_type = 'study'
AND srp.permission IN ('create');

-- read and write on views
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, CONCAT('view', ':', srp.permission, ':', s.name, '-', srp.resource) as permission
FROM study_role_permissions srp
JOIN study_roles sr ON srp.study_role_id = sr.id
JOIN studies s ON sr.study_id = s.id
JOIN roles r ON r.name = CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', '')))
WHERE srp.permission IN ('read', 'write')
AND srp.resource_type = 'view';

INSERT INTO role_permissions (role_id, permission)
SELECT r.id, CONCAT('view', ':', '*', ':', s.name, '-', REPLACE(srp.resource, '.', '')) as permission
FROM study_role_permissions srp
JOIN study_roles sr ON srp.study_role_id = sr.id
JOIN studies s ON sr.study_id = s.id
JOIN roles r ON r.name = CONCAT('ROLE_', UCASE(s.name), '_', UCASE(REPLACE(sr.name, ' ', '')))
WHERE srp.permission IN ('all')
AND srp.resource_type = 'view';

COMMIT;
