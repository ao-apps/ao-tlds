/*
 * ao-tlds - Self-updating Java API to get top-level domains.
 * Copyright (C) 2016, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.tlds;

import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class TopLevelDomainTest {
	
	public TopLevelDomainTest() {
	}

	/**
	 * Test of getTopLevelDomains method, of class TopLevelDomain.
	 */
	@org.junit.Test
	public void testGetTopLevelDomains() throws InterruptedException {
		System.out.println("getTopLevelDomains");
		List<String> result = TopLevelDomain.getTopLevelDomains();
		System.out.println("\tresult = " + result);
		assertTrue(!result.isEmpty());
		TopLevelDomain.waitUntilNoThread();
	}

	/**
	 * Test of getComments method, of class TopLevelDomain.
	 */
	@org.junit.Test
	public void testGetComments() throws InterruptedException {
		System.out.println("getComments");
		List<String> result = TopLevelDomain.getComments();
		System.out.println("\tresult = " + result);
		assertTrue(!result.isEmpty());
		TopLevelDomain.waitUntilNoThread();
	}

	/**
	 * Test of getLastUpdatedTime method, of class TopLevelDomain.
	 */
	@org.junit.Test
	public void testGetLastUpdatedTime() throws InterruptedException {
		System.out.println("getLastUpdatedTime");
		long result = TopLevelDomain.getLastUpdatedTime();
		System.out.println("\tresult = " + new Date(result));
		assertTrue(result >= 0);
		TopLevelDomain.waitUntilNoThread();
	}

	/**
	 * Test of getLastUpdateSuccessful method, of class TopLevelDomain.
	 */
	@org.junit.Test
	public void testGetLastUpdateSuccessful() throws InterruptedException {
		System.out.println("getLastUpdateSuccessful");
		boolean result = TopLevelDomain.getLastUpdateSuccessful();
		System.out.println("\tresult = " + result);
		assertTrue(result);
		TopLevelDomain.waitUntilNoThread();
	}

	/**
	 * Test of getLastSuccessfulUpdateTime method, of class TopLevelDomain.
	 */
	@org.junit.Test
	public void testGetLastSuccessfulUpdateTime() throws InterruptedException {
		System.out.println("getLastSuccessfulUpdateTime");
		long result = TopLevelDomain.getLastSuccessfulUpdateTime();
		System.out.println("\tresult = " + new Date(result));
		assertTrue(result >= 0);
		TopLevelDomain.waitUntilNoThread();
	}
}
