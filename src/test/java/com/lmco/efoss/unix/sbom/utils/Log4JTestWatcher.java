/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
/**
 * 
 */
package com.lmco.efoss.unix.sbom.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 * @author wrgoff
 * @since 27 Apr 2020
 */
public class Log4JTestWatcher extends TestWatcher
{
	private static final String JUNIT_LOGFILE_DIR = "./src/test/resources/logging/";
	
	private Logger logger;
	
	/**
	 * (U) This constructor constructs a single class with a handle to the log file for the caller.
	 *
	 * @param fileName   String value of the file name to get the log4j configuration from.
	 * @param loggerName String value of the logger's name.
	 */
	public Log4JTestWatcher(String fileName, String loggerName)
	{
		// Load log4J config.
		DOMConfigurator.configure(JUNIT_LOGFILE_DIR + fileName);
		
		// super(fileName, loggerName);
		logger = LogManager.getLogger(loggerName);
	}
	
	/**
	 * (U) This method gets a handle to our logger.
	 *
	 * @return Logger our logger to log message to.
	 */
	public Logger getLogger()
	{
		return logger;
	}
	
	/**
	 * (U) This method overrides JUnit, so Failed messages go to our logs.
	 *
	 * @param e           Throwable to log.
	 * @param description Description object of what failed.
	 */
	@Override
	protected void failed(final Throwable e, final Description description)
	{
		logger.error("*******Failed: " + description, e);
	}
	
	/**
	 * (U) This method overrides JUnit, so finished messages go to our logs.
	 *
	 * @param description Description object of what has finished.
	 */
	@Override
	protected void finished(Description description)
	{
//		logger.info("Finished Test: " + description.getMethodName() + "\n\n");
	}
	
	/**
	 * (U) This method overrides JUnit, so starting messages go to our logs.
	 *
	 * @param description Description object of what was started.
	 */
	@Override
	protected void starting(Description description)
	{
//		logger.info("Starting Test: " + description.getMethodName());
	}
	
	/**
	 * (U) This method overrides JUnit, so Succeed messages go to our logs.
	 *
	 * @param description Description object of what succeeded.
	 */
	@Override
	protected void succeeded(final Description description)
	{
		// logger.debug ("Success: " + description);
	}
}
