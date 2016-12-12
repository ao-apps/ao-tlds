/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aoindustries.tlds;

import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertTrue;

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
