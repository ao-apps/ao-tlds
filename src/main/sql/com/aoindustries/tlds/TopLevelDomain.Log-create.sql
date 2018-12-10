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

CREATE TABLE "com.aoindustries.tlds"."TopLevelDomain.Log" (
  "lastUpdatedTime" timestamp with time zone
    PRIMARY KEY,
  "lastUpdatedSuccessful" boolean
    NOT NULL,
  "lastSuccessfulUpdateTime" timestamp with time zone
    NOT NULL,
  comments text
    NOT NULL,
  inserted integer
    NOT NULL,
  updated  integer
    NOT NULL,
  deleted  integer
    NOT NULL
);

COMMENT ON TABLE "com.aoindustries.tlds"."TopLevelDomain.Log" IS
'The auto-update history of the TopLevelDomain table';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log".comments IS
'The comments, joined by newlines when there is more than one comment';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log"."lastUpdatedTime" IS
'The last time the list was updated, whether successful or not';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log"."lastUpdatedSuccessful" IS
'Whether the last update was successful';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log"."lastSuccessfulUpdateTime" IS
'The last time the list was successfully updated';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log".inserted IS
'The number of new top-level domains added';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log".updated IS
'The number of top-level domains updated, which will only occur when
the case is changed';

COMMENT ON COLUMN "com.aoindustries.tlds"."TopLevelDomain.Log".deleted IS
'The number of old top-level domains removed';
