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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Component.Scope;
import org.cyclonedx.model.Component.Type;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Property;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.generator.UnixSBomGenerator.AVAILABLE_LINUX_FLAVORS;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.DateUtils;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.Log4JTestWatcher;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.TestUtils;

/**
 * (U) Test cases for the common Unix SBom Generator.
 * 
 * @author wrgoff
 * @since 27 Apr 2020
 */
class UnixSBomGeneratorTest extends BaseSBomGeneratorTest
{
	private static final String LOG4J_FILE = "UnixSBomGeneratorTestLog4J.xml";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	private UnixSBomGenerator generator = new UnixSBomGenerator();
	
	/**
	 * (U) This test method test to make sure the Unix Property Manager is added to the list of
	 * properties for a Component.
	 */
	@Test
	void addPackageManagerToComponetTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		Component component = new Component();
		component.setType(Type.OPERATING_SYSTEM);;
		component.setName("adduser");
		component.setVersion("3.116ubuntu1");
		component.setScope(Scope.REQUIRED);
		
		String expectedPackageManager = "apt";
		try
		{
			Component newComp = generator.addPackageManager(component,
					expectedPackageManager);
			
			Property unixPropertyManager = getProperty(newComp, "unixPropertyManager");
			
			if ((unixPropertyManager != null) &&
					(expectedPackageManager.equalsIgnoreCase(unixPropertyManager.getValue())))
			{
				watcher.getLogger().debug("Got the expected unix package manager " +
						"property value (" + expectedPackageManager + ").");
			}
			else
			{
				StringBuilder warning = new StringBuilder("Did NOT get the expected " +
						"unix package manager.\n	Expected: " +
						expectedPackageManager + "\n	Actual: ");
				if (unixPropertyManager == null)
					warning.append("null");
				else
					warning.append(unixPropertyManager.getValue());
				watcher.getLogger().warn(warning.toString());
			}
			Assert.assertTrue((unixPropertyManager != null) &&
					(expectedPackageManager.equalsIgnoreCase(unixPropertyManager.getValue())));
		}
		catch (Exception e)
		{
			String error = "Our test for the package manager addition failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method test to make sure the Unix Property Manager is added to the list of
	 * properties for a Component.
	 */
	@Test
	void createComponentsTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/packageDetails/ubuntu.txt";
		
		String expectedComponetName = "zip";
		String expectedComponentVersion = "3.0-11build1";
		String expectedComponentScope = "OPTIONAL";
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(file))))
		{
			Map<String, String> detailMap = generator.generateUbuntuDetailMap(reader);
			
			String version = detailMap.get("Version");
			String group = detailMap.get("Release");
			Component component = generator.createComponents("zip",
					detailMap, null, group, version, null, detailMap.get("Priority"), null);
			
			String actualComponentName = component.getName();
			String actualComponentVerison = component.getVersion();
			String actualComponentScope = component.getScope().toString();
			
			logValues("name", actualComponentName, expectedComponetName);
			logValues("version", actualComponentVerison, expectedComponentVersion);
			logValues("scope", actualComponentScope, expectedComponentScope);
			
			Assert.assertEquals(expectedComponetName, actualComponentName);
			Assert.assertEquals(expectedComponentVersion, actualComponentVerison);
			Assert.assertEquals(expectedComponentScope, actualComponentScope);
		}
		catch (Exception e)
		{
			String error = "Create Components test failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * Convenience method, to log expected versus actual values.
	 * 
	 * @param name          String name of field
	 * @param actualValue   String value of actual value.
	 * @param expectedValue String value of expected value.
	 */
	private void logValues(String name, String actualValue, String expectedValue)
	{
		if (expectedValue.equalsIgnoreCase(actualValue))
			watcher.getLogger().debug("Got expected " + name + " (" + expectedValue + ")");
		else
			watcher.getLogger().debug("Did NOT get the expected " + name + "!\n" +
					"	expected: " + expectedValue + "\n" +
					"	actual: " + actualValue);
	}
	
	/**
	 * (U) This method is used to get a specific property from a components list of properties.
	 * 
	 * @param component    Component to look the property up in.
	 * @param propertyName String value of the propery's name we want.
	 * @return Property the property that matches the name passed in.
	 */
	private Property getProperty(Component component, String propertyName)
	{
		List<Property> props = component.getProperties();
		if ((props != null) && (!props.isEmpty()))
		{
			for (Property prop : props)
			{
				if (prop.getName().equalsIgnoreCase(propertyName))
				{
					return prop;
				}
			}
		}
		return null;
	}
	
	/**
	 * (U) This test is used to generate the IOException that a detail map might throw.
	 */
	@Test
	void alpineDetailMapIOExceptionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String alpineFile = "/packageDetails/alpine.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(alpineFile))))
		{
			reader.close();
			generator.generateAlpineDetailMap(reader);
			Assert.fail("Expected an SBomException!");
		}
		catch (SBomException sbomE)
		{
			Assert.assertTrue(sbomE.getMessage().equals("Unable to process output from unix " +
					"process to get details for a specific piece of software."));
		}
		catch (IOException ioe)
		{
			String error = "Our Test case alpineDetailMapIOExceptionTest failed to throw " +
					"expected SBomException.";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Alpine Detailed Map File (" + alpineFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case alpineDetailMapIOExceptionTest failed unexpectedly.";
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
	void alpineDetialMapTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedAlpineDetailMap.txt";
		Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE,
				watcher);
		
		String file = "/packageDetails/alpine.txt";
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(file))))
		{
			
			Map<String, String> detailMap = generator.generateAlpineDetailMap(reader);
			
			if (expectedDetailMap.equals(detailMap))
			{
				watcher.getLogger().debug("Expected Detail map matches the Acutal detail map!");
			}
			else
			{
				StringBuilder sb = new StringBuilder("Expected Detail map does NOT " +
						"match the detail map!");
				sb.append("	Expected Map: \n");
				sb.append(getDetailMapAsString(expectedDetailMap, "		"));
				sb.append("	Actual Detail Map: \n");
				sb.append(getDetailMapAsString(detailMap, "		"));
					
				watcher.getLogger().info(sb.toString());
			}
			Assert.assertTrue(expectedDetailMap.equals(detailMap));
		}
		catch (Exception e)
		{
			String error = "Our Test case, to generate the Alpine detail map failed " +
					"unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the detail map from a process.
	 */
	@Test
	void alpineDetailMapFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedAlpineDetailMap.txt";
		Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE,
				watcher);
		
		String file = "/packageDetails/alpine.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			Map<String, String> detailMap = generator.processDetailMapCommand(
					process, AVAILABLE_LINUX_FLAVORS.ALPINE);
			
			if (expectedDetailMap.equals(detailMap))
			{
				watcher.getLogger().debug("Expected Detail map matches " +
						"the Actual Detail map!");
			}
			else
			{
				StringBuilder sb = new StringBuilder("Expected Detail map does NOT " +
						"match the detail map!");
				sb.append("	Expected Map: \n");
				sb.append(getDetailMapAsString(expectedDetailMap, "		"));
				sb.append("	Actual Detail Map: \n");
				sb.append(getDetailMapAsString(detailMap, "		"));
				
				watcher.getLogger().error(sb.toString());
			}
			Assert.assertTrue(expectedDetailMap.equals(detailMap));
		}
		catch (Exception e)
		{
			String error = "Our Test case, to generate the Alpine detail map failed " +
					"unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test case is used to test the parsing of the output of the Alpine Command into a
	 * list of packages.
	 */
	@Test
	void alpineSoftwareListTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String alpineFile = "/packageList/alpine.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(alpineFile))))
		{
			List<String> expectedSoftwareList = getPackageList(
					"/packageList/expectedAlpinePackages.txt");
			List<String> actualSoftwareList = generator.processListCmdOutput(reader, '\n',
					null);
			
			Collections.sort(expectedSoftwareList);
			Collections.sort(actualSoftwareList);
			
			Assert.assertArrayEquals("Actual Packages do NOT equal Expected Packages.",
					expectedSoftwareList.toArray(), actualSoftwareList.toArray());
				
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case alpineSoftwareListTest failed to parse the contents of " +
					"the Alpine package file(" + alpineFile + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case alpineSoftwareListTest failed to read the Alpline " +
					"package file(" + alpineFile + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Alpine Package List File (" + alpineFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case alpineSoftwareListTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to get the list of expected packages.
	 * 
	 * @param fileName String value of the name of the file to get the expected packages from.
	 * @return List of Strings that are the expected packages.
	 */
	private List<String> getPackageList(String fileName)
	{
		List<String> expectedPackages = new ArrayList<String>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(fileName))))
		{
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				expectedPackages.add(line.trim());
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case failed to read the Expected package list " +
					"file(" + fileName + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Expected Package List File (" + fileName + "). ");
		}
		return expectedPackages;
	}
	
	/**
	 * (U) This test is used to generate the IOException that a detail map might throw.
	 */
	@Test
	void redhatDetailMapIOExceptionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String redhatFile = "/packageDetails/redhat.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
		{
			reader.close();
			generator.generateRedHatDetailMap(reader);
			Assert.fail("Expected an SBomException!");
		}
		catch (SBomException sbomE)
		{
			Assert.assertTrue(sbomE.getMessage().equals("Unable to process output from unix " +
					"process to get details for a specific piece of software."));
		}
		catch (IOException ioe)
		{
			String error = "Our Test case redhatDetailMapIOExceptionTest failed to throw " +
					"expected SBomException.";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Red Hat Detail Map File (" + redhatFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case redhatDetailMapIOExceptionTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test case tests the code that generates the Detail Map.
	 */
	@Test
	void redhatDetailMapTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedRedhatDetailMap.txt";
		Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE, watcher);
		String redhatFile = "/packageDetails/redhat.txt";
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
		{
			Map<String, String> acutalDetailMap = generator.generateRedHatDetailMap(reader);
			
			if (expectedDetailMap.equals(acutalDetailMap))
			{
				StringBuilder sb = new StringBuilder(
						"Expected Detail map matches the detail map!\n");
				sb.append(getDetailMapAsString(acutalDetailMap, "	"));
				watcher.getLogger().info(sb.toString());
			}
			else
			{
				StringBuilder sb = new StringBuilder(
						"Expected Detail map does NOT match the detail map!");
				sb.append("	Expected Map: \n");
				sb.append(getDetailMapAsString(expectedDetailMap, "		"));
				sb.append("	Actual Detail Map: \n");
				sb.append(getDetailMapAsString(acutalDetailMap, "		"));
				
				watcher.getLogger().info(sb.toString());
			}
			Assert.assertTrue(expectedDetailMap.equals(acutalDetailMap));
		}
		catch (Exception e)
		{
			String error = "Our Test case generateDetailMapTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the detail map from a process.
	 */
	@Test
	void redhatDetailMapFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedRedhatDetailMap.txt";
		Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE, watcher);
		String file = "/packageDetails/redhat.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			Map<String, String> detailMap = generator.processDetailMapCommand(
					process, AVAILABLE_LINUX_FLAVORS.REDHAT);
			
			if (expectedDetailMap.equals(detailMap))
			{
				watcher.getLogger().debug("Expected Detail map matches " +
						"the Actual Detail map!");
			}
			else
			{
				StringBuilder sb = new StringBuilder("Expected Detail map does NOT " +
						"match the detail map!");
				sb.append("	Expected Map: \n");
				sb.append(getDetailMapAsString(expectedDetailMap, "		"));
				sb.append("	Actual Detail Map: \n");
				sb.append(getDetailMapAsString(detailMap, "		"));
				
				watcher.getLogger().error(sb.toString());
			}
			Assert.assertTrue(expectedDetailMap.equals(detailMap));
		}
		catch (Exception e)
		{
			String error = "Our Test case, to generate the RedHat detail map failed " +
					"unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test case is used to test the parsing of the output of the RedHat Command into a
	 * list of packages.
	 */
	@Test
	void redHatSoftwareListTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String redhatFile = "/packageList/redhat.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(redhatFile))))
		{
			List<String> expectedSoftwareList = getPackageList(
					"/packageList/expectedRedhatPackages.txt");
			List<String> actualSoftwareList = generator.processListCmdOutput(reader, ' ',
					"Installed Packages");
			
			Collections.sort(expectedSoftwareList);
			Collections.sort(actualSoftwareList);
			
			Assert.assertArrayEquals("Actual Packages do NOT equal Expected Packages.",
					expectedSoftwareList.toArray(), actualSoftwareList.toArray());
				
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case redHatSoftwareListTest failed to parse the contents of " +
					"the Red Hat package file(" + redhatFile + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case redHatSoftwareListTest failed to read the Red Hat " +
					"package file(" + redhatFile + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Red Hat Package List File (" + redhatFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case redHatSoftwareListTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method is used to test the reading of a String into a LicenseChoice Object.
	 */
	@Test
	void redhatParseLicenseTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/license/redhat/COPYING";
		try
		{
			try (InputStream inputStream = UnixSBomGenerator.class.getResourceAsStream(file))
			{
				String theString = IOUtils.toString(inputStream);
				
				LicenseChoice lc = generator.parseLicenseText(theString,
						UnixSBomGenerator.AVAILABLE_LINUX_FLAVORS.REDHAT);
				Assert.assertNotNull(lc);
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parseLicenseTest failed to read the Ubuntu " +
					"license file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu license File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parseLicenseTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This test is used to generate the IOException that a detail map might throw.
	 */
	@Test
	void ubuntuDetailMapIOExceptionTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String ubuntuFile = "/packageDetails/ubuntu.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(ubuntuFile))))
		{
			reader.close();
			generator.generateUbuntuDetailMap(reader);
			Assert.fail("Expected an SBomException!");
		}
		catch (SBomException sbomE)
		{
			Assert.assertTrue(sbomE.getMessage().equals("Unable to process output from unix " +
					"process to get details for a specific piece of software."));
		}
		catch (IOException ioe)
		{
			String error = "Our Test case ubuntuDetailMapIOExceptionTest failed to throw " +
					"expected SBomException.";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu Detailed Map File (" + ubuntuFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case ubuntuDetailMapIOExceptionTest failed unexpectedly.";
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
	void ubuntuDetailMapTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedUbuntuDetailMap.txt";
		
		String file = "/packageDetails/ubuntu.txt";
		try
		{
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(
					UnixSBomGeneratorTest.class.getResourceAsStream(file))))
			{
				Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE,
						watcher);
				Map<String, String> detailMap = generator.generateUbuntuDetailMap(reader);
				
				if (expectedDetailMap.equals(detailMap))
				{
					StringBuilder sb = new StringBuilder(
							"Expected Detail map matches the detail map!\n");
					sb.append(getDetailMapAsString(detailMap, "	"));
					watcher.getLogger().info(sb.toString());
				}
				else
				{
					StringBuilder sb = new StringBuilder(
							"Expected Detail map does NOT match the detail map!");
					sb.append("	Expected Map: \n");
					sb.append(getDetailMapAsString(expectedDetailMap, "		"));
					sb.append("	Actual Detail Map: \n");
					sb.append(getDetailMapAsString(detailMap, "		"));
					
					watcher.getLogger().info(sb.toString());
				}
				Assert.assertTrue(expectedDetailMap.equals(detailMap));
			}
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case ubuntuDetailMapTest failed to parse the contents of " +
					"the Alpine Package Details Map (" + file + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case ubuntuDetailMapTest failed to read the Ubuntu " +
					"Package Details file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu Package Details File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case ubuntuDetailMapTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the detail map from a process.
	 */
	@Test
	void ubuntuDetailMapFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String EXPECTED_DETAIL_MAP_FILE = "/packageDetails/expectedUbuntuDetailMap.txt";
		
		Map<String, String> expectedDetailMap = getDetailMap(EXPECTED_DETAIL_MAP_FILE,
				watcher);
		
		String file = "/packageDetails/ubuntu.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			Map<String, String> detailMap = generator.processDetailMapCommand(
					process, AVAILABLE_LINUX_FLAVORS.UBUNTU);
			
			if (expectedDetailMap.equals(detailMap))
			{
				watcher.getLogger().debug("Expected Detail map matches " +
						"the Actual Detail map!");
			}
			else
			{
				StringBuilder sb = new StringBuilder("Expected Detail map does NOT " +
						"match the detail map!");
				sb.append("	Expected Map: \n");
				sb.append(getDetailMapAsString(expectedDetailMap, "		"));
				sb.append("	Actual Detail Map: \n");
				sb.append(getDetailMapAsString(detailMap, "		"));
				
				watcher.getLogger().error(sb.toString());
			}
			Assert.assertTrue(expectedDetailMap.equals(detailMap));
		}
		catch (Exception e)
		{
			String error = "Our Test case ubuntuDetailMapTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the reading of a detail map from the process.
	 */
	@Test
	void readVersionFailFromProcessTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String expectedMessage = "Unexpected process exit value (-1), while " +
				"attempting to generate the Detail Map!";
		
		String file = "/packageDetails/ubuntu.txt";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Exception exception = Assertions.assertThrows(SBomException.class, () ->
			{
				Process process = TestUtils.mockFailedProcess(stream, null, null);
				generator.processDetailMapCommand(
						process, AVAILABLE_LINUX_FLAVORS.UBUNTU);
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
			String error = "Our test case to read the detail messgae from a " +
					"failed process, failed unexpectedly!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	@Test
	void ubuntuPackageListTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		String fileName = "/packageList/ubuntu.txt";
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(fileName))
		{
			Process process = TestUtils.mockProcess(stream, null, null);
			
			List<String> actualSoftwareList = generator.processListOfSoftware(process,
					UbuntuSBomGenerator.SOFTWARE_LIST_CMD, '/', "");
			
			List<String> expectedSoftwareList = getPackageList(
					"/packageList/expectedUbuntuPackages.txt");
			
			Collections.sort(expectedSoftwareList);
			Collections.sort(actualSoftwareList);
			
			Assert.assertArrayEquals("Actual Packages do NOT equal Expected Packages.",
					expectedSoftwareList.toArray(), actualSoftwareList.toArray());
		}
		catch (Exception e)
		{
			String error = "Our test case to read the Ubuntu Package List from a process failed!";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test method is used to test the reading of a String into a LicenseChoice Object.
	 */
	@Test
	void ubuntuParseLicenseTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/license/ubuntu/copyright";
		try
		{
			try (InputStream inputStream = UnixSBomGenerator.class.getResourceAsStream(file))
			{
				String theString = IOUtils.toString(inputStream);
				
				LicenseChoice lc = generator.parseLicenseText(theString,
						UnixSBomGenerator.AVAILABLE_LINUX_FLAVORS.UBUNTU);
				Assert.assertNotNull(lc);
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case parseLicenseTest failed to read the Ubuntu " +
					"license file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu license File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case parseLicenseTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This test case is used to test the parsing of the output of the Ubuntu Command into a
	 * list of packages.
	 */
	@Test
	void ubuntuSoftwareListTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		
		String ubuntuFile = "/packageList/ubuntu.txt";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(ubuntuFile))))
		{
			List<String> expectedSoftwareList = getPackageList(
					"/packageList/expectedUbuntuPackages.txt");
			List<String> actualSoftwareList = generator.processListCmdOutput(reader, '/', "");
			
			Collections.sort(expectedSoftwareList);
			Collections.sort(actualSoftwareList);
			
			Assert.assertArrayEquals("Actual Packages do NOT equal Expected Packages.",
					expectedSoftwareList.toArray(), actualSoftwareList.toArray());
			
		}
		catch (SBomException sbomE)
		{
			String error = "Our Test case ubuntuSoftwareListTest failed to parse the contents of " +
					"the Ubuntu package file(" + ubuntuFile + ").";
			watcher.getLogger().error(error, sbomE);
			Assert.fail(error);
		}
		catch (IOException ioe)
		{
			String error = "Our Test case ubuntuSoftwareListTest failed to read the Ubuntu " +
					"package file(" + ubuntuFile + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu Package List File (" + ubuntuFile + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case ubuntuSoftwareListTest failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
