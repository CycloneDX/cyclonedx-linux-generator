/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package org.cyclonedx.contrib.com.lmco.efoss.unix.sbom;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.test.utils.Log4JTestWatcher;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.test.utils.TestUtils;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.DateUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

class UnixSbomGeneratorApplicationTests {

	private static final String LOG4J_FILE = "UnixSbomGeneratorApplicationAppender.xml";

	@Rule
	public static Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE,
					"UnixSbomGeneratorApplicationTests");

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

	/**
	 * Turns on stdOut output capture
	 */
	private void captureOut()
	{
		System.setOut(new PrintStream(outContent));
	}

	/**
	 * Turns off stdOut capture and returns the contents that have been captured
	 *
	 * @return String the contents of our stdOut.
	 */
	private String getOut()
	{
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
		return outContent.toString().replaceAll("\r", "");
	}

	/**
	 * (U) This method is used to test to make sure our help menu works. It does NOT
	 * check the contents, but check to make sure it begins "usage: help".
	 */
	@Test
	public void testHelp()
	{
		String methodName = new Object()
		{
		}.getClass().getEnclosingMethod().getName();

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String helpUsageText = "usage: help";

		try
		{
			captureOut();

			String[] args = new String[] {
				"-h"
			};
			UnixSbomGeneratorApplication.main(args);

			String theOutput = getOut();

			watcher.getLogger().debug("Help Test: \n" + theOutput);

			Assert.assertTrue(theOutput.contains(helpUsageText));
		}
		catch (Exception e)
		{
			String error = "Unexpected error occured while attempting to check to make sure our " +
							"help menu works!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
