/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package com.lmco.efoss.unix.sbom.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.lmco.efoss.unix.sbom.exceptions.SBomException;
import com.lmco.efoss.unix.sbom.utils.DateUtils;
import com.lmco.efoss.unix.sbom.utils.Log4JTestWatcher;
import com.lmco.efoss.unix.sbom.utils.TestUtils;


/**
 * (U) Test cases for the Alpine Linux SBom Generator.
 * 
 * @author wrgoff
 * @since 6 October 2020
 */
@RunWith(MockitoJUnitRunner.class)
class AlpineSBomGeneratorTest extends BaseSBomGeneratorTest
{
	private static final String LOG4J_FILE = "AlpineSBomGeneratorTestLog4J.xml";
		
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	@Mock
	private ProcessBuilder pbMock = new ProcessBuilder();
	
	@InjectMocks
	private AlpineSBomGenerator generator = new AlpineSBomGenerator();
	
	/**
	 * (U) This method is used to test the reading of a version from the process.
	 */
	@Test
	void readVersionFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String expected = "1.1.24-r8";
		String fileName = "/version/alpineVersion.txt";
		
		try (InputStream stream = AlpineSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			String cmd = "apk policy musl";
			
			String version = generator.parseVersion(process, cmd);
			
			if (expected.equalsIgnoreCase(version))
				watcher.getLogger().debug("Got expected version (" + version + ")");
			else
				watcher.getLogger().debug("Did NOT get the expected version.  Expected: " +
						expected + ", Got: " + version);
			
			Assert.assertEquals(expected, version);
		}
		catch (Exception e)
		{
			String error = "Our test case to read the version from a process failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the reading of a version from the process.
	 */
	@Test
	void readVersionFailFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String cmd = "apk policy musl";
		
		String expectedMessage = "Unexpected process exit value (-1), while " +
				"attempting to get Software Version (" + cmd + ")!";
		
		String fileName = "/version/alpineVersion.txt";
		
		try (InputStream stream = AlpineSBomGenerator.class.getResourceAsStream(fileName))
		{
			Exception exception = Assertions.assertThrows(SBomException.class, () ->
			{
				Process process = TestUtils.mockFailedProcess(stream, null, null);
				generator.parseVersion(process, cmd);
			});
			String actualMessage = exception.getMessage();
			
			if (expectedMessage.equals(actualMessage))
				watcher.getLogger().debug("Got expected exception message for a failed process!");
			else
				watcher.getLogger().debug("Did NOT get the expected exception " +
						"message for a failed process!" +
						"\n	Expected Message: " + expectedMessage +
						"\n	Actual Message: " + actualMessage);
			
			Assert.assertTrue(expectedMessage.equals(actualMessage));
		}
		catch (Exception e)
		{
			String error = "Our test case to read the version from a process failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the version.
	 */
	@Test
	void testParseVersion()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/version/alpineVersion.txt";
		String expected = "1.1.24-r8";
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(file))))
			{
				String version = generator.parseVersion(reader);
				if(expected.equalsIgnoreCase(version))
					watcher.getLogger().debug("Got expected version (" + version + ")");
				else
					Assert.assertEquals(expected, version);
			}
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case testParseVersion failed to parse the contents of " +
					"the Alpine version (" + file + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case testParseVersion failed to read the Alpine " +
					"version file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Alpine Version File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case testParseVersion failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the version.
	 */
	@Test
	void getVersionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/version/alpineVersion.txt";
		String expected = "1.1.24-r8";
		
		AutoCloseable openMocks = null;
		
		try (InputStream stream = AlpineSBomGeneratorTest.class.getResourceAsStream(file))
		{
			openMocks = MockitoAnnotations.openMocks(this);
			
			Process process = TestUtils.mockProcess(stream, null, null);
			
			Mockito.when(pbMock.start()).thenReturn(process);
			
			String version = generator.getVersion("musl");
			
			if (expected.equalsIgnoreCase(version))
				watcher.getLogger().debug("Got expected version (" + version + ")");
			else
				Assert.assertEquals(expected, version);
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case testParseVersion failed to parse the contents of " +
					"the Alpine version (" + file + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case testParseVersion failed to read the Alpine " +
					"version file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Alpine Version File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case testParseVersion failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			try
			{
				openMocks.close();
			}
			catch (Exception e)
			{
				watcher.getLogger().warn("Unable to close Mockito Annotations!");
			}
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
