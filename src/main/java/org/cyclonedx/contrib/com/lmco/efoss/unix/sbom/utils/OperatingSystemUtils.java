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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.StringUtils;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;

import com.google.common.base.CharMatcher;

/**
 * (U) This class is used to determine the operating system.
 * 
 * @author wrgoff
 * @since 22 April 2020
 */
public class OperatingSystemUtils
{
	private static final Logger logger = Logger.getLogger(OperatingSystemUtils.class.getName());
	
	private static final String OS_RELEASE_FILE = "/etc/os-release";
	
	private Map<String, String> osMap = null;

	/**
	 * (U) Base Constructor.
	 * 
	 * @throws SBomException in the event we are unable to read the file.
	 */
	public OperatingSystemUtils()
	{
		osMap = getOs();
	}

	/**
	 * (U) Constructor.
	 * 
	 * @param fileContents String value for the contents of the "/etc/os-release"
	 *                     file.
	 */
	public OperatingSystemUtils(String fileContents)
	{
		osMap = readOs(fileContents);
	}
	
	/**
	 * (U) This method is used to get the operating System.
	 * 
	 * @return String the operating system.
	 */
	public String getOsVendor()
	{
		String osVendor = osMap.get("NAME");

		if (StringUtils.isValid(osVendor))
			osVendor = CharMatcher.is('\"').trimFrom(osVendor);
		else
		{
			logger.warn("Unable to determine OS.  Assuming a flavor of Redhat.!");
			osVendor = "REDHAT";
		}
		return osVendor;
	}
	
	/**
	 * (U) This method is used to get the operating System Name.
	 * 
	 * @return String the operating system name.
	 */
	public String getOsName()
	{
		String osName = null;

		if (osMap.containsKey("ID"))
			osName = osMap.get("ID");
		else
			osName = osMap.get("NAME");

		osName = osName.replace('"', ' ').trim();

		return osName;
	}

	/**
	 * (U) This method is used to get the operating System Version.
	 * 
	 * @return String the operating system version.
	 */
	public String getOsVersion()
	{
		String version = null;

		if (osMap.containsKey("VERSION_ID"))
			version = osMap.get("VERSION_ID");
		else
			version = osMap.get("VERSION");

		version = version.replace('"', ' ').trim();

		return version;
	}

	/**
	 * (U) This method is used to get the operating system. From the /etc/os-release
	 * file.
	 * 
	 * @return Map containing the information from the os-release file.
	 * @throws SBomException in the event we are unable to read the file.
	 */
	public Map<String, String> getOs()
	{
		Map<String, String> detailMap = new HashMap<>();
		
		try
		{
			String content = FileUtils.readFileToString(new File(OS_RELEASE_FILE));
			
			detailMap = readOs(content);
		}
		catch (IOException ioe)
		{
			String error = "Unable to read file(" + OS_RELEASE_FILE + ") to get the " +
					"operating sytem!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return detailMap;
	}
	
	/**
	 * (U) This method is used to read the contents of the OS file.
	 * 
	 * @param content String value read from the file.
	 * @return Map containing the information about the Operating system.
	 */
	public static Map<String, String> readOs(String content)
	{
		Map<String, String> detailMap = new HashMap<>();
		
		String[] contents = content.split("\n");
		String componentDetailName = null;
		StringBuilder componentDetailValue = new StringBuilder();
		int index = 0;
		
		for (String line : contents)
		{
			if (line.startsWith(" "))
			{
				componentDetailValue.append(line);
			}
			else
			{
				if (componentDetailName != null)
				{
					detailMap.put(componentDetailName, componentDetailValue.toString());
					componentDetailName = null;
					componentDetailValue = new StringBuilder();
				}
				
				if (line.contains("="))
				{
					index = line.indexOf('=');
					componentDetailName = line.substring(0, index);
					componentDetailValue.append(line.substring(index + 1).trim());
				}
			}
		}
		return detailMap;
	}
}
