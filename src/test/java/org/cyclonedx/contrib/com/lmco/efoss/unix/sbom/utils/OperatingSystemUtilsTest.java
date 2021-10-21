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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.CharMatcher;

/**
 * (U) Test cases for the Operating System Utilities.
 * 
 * @author wrgoff
 * @since 15 October 2020
 */
@RunWith(MockitoJUnitRunner.class)
class OperatingSystemUtilsTest
{
	private static final String LOG4J_FILE = "OperatingSystemUtilsTestLog4J.xml";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Alpine.
	 */
	@Test
	void getOsNameAlpine()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/alpine-os-release.txt";
		String expectedOsName = "alpine";
		try
		{
			testOsName(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Centos.
	 */
	@Test
	void getOsNameCentos()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/centos-os-release.txt";
		String expectedOsName = "centos";
		try
		{
			testOsName(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Debian.
	 */
	@Test
	void getOsNameDebian()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/debian-os-release.txt";
		String expectedOsName = "debian";
		try
		{
			testOsName(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Redhat.
	 */
	@Test
	void getOsNameRedhat()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/redhat-os-release.txt";
		String expectedOsName = "rhel";
		try
		{
			testOsName(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Ubuntu.
	 */
	@Test
	void getOsNameUbuntu()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/ubuntu-os-release.txt";
		String expectedOsName = "ubuntu";
		try
		{
			testOsName(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS name from the
	 * os-release file. For Alpine.
	 */
	@Test
	void getOsVenderAlpine()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/alpine-os-release.txt";
		String expectedOsVender = "Alpine Linux";
		try
		{
			testOsVender(file, expectedOsVender);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS Vender from the
	 * os-release file. For Centos.
	 */
	@Test
	void getOsVenderCentos()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/centos-os-release.txt";
		String expectedOsName = "CentOS Linux";
		try
		{
			testOsVender(file, expectedOsName);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS vender from the
	 * os-release file. For Debian.
	 */
	@Test
	void getOsVenderDebian()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/debian-os-release.txt";
		String expectedOsVender = "Debian GNU/Linux";
		try
		{
			testOsVender(file, expectedOsVender);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS vender from the
	 * os-release file. For Redhat.
	 */
	@Test
	void getOsVenderRedhat()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/redhat-os-release.txt";
		String expectedOsVender = "Red Hat Enterprise Linux";
		try
		{
			testOsVender(file, expectedOsVender);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS vender from the
	 * os-release file. For Ubuntu.
	 */
	@Test
	void getOsVenderUbuntu()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/ubuntu-os-release.txt";
		String expectedOsVender = "Ubuntu";
		try
		{
			testOsVender(file, expectedOsVender);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS Version from the
	 * os-release file. For Alpine.
	 */
	@Test
	void getOsVersionAlpine()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/alpine-os-release.txt";
		String expectedOsVersion = "3.14.2";
		try
		{
			testOsVersion(file, expectedOsVersion);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS Version from the
	 * os-release file. For Centos.
	 */
	@Test
	void getOsVersionCentos()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/centos-os-release.txt";
		String expectedOsVersion = "8";
		try
		{
			testOsVersion(file, expectedOsVersion);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS Version from the
	 * os-release file. For Debian.
	 */
	@Test
	void getOsVersionDebian()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/debian-os-release.txt";
		String expectedOsVersion = "10";
		try
		{
			testOsVersion(file, expectedOsVersion);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS version from the
	 * os-release file. For Redhat.
	 */
	@Test
	void getOsVersionRedhat()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/redhat-os-release.txt";
		String expectedOsVersion = "8.1";
		try
		{
			testOsVersion(file, expectedOsVersion);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) This method is used to test the getting of the OS version from the
	 * os-release file. For Ubuntu.
	 */
	@Test
	void getOsVersionUbuntu()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on

		Date startDate = DateUtils.rightNowDate();

		TestUtils.logTestStart(methodName, watcher.getLogger());

		String file = "/osReleaseFiles/ubuntu-os-release.txt";
		String expectedOsVersion = "20.04";
		try
		{
			testOsVersion(file, expectedOsVersion);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}

	/**
	 * (U) Convenience method used to test the reading of the OS Name from the
	 * "/etc/os-release" file.
	 * 
	 * @param file           String value of the contents of the "/etc/os-release"
	 *                       file.
	 * @param expectedOsName String value of the expected OS Name.
	 */
	void testOsName(String file, String expectedOsName)
	{
		try (InputStream inputStream = OperatingSystemUtilsTest.class.getResourceAsStream(file))
		{
			String osReleaseFileContents = IOUtils.toString(inputStream);

			OperatingSystemUtils osUtils = new OperatingSystemUtils(osReleaseFileContents);

			String actualOsName = osUtils.getOsName();

			if (expectedOsName.equalsIgnoreCase(actualOsName))
				watcher.getLogger().debug("Got expected operating system name (" +
								expectedOsName + ").");
			else
				watcher.getLogger().debug("Did NOT get expected operating system name!\n" +
								"	Expected: " + expectedOsName + "\n" +
								"	Acutal: " + actualOsName);

			Assert.assertEquals(expectedOsName, actualOsName);
		}
		catch (IOException ioe)
		{
			String error = "Our test case failed to read the operating system " +
							"etc/os-release file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read /etc/os-release File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our test case failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
	}

	/**
	 * (U) Convenience method used to test the reading of the OS Vender from the
	 * "/etc/os-release" file.
	 * 
	 * @param file             String value of the contents of the "/etc/os-release"
	 *                         file.
	 * @param expectedOsVender String value of the expected OS Name.
	 */
	void testOsVender(String file, String expectedOsVender)
	{
		try (InputStream inputStream = OperatingSystemUtilsTest.class.getResourceAsStream(file))
		{
			String osReleaseFileContents = IOUtils.toString(inputStream);

			OperatingSystemUtils osUtils = new OperatingSystemUtils(osReleaseFileContents);

			String actualOsVender = osUtils.getOsVendor();

			if (expectedOsVender.equalsIgnoreCase(actualOsVender))
				watcher.getLogger().debug("Got expected operating system vender (" +
								expectedOsVender + ").");
			else
				watcher.getLogger().debug("Did NOT get expected operating system vender!\n" +
								"	Expected: " + expectedOsVender + "\n" +
								"	Acutal: " + actualOsVender);

			Assert.assertEquals(expectedOsVender, actualOsVender);
		}
		catch (IOException ioe)
		{
			String error = "Our test case failed to read the operating system vender " +
							"etc/os-release file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read /etc/os-release File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our test case failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
	}

	/**
	 * (U) Convenience method used to test the reading of the OS Version from the
	 * "/etc/os-release" file.
	 * 
	 * @param file              String value of the contents of the
	 *                          "/etc/os-release" file.
	 * @param expectedOsVersion String value of the expected OS Version.
	 */
	void testOsVersion(String file, String expectedOsVersion)
	{
		try (InputStream inputStream = OperatingSystemUtilsTest.class.getResourceAsStream(file))
		{
			String osReleaseFileContents = IOUtils.toString(inputStream);

			OperatingSystemUtils osUtils = new OperatingSystemUtils(osReleaseFileContents);

			String actualOsVersion = osUtils.getOsVersion();

			if (expectedOsVersion.equalsIgnoreCase(actualOsVersion))
				watcher.getLogger().debug("Got expected operating system version (" +
								expectedOsVersion + ").");
			else
				watcher.getLogger().debug("Did NOT get expected operating system version!\n" +
								"	Expected: " + expectedOsVersion + "\n" +
								"	Acutal: " + actualOsVersion);

			Assert.assertEquals(expectedOsVersion, actualOsVersion);
		}
		catch (IOException ioe)
		{
			String error = "Our test case failed to read the operating system " +
							"etc/os-release file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read /etc/os-release File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our test case failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
	}

	/**
	 * (U) This method is used to test the parsing of the Ubuntu Os File.
	 */
	@Test
	void testReadOsUbuntu()
	{
		// @formatter:off
		String methodName = new Object(){}.getClass().getEnclosingMethod().getName();
		// @formatter:on
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/operatingSystem/ubuntu.txt";
		String expected = "Ubuntu";
		try
		{
			try (InputStream inputStream = OperatingSystemUtilsTest.class.getResourceAsStream(file))
			{
				String theString = IOUtils.toString(inputStream); 
				
				OperatingSystemUtils osUtils = new OperatingSystemUtils();
				Map<String, String> osMap = osUtils.readOs(theString);
				
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
			String error = "Our Test case " + methodName + " failed to read the " +
							"Ubuntu operating system file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Ubuntu operating system File (" + file + "). ");
		}
		catch (Exception e)
		{
			String error = "Our Test case " + methodName + " failed unexpectedly.";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
