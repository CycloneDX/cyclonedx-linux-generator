/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.cyclonedx.model.ExternalReference;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.DateUtils;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.Log4JTestWatcher;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.TestUtils;

/**
 * (U) Test cases for the Ubuntu SBom Generator.
 * 
 * @author wrgoff
 * @since 29 Apr 2020
 */
@RunWith(MockitoJUnitRunner.class)
class UbuntuSBomGeneratorTest extends BaseSBomGeneratorTest
{
	private static final String LOG4J_FILE = "UbuntuSBomGeneratorTestLog4J.xml";
	
	private static final String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedUbuntuDetailMap.txt";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	@Mock
	private ProcessBuilder pbMock = new ProcessBuilder();
	
	@InjectMocks
	private UbuntuSBomGenerator generator = new UbuntuSBomGenerator();
	
	/**
	 * (U) This method is used to test the parsing of the Map into External References.
	 */
	@Test
	void buildExternalReferencesTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		try
		{
			List<ExternalReference> expectedRefs = generateExpectedExternalReferences();
			
			Map<String, String> detailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE, watcher);
			
			List<ExternalReference> actualRefs = generator
					.buildExternalReferences(detailMap);
			
			Assert.assertArrayEquals(expectedRefs.toArray(), actualRefs.toArray());
		}
		catch (Exception e)
		{
			String error = "Our Test case buildExternalReferencesTest failed unexpectedly.";
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
	void parseVersionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/version/ubuntuVersion.txt";
		String expected = "7.58.0-2ubuntu3.10";
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(file))))
			{
				String version = generator.parseVersion(reader);
				if (expected.equalsIgnoreCase(version))
					watcher.getLogger().debug("Got expected version (" + version + ")");
				else
					watcher.getLogger().debug("Did NOT get the expected version.  Expected: " +
							expected + ", Got: " + version);
				
				Assert.assertEquals(expected, version);
			}
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case parseVersionTest failed to parse the contents of " +
					"the Ubuntu version (" + file + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parseVersionTest failed to read the Ubuntu " +
					"version file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu Version File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parseVersionTest failed unexpectedly.";
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
	void parseVersionFailureTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/version/ubuntuVersion.txt";
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(file))))
			{
				reader.close();
				generator.parseVersion(reader);
				Assert.fail("Expected an SBomException!");
			}
			catch (SBomException sbomE)
			{
				Assert.assertTrue(sbomE.getMessage().equals("Failed to read version."));
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parseVersionFailureTest failed to read the Ubuntu " +
					"version file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu Version File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parseVersionFailureTest failed unexpectedly.";
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
	void readVersionFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String expected = "7.58.0-2ubuntu3.10";
		String fileName = "/version/ubuntuVersion.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			String version = generator.readVersion(process);
			
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
		
		String expectedMessage = "Unexpected process exit value (-1), while " +
				"attempting to get Installed Software Version!";
		String fileName = "/version/ubuntuVersion.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Exception exception = Assertions.assertThrows(SBomException.class, () ->
			{
				Process process = TestUtils.mockFailedProcess(stream, null, null);
				generator.readVersion(process);
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
	 * (U) This method is used to generate the Expected External References.
	 * 
	 * @return List of ExternalReferences that should be built.
	 */
	private List<ExternalReference> generateExpectedExternalReferences()
	{
		List<ExternalReference> refs = new ArrayList<ExternalReference>();
		
		ExternalReference webpage = new ExternalReference();
		webpage.setUrl("http://www.info-zip.org/Zip.html");
		webpage.setType(ExternalReference.Type.WEBSITE);
		refs.add(webpage);
		
		ExternalReference docs = new ExternalReference();
		docs.setUrl("http://us.archive.ubuntu.com/ubuntu bionic/main amd64 Packages");
		docs.setType(ExternalReference.Type.DOCUMENTATION);
		refs.add(docs);
		
		ExternalReference bugs = new ExternalReference();
		bugs.setUrl("https://bugs.launchpad.net/ubuntu/+filebug");
		bugs.setType(ExternalReference.Type.ISSUE_TRACKER);
		refs.add(bugs);
		
		return refs;
	}
	
	/**
	 * (U) This method is used to test the method to get the Installed Version of a peice of
	 * software.
	 */
	@Test
	void getInstalledVersionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/version/ubuntuVersion.txt";
		String expected = "7.58.0-2ubuntu3.10";
		
		AutoCloseable openMocks = null;
		
		try (InputStream stream = AlpineSBomGeneratorTest.class.getResourceAsStream(file))
		{
			openMocks = MockitoAnnotations.openMocks(this);
			
			Process process = TestUtils.mockProcess(stream, null, null);
			
			Mockito.when(pbMock.start()).thenReturn(process);
			
			String version = generator.getInstalledVersion("curl");
			
			if (expected.equalsIgnoreCase(version))
				watcher.getLogger().debug("Got expected version (" + version + ")");
			else
				Assert.assertEquals(expected, version);
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case testParseVersion failed to parse the contents of " +
					"the Ubuntu version (" + file + ").";
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
