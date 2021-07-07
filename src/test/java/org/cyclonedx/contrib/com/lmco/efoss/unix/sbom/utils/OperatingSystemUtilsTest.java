/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.google.common.base.CharMatcher;

/**
 * (U) Test cases for the Operating System Utilities.
 * 
 * @author wrgoff
 * @since 15 October 2020
 */
class OperatingSystemUtilsTest
{
	private static final String LOG4J_FILE = "OperatingSystemUtilsTestLog4J.xml";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	/**
	 * (U) This method is used to test the parsing of the Ubuntu Os File.
	 */
	@Test
	void testReadOsUbuntu()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/operatingSystem/ubuntu.txt";
		String expected = "Ubuntu";
		try
		{
			try (InputStream inputStream = OperatingSystemUtilsTest.class.getResourceAsStream(file))
			{
				String theString = IOUtils.toString(inputStream); 
				
				Map<String, String> osMap = OperatingSystemUtils.readOs(theString);
				
				String osVendor = osMap.get("NAME");
				osVendor = CharMatcher.is('\"').trimFrom(osVendor);
				
				if (expected.equalsIgnoreCase(osVendor))
					watcher.getLogger().debug("Got expected operating system (Ubuntu)");
				else
					watcher.getLogger().debug("Did NOT get expected operating system!  " +
							"Expected Ubuntu, got " + osVendor);
				Assert.assertEquals(expected, osVendor);
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case testReadOsUbuntu failed to read the Ubuntu operating " +
					"system file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu operating system File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case testReadOsUbuntu failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
