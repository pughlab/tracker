-- =============================================================================================
-- Delete permissions due to MySQL stupid integrity ordering

DROP TABLE IF EXISTS "ROLE_PERMISSIONS";
DROP TABLE IF EXISTS "USER_ROLES";
DROP TABLE IF EXISTS "ROLES";

-- =============================================================================================
-- Now for the studies
DROP TABLE IF EXISTS "STUDIES";

CREATE TABLE "STUDIES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "NAME" VARCHAR(24) NOT NULL,
  "DESCRIPTION" VARCHAR(2048),
  "OPTIONS" VARCHAR(2048),
  UNIQUE ("NAME")
);

INSERT INTO "STUDIES" ("ID", "NAME", "DESCRIPTION") VALUES (0, 'ADMIN', 'Admin study');
INSERT INTO "STUDIES" ("ID", "NAME", "DESCRIPTION", "OPTIONS") VALUES (1, 'DEMO', 'A demo clinical genomics study', '{"stateLabels":{"pending":"label1","returnPending":"label2"}}');
INSERT INTO "STUDIES" ("ID", "NAME", "DESCRIPTION") VALUES (2, 'SECOND', 'A second study');

-- =============================================================================================
-- Now for the attributes

DROP TABLE IF EXISTS "ATTRIBUTES";

CREATE TABLE "ATTRIBUTES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER NOT NULL,
  "NAME" VARCHAR(48) NOT NULL,
  "DESCRIPTION" VARCHAR(2048),
  "LABEL" VARCHAR(128) NOT NULL,
  "TYPE" VARCHAR(24) NOT NULL,
  "RANK" INTEGER DEFAULT 0 NOT NULL,
  "OPTIONS" VARCHAR(2048),
  UNIQUE("STUDY_ID", "NAME")
);

INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (1, 1, 1, 'dateEntered', 'Date Entered', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (2, 2, 1, 'patientId', 'Patient ID', 'string', '{"unique":true,"display":"pin_left"}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (3, 3, 1, 'mrn', 'MRN', 'string', '{"tags": ["identifiable"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (4, 4, 1, 'primarySite', 'Primary Disease Site', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (5, 5, 1, 'consentDate', 'Date of Consent', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (6, 6, 1, 'physician', 'Treating Physician', 'string', '{"tags": ["identifiable"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (7, 7, 1, 'sampleAvailable', 'Sample Available At (Hospital/Lab)', 'option', '{"values": ["LMP","St. Michaels","Toronto East General"]}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (8, 8, 1, 'specimenNo', 'Specimen #', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (9, 9, 1, 'procedureDate', 'Date of Procedure', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (10, 10, 1, 'tissueSite', 'Tissue Site', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (11, 11, 1, 'trackerDate', 'Date Internal Request Entered in Tracker', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (12, 12, 1, 'specimenAvailable', 'Biobank Specimen Available? (Yes/No)', 'boolean');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (13, 13, 1, 'biobankDate', 'Date Column M (Biobank Yes/No) Entered', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (14, 14, 1, 'biobankReason', 'Reason Tissue Not Banked', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (15, 15, 1, 'requestDate', 'Date Request Processed', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (16, 16, 1, 'LMPComments', 'LMP Comments', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (17, 17, 1, 'blockLocation', 'Current Block Location', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (18, 18, 1, 'numberCores', 'Number of cores', 'number');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (19, 19, 1, 'specimenBiobankDate', 'Date Specimen went to Biobank', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (20, 20, 1, 'specimenType', 'Specimen Type Collected by CSP (block/slides)', 'string');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (21, 21, 1, 'diagnosticsDate', 'Date of transfer to Molecular Diagnostics', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (22, 22, 1, 'returnRequested', 'Return Requested? (Yes/No)', 'boolean');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (23, 23, 1, 'returnDate', 'Date of Return to Outside Institution', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (24, 24, 1, 'bloodCollDate', 'Date and Time of Blood Collection', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (25, 25, 1, 'insufficientDate', 'Date Tissue Deemed Insufficient', 'date');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE", "OPTIONS") VALUES (26, 26, 1, 'notes', 'Notes', 'string', '{"longtext":true}');
INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (27, 27, 1, 'study', 'Study', 'string');

INSERT INTO "ATTRIBUTES" ("ID", "RANK", "STUDY_ID", "NAME", "LABEL", "TYPE") VALUES (28, 1, 2, 'patientId', 'Patient ID', 'string');

-- =============================================================================================
-- Now for the views

DROP TABLE IF EXISTS "VIEWS";

CREATE TABLE "VIEWS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER NOT NULL,
  "NAME" VARCHAR(24) NOT NULL,
  "DESCRIPTION" VARCHAR(2048),
  "OPTIONS" VARCHAR(2048),
  UNIQUE ("STUDY_ID", "NAME")
);

INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (1, 1, 'Manages the whole study', 'complete');
INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (2, 1, 'Tracks the study', 'track');
INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME", "OPTIONS") VALUES (3, 1, 'Tracks only secondary', 'secondary', '{"rows":[{"attribute":"study","value":"secondary"}]}');

INSERT INTO "VIEWS" ("ID", "STUDY_ID", "DESCRIPTION", "NAME") VALUES (4, 2, 'Manages the whole study', 'complete');

-- =============================================================================================
-- Now for the view attributes

DROP TABLE IF EXISTS "VIEW_ATTRIBUTES";

CREATE TABLE "VIEW_ATTRIBUTES" (
  "VIEW_ID" INTEGER NOT NULL,
  "ATTRIBUTE_ID" INTEGER NOT NULL,
  "RANK" INTEGER DEFAULT 0 NOT NULL,
  "OPTIONS" VARCHAR(2048),
  PRIMARY KEY ("VIEW_ID", "ATTRIBUTE_ID")
);

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (1, 1, 1, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (1, 2, 2, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 3, 3);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 4, 4);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 7, 7);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 8, 8);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 9, 9);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 11, 11);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 13, 13);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 14, 14);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 15, 15);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 17, 17);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 18, 18);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 20, 20);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 21, 21);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 22, 22);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 23, 23);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (1, 27, 27);

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 1, 1);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 2, 2);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (2, 27, 27);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 14, 14, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 20, 20, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (2, 22, 22, '{"classes": ["label5"]}');

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 1, 1);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 2, 2);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 5, 5);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 6, 6);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 10, 10);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 12, 12);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 16, 16);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 19, 19);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 24, 24);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 25, 25);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 26, 26);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (3, 27, 27);
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 14, 14, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 20, 20, '{"classes": ["label5"]}');
INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK", "OPTIONS") VALUES (3, 22, 22, '{"classes": ["label5"]}');

INSERT INTO "VIEW_ATTRIBUTES" ("VIEW_ID", "ATTRIBUTE_ID", "RANK") VALUES (4, 28, 1);

-- =============================================================================================
-- Now for the cases

DROP TABLE IF EXISTS "CASES";

CREATE TABLE "CASES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER NOT NULL,
  "ORDER" INTEGER NOT NULL,
  "STATE" VARCHAR(255) DEFAULT NULL
);
CREATE INDEX "CASES_ORDER" ON "CASES"("ORDER");

INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (1, 1, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (2, 2, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (3, 3, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (4, 4, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (5, 5, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (6, 6, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (7, 7, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (8, 8, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID") VALUES (9, 9, 1);
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (10, 10, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (11, 11, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (12, 12, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (13, 13, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (14, 14, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (15, 15, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (16, 16, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (17, 17, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (18, 18, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (19, 19, 1, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (20, 20, 1, 'pending');

INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (21, 21, 2, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (22, 22, 2, 'active');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (23, 23, 2, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (24, 24, 2, 'pending');
INSERT INTO "CASES" ("ID", "ORDER", "STUDY_ID", "STATE") VALUES (25, 25, 2, 'pending');

-- =============================================================================================
-- Now for the case attribute values, of all types

DROP TABLE IF EXISTS "CASE_ATTRIBUTE_STRINGS";
DROP TABLE IF EXISTS "CASE_ATTRIBUTE_DATES";
DROP TABLE IF EXISTS "CASE_ATTRIBUTE_BOOLEANS";
DROP TABLE IF EXISTS "CASE_ATTRIBUTE_NUMBERS";

CREATE TABLE "CASE_ATTRIBUTE_STRINGS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "CASE_ID" INTEGER NOT NULL,
  "ATTRIBUTE_ID" INTEGER NOT NULL,
  "VALUE" VARCHAR(4096) COLLATE SQL_TEXT_UCC,
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL,
  "NOTES" VARCHAR(2048)
);

CREATE TABLE "CASE_ATTRIBUTE_DATES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "CASE_ID" INTEGER NOT NULL,
  "ATTRIBUTE_ID" INTEGER NOT NULL,
  "VALUE" DATE,
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL,
  "NOTES" VARCHAR(2048)
);

CREATE TABLE "CASE_ATTRIBUTE_BOOLEANS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "CASE_ID" INTEGER NOT NULL,
  "ATTRIBUTE_ID" INTEGER NOT NULL,
  "VALUE" BOOLEAN,
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL,
  "NOTES" VARCHAR(2048)
);

CREATE TABLE "CASE_ATTRIBUTE_NUMBERS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "CASE_ID" INTEGER NOT NULL,
  "ATTRIBUTE_ID" INTEGER NOT NULL,
  "VALUE" DOUBLE,
  "NOT_AVAILABLE" BOOLEAN DEFAULT 0 NOT NULL,
  "NOTES" VARCHAR(2048)
);

CREATE INDEX "IDX_CASE_ATTRIBUTE_STRINGS" ON "CASE_ATTRIBUTE_STRINGS"("CASE_ID", "ATTRIBUTE_ID", "VALUE");
CREATE INDEX "IDX_CASE_ATTRIBUTE_BOOLEANS" ON "CASE_ATTRIBUTE_BOOLEANS"("CASE_ID", "ATTRIBUTE_ID", "VALUE");
CREATE INDEX "IDX_CASE_ATTRIBUTE_DATES" ON "CASE_ATTRIBUTE_DATES"("CASE_ID", "ATTRIBUTE_ID", "VALUE");
CREATE INDEX "IDX_CASE_ATTRIBUTE_NUMBERS" ON "CASE_ATTRIBUTE_NUMBERS"("CASE_ID", "ATTRIBUTE_ID", "VALUE");

INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 1, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 1, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 1, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 1, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 1, '2014-08-21');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (6, 1, '2014-08-22');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (7, 1, '2014-08-22');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (8, 1, '2014-08-23');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (9, 1, '2014-08-23');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (10, 1, '2014-08-24');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (11, 1, '2014-08-25');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (12, 1, '2014-08-26');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (13, 1, '2014-08-28');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (14, 1, '2014-08-28');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (15, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (16, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (17, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (18, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (19, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (20, 1, '2014-08-30');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 2, 'DEMO-01');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 2, 'DEMO-02');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 2, 'DEMO-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 2, 'DEMO-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 2, 'DEMO-06');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (6, 2, 'DEMO-05');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (7, 2, 'DEMO-04');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (8, 2, 'DEMO-07');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (9, 2, 'DEMO-08');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (10, 2, 'DEMO-09');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (11, 2, 'DEMO-10');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (12, 2, 'DEMO-11');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (13, 2, 'DEMO-12');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (14, 2, 'DEMO-13');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (15, 2, 'DEMO-14');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (16, 2, 'DEMO-15');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (17, 2, 'DEMO-16');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (18, 2, 'DEMO-17');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (19, 2, 'DEMO-18');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (20, 2, 'DEMO-19');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 3, '0101010');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 3, '0202020');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 3, '0303030');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 3, '0303030');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 3, '0404040');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 4, 'breast');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 4, 'gyne - ovarian');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 4, 'lung');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 4, 'lung');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 4, 'pancreatobiliary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 6, 'Dr. X');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 6, 'Dr. Y');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 6, 'Dr. Z');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 6, 'Dr. Z');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 6, 'Dr. W');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 7, 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 7, 'St. Michael''s');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 7, 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 7, 'Toronto East General');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 7, 'LMP');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 8, 'S14-1');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 8, 'S14-200');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 8, 'S12-3000');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 8, 'S12-400');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 8, 'S13-333');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 9, '2014-01-01');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 9, '2014-02-03');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 9, '2012-05-04');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 9, '2012-04-26');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 9, '2013-03-01');
INSERT INTO "CASE_ATTRIBUTE_NUMBERS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 18, 2);
INSERT INTO "CASE_ATTRIBUTE_NUMBERS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 18, 4);
INSERT INTO "CASE_ATTRIBUTE_NUMBERS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 18, 1);
INSERT INTO "CASE_ATTRIBUTE_NUMBERS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 18, 5);
INSERT INTO "CASE_ATTRIBUTE_NUMBERS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 18, 4);
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 10, 'breast (left), lymph node');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 10, 'omentum');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 10, 'Lymph node');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 10, 'RLL lung');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 11, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 11, '2014-08-20');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 11, '2014-08-21');
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 12, 1);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 12, 0);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 12, 0);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 12, 0);
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 26, 'This is an extremely long note, which should be able to wrap around and which ought to be clipped properly in the grid');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (1, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (2, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (3, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (4, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (5, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (6, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (7, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (8, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (9, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (10, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (11, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (12, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (13, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (14, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (15, 27, 'primary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (16, 27, 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (17, 27, 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (18, 27, 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (19, 27, 'secondary');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (20, 27, 'secondary');

INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "NOT_AVAILABLE") VALUES (5, 10, 1);
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "NOT_AVAILABLE") VALUES (2, 11, 1);
INSERT INTO "CASE_ATTRIBUTE_BOOLEANS" ("CASE_ID", "ATTRIBUTE_ID", "NOT_AVAILABLE") VALUES (4, 12, 1);

INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE", "NOTES") VALUES (1, 5, '2014-08-19', '{"locked" : true, "tags": ["label3"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE", "NOTES") VALUES (2, 5, '2014-08-18', '{"locked" : true, "tags": ["label3"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE", "NOTES") VALUES (3, 5, '2014-08-19', '{"locked" : true, "tags": ["label3"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE", "NOTES") VALUES (4, 5, '2014-08-19', '{"locked" : true, "tags": ["label3"]}');
INSERT INTO "CASE_ATTRIBUTE_DATES" ("CASE_ID", "ATTRIBUTE_ID", "VALUE", "NOTES") VALUES (5, 5, '2014-08-20', '{"locked" : true, "tags": ["label3"]}');

INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (21, 28, 'SECOND-01');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (22, 28, 'SECOND-02');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (23, 28, 'SECOND-03');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (24, 28, 'SECOND-04');
INSERT INTO "CASE_ATTRIBUTE_STRINGS" ("CASE_ID", "ATTRIBUTE_ID", "VALUE") VALUES (25, 28, 'SECOND-05');

-- =============================================================================================
-- Now for the audit log

DROP TABLE IF EXISTS "AUDIT_LOG";

CREATE TABLE "AUDIT_LOG" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER,
  "CASE_ID" INTEGER,
  "ATTRIBUTE" VARCHAR(24),
  "ATTRIBUTE_ID" INTEGER,
  "EVENT_TIME" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  "EVENT_USER" VARCHAR(128) NOT NULL,
  "EVENT_TYPE" VARCHAR(12) NOT NULL,
  "EVENT_ARGS" VARCHAR(2048)
);

-- =============================================================================================
-- Now for the users

DROP TABLE IF EXISTS "USER_ROLES";
DROP TABLE IF EXISTS "ROLE_PERMISSIONS";
DROP TABLE IF EXISTS "ROLES";
DROP TABLE IF EXISTS "USERS";

CREATE TABLE "USERS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "USERNAME" VARCHAR(64) NOT NULL,
  "HASH" VARCHAR(64),
  "EMAIL" VARCHAR(256),
  "APIKEY" VARCHAR(64),
  "LOCKED" BOOLEAN DEFAULT 0 NOT NULL,
  "FORCE_PASSWORD_CHANGE" BOOLEAN DEFAULT 0 NOT NULL,
  "EXPIRES" DATE
);

INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (1, 'admin', '$2a$10$iiwt9.KFFYLHaIJTrhMH..xIduQS2jSJAcj.7v123prhYG7UgL54.');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (2, 'anca', '$2a$10$r0PaPczi7J8YKL3kOJu4FemQzWM/qUG94po81CxgCBE/pN/hikz2W');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (3, 'aaron', '$2a$10$b3dQzerwzZhvcM9p1xB9H.ugoyjLJwBK1bhPu9TVQp1zmXLgk/UIq');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (4, 'stuart', '$2a$10$axuMX9WrGc4j6q0ifC06K.fv1L7wrfKH5RTrHycp7FqjKJxrKTvde');
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (5, 'morag', '$2a$10$mMbLLrFiRVWTJsON3eHEeea8xCN.FMMvI8Kkjx1/dawnuEAed.wmu');
INSERT INTO "USERS" ("ID", "USERNAME", "APIKEY") VALUES (6, 'medidata', 'JHzhM9EI18flp7l540wtaRz1z3d4689u');

-- Test with susan 5fx/FG1a -- generated at: http://aspirine.org/htpasswd_en.html
-- Note signature patched to $2a -- we'll add this to the core code
INSERT INTO "USERS" ("ID", "USERNAME", "HASH") VALUES (7, 'susan', '$2y$11$gSosE7GOkfL/j8SwYGPnBe0WnRWypxlxlsxWe0towOGLp2mIaLK.6');

CREATE TABLE "ROLES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "STUDY_ID" INTEGER REFERENCES "STUDIES"("ID"),
  "NAME" VARCHAR(64) NOT NULL,
  UNIQUE ("NAME")
);

INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (1, 0, 'ROLE_ADMIN');
INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (2, 1, 'ROLE_DEMO_ADMIN');
INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (3, 1, 'ROLE_DEMO_TRACK');
INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (4, 1, 'ROLE_DEMO_READ');
INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (5, 2, 'ROLE_OTHER_ADMIN');
INSERT INTO "ROLES" ("ID", "STUDY_ID", "NAME") VALUES (6, 2, 'ROLE_OTHER_READ');

CREATE TABLE "USER_ROLES" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "USERNAME" VARCHAR(64) NOT NULL,
  "ROLE_ID" INTEGER NOT NULL REFERENCES "ROLES"("ID")
);

INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('morungos@gmail.com', 1);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('admin', 1);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('stuart', 2);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('anca', 3);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('morag', 4);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('stuartw@ads.uhnresearch.ca', 1);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('oidcprofile#stuartw', 1);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('stuartw@ads.uhnresearch.ca', 5);
INSERT INTO "USER_ROLES" ("USERNAME", "ROLE_ID") VALUES ('morag', 6);

CREATE TABLE "ROLE_PERMISSIONS" (
  "ID" INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  "ROLE_ID" INTEGER NOT NULL REFERENCES "ROLES"("ID"),
  "PERMISSION" VARCHAR(4096) NOT NULL
);

INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (1,  1, '*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (2,  2, '*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (3,  3, 'view');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (4,  3, 'read:track');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (5,  3, 'write:track');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (6,  3, 'attribute:*:*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (7,  4, 'view');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (8,  4, 'read:track');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (9,  4, 'attribute:read:dateEntered,patientId,consentDate,procedureDate,trackerDate,diagnosticsDate,study');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (10, 4, 'attribute:write:dateEntered,patientId,consentDate');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (11, 5, '*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (12, 5, 'attribute:*:*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (13, 6, 'view');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (14, 6, 'write:*');
INSERT INTO "ROLE_PERMISSIONS" ("ID", "ROLE_ID", "PERMISSION") VALUES (15, 6, 'attribute:read:*');
