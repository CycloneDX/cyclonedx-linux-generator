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

import com.github.packageurl.PackageURL;
import org.cyclonedx.model.ExternalReference;
import org.junit.Assert;
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
 * (U) Test cases for the RedHat SBom Generator.
 * 
 * @author wrgoff
 * @since 27 Apr 2020
 */
@RunWith(MockitoJUnitRunner.class)
class RedHatSBomGeneratorTest extends BaseSBomGeneratorTest
{
	private static final String LOG4J_FILE = "RedHatSBomGeneratorTestLog4J.xml";
	
	private static final String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedRedhatDetailMap.txt";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	@InjectMocks
	private RedHatSBomGenerator generator = new RedHatSBomGenerator();
	
	@Mock
	private ProcessBuilder pbMock = new ProcessBuilder();
	
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
		
		return refs;
	}
	
	@Test
	void getPurlTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());

		String expectedUrl = "https://rhui3.us-west-2.aws.ce.redhat.com/pulp/repos/content/dist/rhel/rhui/server/7/7Server/x86_64/os/Packages/z/zip-3.0-11.el7.x86_64.rpm";
		String expected = "pkg:rpm/rhel/zip@3.0-11.el7?arch=x64_86";

		AutoCloseable openMocks = null;
		
		String fileName = "/purl/redhatPurl.txt";
		
		try (InputStream stream = RedHatSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			openMocks = MockitoAnnotations.openMocks(this);
			
			Mockito.when(pbMock.start()).thenReturn(process);
			
			PackageURL purl = generator.getPurl("zip.x64_86", "3.0-11.el7");
			
			if (expected.equalsIgnoreCase(purl.toString()))
				watcher.getLogger().debug("Got expected version (" + purl + ")");
			else
				Assert.assertEquals(expected, purl.toString());
		}
		catch (Exception e)
		{
			String error = "Our Test case getPurlTest failed unexpectedly.";
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
	
	/**
	 * (U) This method is used to test the reading of a Purl from the process.
	 */
	@Test
	void parsePurlProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String expected = "https://rhui3.us-west-2.aws.ce.redhat.com/pulp/repos/content/dist/rhel/rhui/server/7/7Server/x86_64/os/Packages/z/zip-3.0-11.el7.x86_64.rpm";
		String fileName = "/purl/redhatPurl.txt";
		
		try (InputStream stream = RedHatSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			String purl = generator.parsePurl(process, "zip");
			
			if (expected.equalsIgnoreCase(purl))
				watcher.getLogger().debug("Got expected purl (" + purl + ")");
			else
				watcher.getLogger().debug("Did NOT get the expected Purl.\n" +
						"	Expected: " + expected + "\n" +
						"	Actual: " + purl);
			
			Assert.assertEquals(expected, purl);
		}
		catch (Exception e)
		{
			String error = "Our test case to read the purl from a process failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method is used to test the parsing of the PURL, if an NO URL is found.
	 */
	@Test
	void parseBadPurlCmdOutput()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String redhatFile = "/purl/redhatPurl2.txt";
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
			{
				String actualPurl = generator.parsePurlCmdOutput(reader);
				if (actualPurl != null)
					Assert.fail("Expected a null, but got " + actualPurl);
			}
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case parsePurlCmdOutput failed to parse the contents of " +
					"the Red Hat package PURL (" + redhatFile + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parsePurlCmdOutput failed to read the Red Hat " +
					"package PURL file(" + redhatFile + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Red Hat Package PURL File (" + redhatFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parsePurlCmdOutput failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method is used to test the parsing of the PURL, if an actual URL is found.
	 */
	@Test
	void parsePurlCmdOutput()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String redhatFile = "/purl/redhatPurl.txt";
		try
		{
			String expectedPurl = "https://rhui3.us-west-2.aws.ce.redhat.com/pulp/repos/content/dist/rhel/rhui/server/7/7Server/x86_64/os/Packages/z/zip-3.0-11.el7.x86_64.rpm";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
			{
				String actualPurl = generator.parsePurlCmdOutput(reader);
				
				Assert.assertTrue(actualPurl.equals(expectedPurl));
			}
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case parsePurlCmdOutput failed to parse the contents of " +
					"the Red Hat package PURL (" + redhatFile + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parsePurlCmdOutput failed to read the Red Hat " +
					"package PURL file(" + redhatFile + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Red Hat Package PURL File (" + redhatFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parsePurlCmdOutput failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method is used to test the parsing of the PURL, if an NO URL is found.
	 */
	@Test
	void testPurlCmdIOException()
	{
		String redhatFile = "/purl/redhatPurl2.txt";
		
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
		{
			reader.close();
			generator.parsePurlCmdOutput(reader);
			Assert.fail("Expected an SBomException!");
		}
		catch (SBomException sbomE)
		{
			Assert.assertTrue(sbomE.getMessage().equals("Unable to process output from unix " +
					"process to get the Package download URL."));
		}
		catch (IOException ioe)
		{
			String error = "Our Test case testPurlCmdIOException failed to throw " +
					"expected SBomException.";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Red Hat Package PURL File (" + redhatFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case testPurlCmdIOException failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
