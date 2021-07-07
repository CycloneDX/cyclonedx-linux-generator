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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;

/**
 * (U) Unit tests for our Date Utils Class.
 * 
 * @author wrgoff
 * @since 15 October 2020
 */
public class DateUtilsTest
{
	private static final String LOG4J_FILE = "DateUtilsAppender.xml";
		
	@ClassRule
	public static Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, "DateUtilsTest");
		
	/**
	 * (U) Compute Diff between the same date. Should be 0 milliseconds.
	 */
	@Test
	public void zeroDiffTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
			
		Date startDate = DateUtils.rightNowDate();
			
		TestUtils.logTestStart(methodName, watcher.getLogger());
			
		String same = "0 MILLISECONDS";
		try
		{
			String diffString = DateUtils.computeDiff(startDate, startDate);
				
			if (Objects.equals(diffString, same))
				watcher.getLogger().debug("Diff of two dates are Equal (" + same + "), as expected.");
			else
				watcher.getLogger().debug("Diff of two dates are not what is expected, \n" +
						"	Expected : " + same + "\n	But Got: " + diffString);
			
			Assert.assertTrue(diffString.equals("0 MILLISECONDS"));
				
		}
		catch (Exception e)
		{
			String error = "Unexpected error occured while attempt to compare " +
					"two date Strings set to the same time.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) Test the Creation of a String from a date.
	 */
	@Test
	public void toStringTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
			
		Date startDate = DateUtils.rightNowDate();
			
		TestUtils.logTestStart(methodName, watcher.getLogger());
			
		String expectedString = "2020 08 10 10:15:55.000";
			
		try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a",
					Locale.ENGLISH);

			String dateInString = "10-08-2020 10:15:55 AM";
			Date date = formatter.parse(dateInString);
				
			String dateString = DateUtils.toString(date);
				
			if (dateString.equalsIgnoreCase("2020 08 10 10:15:55.000"))
				watcher.getLogger().debug("Got expected String from date (" + expectedString + ")");
			else
				watcher.getLogger()
						.warn("Expected (" + expectedString + "), but got: " + dateString);
				
			Assert.assertTrue(dateString.equalsIgnoreCase("2020 08 10 10:15:55.000"));
		}
		catch (Exception e)
		{
			String error = "Unexpected error occured while attempt create and compare a String " +
					"from a date!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}