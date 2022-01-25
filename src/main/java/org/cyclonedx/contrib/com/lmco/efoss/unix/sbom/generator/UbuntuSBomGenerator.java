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
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.LicenseChoice;

import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;

/**
 * (U) This class is responsible for generating the Software Bill Of Materials (SBOM) for all Ubuntu
 * Linux Operating Systems.
 * 
 * @author wrgoff
 * @since 23 April 2020
 */
public class UbuntuSBomGenerator extends UnixSBomGenerator
{
	private static final String PACKAGE_MANAGER = "apt";
	
	private static final String SOFTWARE_INSTALLED_VERSION = "apt policy";
	private static final String SOFTWARE_DETAIL_CMD = "apt show";
	public static final String SOFTWARE_LIST_CMD = "apt list --installed";
	
	private ProcessBuilder processBuilder = new ProcessBuilder();
	
	/**
	 * (U) This method is used to generate the Software Bill Of Materials (SBOM) for all Ubuntu
	 * Linux Operating systems.
	 * 
	 * @return Bom The Software Bill Of Materials for this Ubuntu Linux Operating System.
	 * @throws SBomException if we are unable to build the SBOM.
	 */
	public Bom generateSBom()
	{
		List<String> softwareList = generateListOfSoftware(SOFTWARE_LIST_CMD, '/',
				"");

		Bom bom = new Bom();
		
		if (logger.isDebugEnabled())
			logger.debug("Processing " + softwareList.size() + " software programs.");
		
		Map<String, String> detailMap = null;
		String version = null;
		String group = null;
		LicenseChoice license = null;
		Component component = null;
		for (String software : softwareList)
		{
			if (logger.isDebugEnabled())
				logger.debug("Generating Component (" + software + ")");
			detailMap = produceDetailMap(software);
			version = detailMap.get("Version");
			group = detailMap.get("Release");
			license = processLicense(software);
			component = createComponents(software, detailMap, license, group,
					version, null, detailMap.get("Priority"));
			bom.addComponent(addPackageManager(component, PACKAGE_MANAGER));
		}
		
		return bom;
	}
	
	/**
	 * (U) This method is used to run the command to get the version of the package that is
	 * currently installed.
	 * 
	 * @param software String value of the software package to get the version of.
	 * @return String value of the version of the software that is currently installed.
	 * @throws SBomException in the event we can NOT get the version.
	 */
	public String getInstalledVersion(String software)
	{
		String version = "";
		
		String cmd = SOFTWARE_INSTALLED_VERSION + " " + software;
		
		if (logger.isDebugEnabled())
			logger.debug("Attempting to get software (" + software + ") version via: " + cmd);
		
		processBuilder.command("bash", "-c", cmd);
		
		try
		{
			Process process = processBuilder.start();
			
			version = readVersion(process);
		}
		catch (IOException ioe)
		{
			String error = "Unable to build unix process to get software version (" +
					cmd +
					") on the server!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Found Version (" + version + ") for " + software + ".");
		
		return version;
	}
	
	/**
	 * (U) This method is used to parse the version from the reader of the Unix Command being run.
	 * 
	 * @param reader BufferedReader of the contents of the Unix command being run.
	 * @return String the version pulled from the reader.
	 * @throws SBomException in the event we are unable to read from the Unix Command.
	 */
	public String parseVersion(BufferedReader reader)
	{
		String version = null;
		
		String line;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				if (line.startsWith("Installed"))
				{
					int index = line.indexOf(':');
					version = line.substring(index + 1).trim();
					break;
				}
			}
		}
		catch (IOException e)
		{
			String error = "Failed to read version.";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return version;
	}
	
	/**
	 * (U) This method is responsible for getting the license (if present) and placing it in the
	 * LicenseChoice Object passed back.
	 * 
	 * @param software String value of the software we are attempting to get the license for.
	 * @return LicenseChoice that contains information about the license.
	 */
	private LicenseChoice processLicense(String software)
	{
		LicenseChoice licenseChoice = null;
		
		String licenseFile = SOFTWARE_LICENSE_DIR + software + "/copyright";
		
		if (logger.isDebugEnabled())
			logger.debug("Attempting to process license (" + licenseFile + ")");
		
		try
		{
			String licenseTxt = new String(Files.readAllBytes(Paths.get(licenseFile)));
			licenseChoice = parseLicenseText(licenseTxt, AVAILABLE_LINUX_FLAVORS.UBUNTU);
		}
		catch (IOException ioe)
		{
			logger.warn("Unable to read license file (" + licenseFile + ")", ioe);
		}
		return licenseChoice;
	}
	
	/**
	 * (U) This method is used to produce a Detail Map of the Software in question. This will be
	 * used to create a CycloneDx Component.
	 * 
	 * @param software String value of the component to build the detail map for.
	 * @return Map containing the key value pairs about the software.
	 * @throws SBomException in the event we can NOT produce the detail map of the software.
	 */
	private Map<String, String> produceDetailMap(String software)
	{
		String cmd = SOFTWARE_DETAIL_CMD + " " + software + "=" + getInstalledVersion(software);
		
		return (produceDetailMap(cmd, AVAILABLE_LINUX_FLAVORS.UBUNTU));
	}
	
	/**
	 * (U) This method is used to read the Version from the Command Process.
	 * 
	 * @param process Process to read the version from.
	 * @return String the version.
	 * @throws SBomException in the event we are unable to process the command.
	 */
	public String readVersion(Process process)
	{
		String version = null;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())))
		{
			version = parseVersion(reader);
			
			// Read the remainder of the standard output, so that the process can finish
			while (reader.read() != -1) {}
			
			int exitVal = process.waitFor();
			if (exitVal != 0)
			{
				String error = "Unexpected process exit value (" + exitVal + "), while " +
						"attempting to get Installed Software Version!";
				logger.error(error);
				throw new SBomException(error);
			}
		}
		catch (SBomException sbom)
		{
			throw sbom;
		}
		catch (Exception e)
		{
			String error = "Unexpected error while attempting to get the software version!";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return version;
	}
}
