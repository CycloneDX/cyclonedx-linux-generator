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
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;

import com.lmco.efoss.unix.sbom.exceptions.SBomException;

/**
 * (U) This class is responsible for generating the Software Bill Of Materials (SBOM) for all Alpine
 * Linux Operating Systems. Or any Linux operating systems using the APK package manager.
 * 
 * @author wrgoff
 * @since 6 October 2020
 */
public class AlpineSBomGenerator extends UnixSBomGenerator
{
	private static final CharSequence POLICY = "policy:";
	// Unix Commands.
	private static final String PACKAGE_MANAGER = "apk";
	private static final String SOFTWARE_DETAIL_CMD = "apk -vv info ";
	private static final String SOFTWARE_LIST_CMD = "apk info";
	
	private static final String SOFTWARE_VERSION_CMD = "apk policy ";
	
	private ProcessBuilder processBuilder = new ProcessBuilder();
	
	/**
	 * (U) This method is used to generate the Software Bill Of Materials (SBOM) for all RedHat
	 * Linux Operating systems.
	 * 
	 * @return Bom The Software Bill Of Materials for this RedHat Linux Operating System.
	 * @throws SBomException if we are unable to build the SBOM.
	 */
	public Bom generateSBom() throws SBomException
	{
		List<String> softwareList = generateListOfSoftware(SOFTWARE_LIST_CMD, '\n',
				null);
		
		Bom bom = new Bom();
		
		if (logger.isDebugEnabled())
			logger.debug("Processing " + softwareList.size() + " software programs.");
		
		Map<String, String> detailMap = null;
		String version = null;
		Component component = null;
		for (String software : softwareList)
		{
			if (logger.isDebugEnabled())
				logger.debug("Generating Component (" + software + ")");
			detailMap = produceDetailMap(software);
			version = getVersion(software);
			component = createComponents(software, detailMap, null, null,
					version, null, null);
			bom.addComponent(addPackageManager(component, PACKAGE_MANAGER));
		}
		return bom;
	}
	
	/**
	 * (U) This method is responsible for getting the version for a specific package.
	 * @param software String value of the software to get the version for.
	 * @return String the version of the software.
	 * @throws SBomException if we are unable to run the command to get the version.
	 */
	public String getVersion(String software) throws SBomException
	{
		String version = null;
		
		String cmd = SOFTWARE_VERSION_CMD + software;
		
		processBuilder.command("bash", "-c", cmd);
		
		try
		{
			Process process = processBuilder.start();
			version = parseVersion(process, cmd);
		}
		catch (IOException ioe)
		{
			String error = "Unable to build unix process to get software version (" + cmd + ")!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return version;
	}
	
	/**
	 * (U) This method is used to parse the version from the linux command that ran.
	 * 
	 * @param reader BufferedReader to get the version from.
	 * @return String the version associated with a piece of software.
	 * @throws SBomException if we are unable to read the version from the reader.
	 */
	public String parseVersion(BufferedReader reader)
			throws SBomException
	{
		String version = null;
		
		try
		{
			String line;
			boolean foundStart = false;
			
			while ((line = reader.readLine()) != null)
			{
				if (!line.trim().startsWith("WARNING:"))
				{
					if (foundStart)
					{
						version = line.trim();
						if (version.endsWith(":"))
						{
							version = version.substring(0, version.length() - 1);
						}
						break;
					}
					if (line.contains(POLICY))
						foundStart = true;
				}
			}
		}
		catch (IOException ioe)
		{
			String error = "Unable to process output from unix process to get version for a " +
					"specific piece of software.";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return version;
	}
	
	/**
	 * (U) This method is used to parse the version from a process created from a Unix command.
	 * 
	 * @param process Process to pull the version from.
	 * @param cmd     String value of the Unix command that was run.
	 * @return String the version pulled from the output of the unix command that was run.
	 * @throws SBomException in the event we are unable to complete the processing of the unix
	 *                       command.
	 */
	public String parseVersion(Process process, String cmd) throws SBomException
	{
		String version = null;
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())))
		{
			version = parseVersion(reader);
			
			int exitVal = process.waitFor();
			if (exitVal != 0)
			{
				String error = "Unexpected process exit value (" + exitVal + "), while " +
						"attempting to get Software Version (" + cmd + ")!";
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
			String error = "Unexpected error while attempting to software version (" + cmd +
					")!";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return version;
	}
	
	/**
	 * (U) This method is used to produce a Detail Map of the Software in question. This will be
	 * used to create a CycloneDx Component.
	 * 
	 * @param software String value of the component to build the detail map for.
	 * @return Map containing the key value pairs about the software.
	 * @throws SBomException in the event we can NOT produce the detail map of the software.
	 */
	private Map<String, String> produceDetailMap(String software) throws SBomException
	{
		String cmd = SOFTWARE_DETAIL_CMD + software;
		
		return (produceDetailMap(cmd, AVAILABLE_LINUX_FLAVORS.ALPINE));
	}
}
