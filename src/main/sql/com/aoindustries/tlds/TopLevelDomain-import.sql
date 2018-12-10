/*
 * ao-tlds - Self-updating Java API to get top-level domains.
 * Copyright (C) 2018  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-tlds.
 *
 * ao-tlds is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-tlds is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-tlds.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Updates the table to contain the set of top-level domains bundled with this
 * release.  Because this API is designed to be self-updating, however, this
 * bundled list will not be updated very often.
 *
 * It is advised, but beyond the scope of this API, to have a routine task in
 * Java update this table from the current values obtained through the Java API.
 */
BEGIN;

CREATE TEMPORARY TABLE "TopLevelDomain_import" (
  label CITEXT PRIMARY KEY
);

-- Skips comments in tlds-alpha-by-domain.txt during import
\copy "TopLevelDomain_import" from program 'grep -v "^#" tlds-alpha-by-domain.txt'

-- Delete old entries
WITH deleted AS (
  DELETE FROM "com.aoindustries.tlds"."TopLevelDomain" WHERE label NOT IN (
    SELECT label FROM "TopLevelDomain_import"
  ) RETURNING label
)
SELECT COUNT(*) AS deleted FROM deleted \gset 

-- Delete old entries where case changed
WITH delete_for_update AS (
  DELETE FROM "com.aoindustries.tlds"."TopLevelDomain" WHERE label::text NOT IN (
    SELECT label::text FROM "TopLevelDomain_import"
  ) RETURNING label
)
SELECT COUNT(*) AS delete_for_update FROM delete_for_update \gset

-- Add new entries
WITH inserted AS (
  INSERT INTO "com.aoindustries.tlds"."TopLevelDomain"
  SELECT * FROM "TopLevelDomain_import" WHERE label NOT IN (
    SELECT label FROM "com.aoindustries.tlds"."TopLevelDomain"
  ) RETURNING label
)
SELECT COUNT(*) AS inserted FROM inserted \gset

DROP TABLE "TopLevelDomain_import";

-- Gets the comments skipped in tlds-alpha-by-domain.txt during import
\set comments `sed -rn 's/^#[[:space:]]*(.*)$/\1/p' tlds-alpha-by-domain.txt`

-- Add Log entry
INSERT INTO "com.aoindustries.tlds"."TopLevelDomain.Log" VALUES (
  now(),
  TRUE,
  now(),
  :'comments',
  :inserted - :delete_for_update,
  :delete_for_update,
  :deleted
);

COMMIT;

VACUUM FULL ANALYZE "com.aoindustries.tlds"."TopLevelDomain";
