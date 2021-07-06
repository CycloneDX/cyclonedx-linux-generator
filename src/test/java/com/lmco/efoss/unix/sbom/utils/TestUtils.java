/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package com.lmco.efoss.unix.sbom.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * (U) This class contains utilities used by the various test classes.
 * 
 * @author wrgoff
 * @since 9 July 2020
 */
public class TestUtils
{
	/**
	 * (U) This method is used to log the starting of a test.
	 * 
	 * @param methodName String value of the method that test was started for.
	 * @param logger     Logger to log the event to.
	 * @return Date the java.util.Date indicating when the test started.
	 */
	public static Date logTestStart(String methodName, Logger logger)
	{
		Date startDate = DateUtils.rightNowDate();
		
		String msg = "Starting " + methodName + " Test (" +
				DateUtils.dateAsPrettyString(startDate) + ").";
		
		if (logger.isInfoEnabled())
			logger.info(msg);
		
		System.out.println(msg);
		
		return startDate;
	}
	
	/**
	 * (U) This method is used to log the finishing of a test.
	 * 
	 * @param methodName String value of the method the test was for.
	 * @param startDate  Date the test started.
	 * @param logger     Logger to log the event to.
	 */
	public static void logTestFinish(String methodName, Date startDate, Logger logger)
	{
		if (logger.isInfoEnabled())
			logger.info("It took " + DateUtils.computeDiff(startDate, DateUtils.rightNowDate()) +
					" to run the " + methodName + " Test!\n\n");
	}
	
	/**
	 * (U) This method is used to create a Mock Process to use for testing.
	 * 
	 * @param outputStream InputStream to assign to the input stream of the process.
	 * @param errorStream  InputStream to assign to the Error Stream.
	 * @param inputStream  OutputStream to assign to the input steam.
	 * @return Process mocked process to use for testing.
	 * @throws InterruptedException in the event something goes wrong.
	 */
	public static Process mockProcess(InputStream outputStream,
			InputStream errorStream, OutputStream inputStream)
			throws InterruptedException
	{
		final Process subProcess = mock(Process.class);
		when(subProcess.waitFor()).thenReturn(0);
		when(subProcess.getInputStream()).thenReturn(outputStream);
		when(subProcess.getErrorStream()).thenReturn(errorStream);
		when(subProcess.getOutputStream()).thenReturn(inputStream);
		return subProcess;
	}
	
	/**
	 * (U) This method is used to create a Mock Process to use for testing.
	 * 
	 * @param outputStream InputStream to assign to the input stream of the process.
	 * @param errorStream  InputStream to assign to the Error Stream.
	 * @param inputStream  OutputStream to assign to the input steam.
	 * @return Process mocked process to use for testing.
	 * @throws InterruptedException in the event something goes wrong.
	 */
	public static Process mockFailedProcess(InputStream outputStream,
			InputStream errorStream, OutputStream inputStream)
			throws InterruptedException
	{
		final Process subProcess = mock(Process.class);
		when(subProcess.waitFor()).thenReturn(-1);
		when(subProcess.getInputStream()).thenReturn(outputStream);
		when(subProcess.getErrorStream()).thenReturn(errorStream);
		when(subProcess.getOutputStream()).thenReturn(inputStream);
		return subProcess;
	}
	
}
