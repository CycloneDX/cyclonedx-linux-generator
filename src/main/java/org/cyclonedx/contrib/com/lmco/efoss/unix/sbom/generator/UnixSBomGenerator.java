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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cyclonedx.model.AttachmentText;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Component.Scope;
import org.cyclonedx.model.Component.Type;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.License;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Property;

import com.google.common.base.CharMatcher;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;

/**
 * (U) This class is the UnixSBomGenerator, it contains shared methods, used by the 
 * various flavors of Unix SBom Generators (RedHatSBomGenerator, and UbuntuSBomGenerator). 
 * 
 * @author wrgoff
 * @since 23 April 2020
 */
public class UnixSBomGenerator
{
	protected enum AVAILABLE_LINUX_FLAVORS
	{
		ALPINE, REDHAT, UBUNTU
	}
	
	protected static final Logger logger = Logger.getLogger(UnixSBomGenerator.class.getName());
	
	private static final String DETAILS_ERROR = "Unable to process output from unix process to " +
			"get details for a specific piece of software.";
	
	protected static final String SOFTWARE_LICENSE_DIR = "/usr/share/doc/";
	
	/**
	 * (U) This method is used to build the External References. To include the Web Page, Bugs, and
	 * documentation.
	 * 
	 * @param detailMap Map to pull the information from.
	 * @return List of ExternalReference created from the map passed in.
	 */
	public List<ExternalReference> buildExternalReferences(Map<String, String> detailMap)
	{
		List<ExternalReference> refs = new ArrayList<>();
		
		if (detailMap.containsKey("URL"))
		{
			ExternalReference webpage = new ExternalReference();
			webpage.setUrl(detailMap.get("URL"));
			webpage.setType(ExternalReference.Type.WEBSITE);
			refs.add(webpage);
		}
		if (detailMap.containsKey("Homepage"))
		{
			ExternalReference webpage = new ExternalReference();
			webpage.setUrl(detailMap.get("Homepage"));
			webpage.setType(ExternalReference.Type.WEBSITE);
			refs.add(webpage);
		}
		if (detailMap.containsKey("APT-Sources"))
		{
			ExternalReference docs = new ExternalReference();
			docs.setUrl(detailMap.get("APT-Sources"));
			docs.setType(ExternalReference.Type.DOCUMENTATION);
			refs.add(docs);
		}
		if (detailMap.containsKey("Bugs"))
		{
			ExternalReference bugs = new ExternalReference();
			bugs.setUrl(detailMap.get("Bugs"));
			bugs.setType(ExternalReference.Type.ISSUE_TRACKER);
			refs.add(bugs);
		}
		
		return refs;
	}
	
	/**
	 * (U) This method is used to build the scope of the software. Right now we are only use
	 * optional and required.
	 * 
	 * @param priority String value used to help us determine the software scope.
	 * @return Scope of the software based on the priority string passed in.
	 */
	private Scope buildScope(String priority)
	{
		if (priority == null)
			return Scope.REQUIRED;
		else if (priority.equalsIgnoreCase("optional"))
			return Scope.OPTIONAL;
		else
			return Scope.REQUIRED;
	}
	
	/**
	 * (U) This method is used to get the details for a particular piece of software that is
	 * installed on the system.
	 * 
	 * @param software  String name that will be used to lookup the software.
	 * @param detailMap Map used to pull specific information from.
	 * @param license   LicenseChoice to set the license to.
	 * @param group     String value to set the group to.
	 * @param version   String value to set the version to.
	 * @param purl      String to set the URL used to pull the software from.
	 * @param scope     String value to help us set the scope of the software.
	 * @return Component Sbom Component created from the supplied inputs.
	 */
	public Component createComponents(String software, Map<String, String> detailMap,
			LicenseChoice license, String group, String version, String purl, String scope)
	{
		Component component = new Component();
		component.setType(Type.OPERATING_SYSTEM);
		
		component.setName(software);
		component.setDescription(detailMap.get("Description"));
		component.setExternalReferences(buildExternalReferences(detailMap));
		component.setGroup(group);
		component.setLicenseChoice(license);
		component.setPublisher(detailMap.get("From repo"));
		component.setPurl(purl);
		component.setScope(buildScope(scope));
		component.setVersion(version);
		
		return component;
	}
	
	/**
	 * (U) This method is used to build the Map containing the fields and their values.
	 * 
	 * @param reader BufferedReader that contains the output to read.
	 * @return Map with the key value pairs generated from the reader passed in.
	 * @throws SBomException if we can NOT read from the reader passed in.
	 */
	public Map<String, String> generateAlpineDetailMap(BufferedReader reader)
	{
		Map<String, String> detailMap = new HashMap<>();
		
		try
		{
			String line;
			int index = 0;
			
			while ((line = reader.readLine()) != null)
			{
				if (!line.trim().startsWith("WARNING:"))
				{
					if (index == 0)
					{
						line = line.substring(line.indexOf(':') + 1);
						detailMap.put("Description", line.trim());
						index++;
					}
					else if (index == 1)
					{
						line = line.substring(line.indexOf(':') + 1);
						detailMap.put("URL", line.trim());
						index++;
					}
					else
						index++;
				}
			}
		}
		catch (IOException ioe)
		{
			logger.error(DETAILS_ERROR, ioe);
			throw new SBomException(DETAILS_ERROR, ioe);
		}
		return detailMap;
	}
	
	/**
	 * (U) This method is used to build the Map containing the fields and their values.
	 * 
	 * @param reader BufferedReader that contains the output to read.
	 * @param linuxFlavor Enumeration that tells us what version of Linux we are generating
	 * the Detail map for.
	 * @return Map with the key value pairs generated from the reader passed in.
	 * @throws SBomException if we can NOT read from the reader passed in.
	 */
	public Map<String, String> generateDetailMap(BufferedReader reader,
			AVAILABLE_LINUX_FLAVORS linuxFlavor)
	{
		Map<String, String> detailMap = new HashMap<>();
		
		try
		{
			String line;
			String componentDetailName = null;
			StringBuilder componentDetailValue = new StringBuilder();
			int index = 0;
			
			boolean foundStart = false;
			
			if (!linuxFlavor.equals(AVAILABLE_LINUX_FLAVORS.REDHAT))
				foundStart = true;
			
			while ((line = reader.readLine()) != null)
			{
				if (!foundStart)
				{
					if (line.startsWith("Installed Packages"))
						foundStart = true;
				}
				else
				{
					if (line.startsWith(" "))
					{
						if (linuxFlavor.equals(AVAILABLE_LINUX_FLAVORS.REDHAT))
						{
							line = line.trim();
							index = line.indexOf(':');
							line = line.substring(index + 1);
						}
						componentDetailValue.append(line);
					}
					else
					{
						if (componentDetailName != null)
						{
							detailMap.put(componentDetailName.trim(),
									componentDetailValue.toString());
							componentDetailName = null;
							componentDetailValue = new StringBuilder();
						}
						
						if (line.contains(":"))
						{
							index = line.indexOf(':');
							componentDetailName = line.substring(0, index);
							componentDetailValue.append(line.substring(index + 1).trim());
						}
					}
				}
			}
		}
		catch (IOException ioe)
		{
			logger.error(DETAILS_ERROR, ioe);
			throw new SBomException(DETAILS_ERROR, ioe);
		}
		return detailMap;
	}
	
	/**
	 * (U) This method is used to get the list of Software that is installed on this instance.
	 * 
	 * @param cmd                 String value for the Unix command that will give us the list of
	 *                            software installed.
	 * @param separator           character value for the separator, used to tell us where to stop
	 *                            the package at.
	 * @param preProcessingString String value used to tell us when to start looking for packages.
	 * @return List of Software installed on the server.
	 * @throws SBomException in the event we are unable to get the list of software that is
	 *                       installed on the server.
	 */
	protected List<String> generateListOfSoftware(String cmd, char separator,
			String preProcessingString)
	{
		List<String> softwareList = new ArrayList<>();
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		processBuilder.command("bash", "-c", cmd);
		
		try
		{
			Process process = processBuilder.start();
			
			softwareList = processListOfSoftware(process, cmd, separator, preProcessingString);
		}
		catch (IOException ioe)
		{
			String error = "Unable to build unix process to get list of installed software (" +
					cmd + ")!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return softwareList;
	}
	
	/**
	 * (U) This method is used to build the Map containing the fields and their values.
	 * 
	 * @param reader BufferedReader that contains the output to read.
	 * @return Map with the key value pairs generated from the reader passed in.
	 * @throws SBomException if we can NOT read from the reader passed in.
	 */
	public Map<String, String> generateRedHatDetailMap(BufferedReader reader)
	{
		return (generateDetailMap(reader, AVAILABLE_LINUX_FLAVORS.REDHAT));
	}
	
	/**
	 * (U) This method is used to build the Map containing the fields and their values.
	 * 
	 * @param reader BufferedReader that contains the output to read.
	 * @return Map with the key value pairs generated from the reader passed in.
	 * @throws SBomException if we can NOT read from the reader passed in.
	 */
	public Map<String, String> generateUbuntuDetailMap(BufferedReader reader)
	{
		return (generateDetailMap(reader, AVAILABLE_LINUX_FLAVORS.UBUNTU));
	}
	
	/**
	 * (U) This method is used to process the String into a LicenseChoice Object.
	 * 
	 * @param licenseTxt  String to process into a LicenseChoice Object.
	 * @param linuxFlavor Enumeration that tells us the flavor of Linux we are parsing the license
	 *                    for.
	 * @return LicenseChoice created from the text passed in.
	 */
	public LicenseChoice parseLicenseText(String licenseTxt,
			AVAILABLE_LINUX_FLAVORS linuxFlavor)
	{
		LicenseChoice licenseChoice = null;
		
		if ((licenseTxt.length() > 0) && (!licenseTxt.contains("--- end of LICENSE ---")))
		{
			if (AVAILABLE_LINUX_FLAVORS.REDHAT.equals(linuxFlavor))
			{
				CharMatcher charsToPreserve = CharMatcher.anyOf("\r\n\t");
				CharMatcher allButPreserved = charsToPreserve.negate();
				CharMatcher controlCharactersToRemove = CharMatcher.javaIsoControl()
						.and(allButPreserved);
				
				licenseTxt = controlCharactersToRemove.removeFrom(licenseTxt);
			}
			
			AttachmentText licenseText = new AttachmentText();
			licenseText.setText(licenseTxt);
			licenseText.setContentType("text/plain");
			
			License license = new License();
			license.setLicenseText(licenseText);
			
			licenseChoice = new LicenseChoice();
			licenseChoice.addLicense(license);
		}
		else
			logger.warn("No license read from file!");
		
		return licenseChoice;
	}
	
	/**
	 * (U) This method is used to process a Unix command's output.
	 * 
	 * @param process     Process associated with the Unix command.
	 * @param linuxFlavor Enumeration that tells us the linux version we are on.
	 * @return Map containing the key value pairs about the software.
	 * @throws SBomException in the event we can NOT produce the detail map of the software.
	 */
	public Map<String, String> processDetailMapCommand(Process process,
			AVAILABLE_LINUX_FLAVORS linuxFlavor)
	{
		Map<String, String> detailMap = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())))
		{
			if (linuxFlavor.equals(AVAILABLE_LINUX_FLAVORS.ALPINE))
				detailMap = generateAlpineDetailMap(reader);
			else if (linuxFlavor.equals(AVAILABLE_LINUX_FLAVORS.REDHAT))
				detailMap = generateRedHatDetailMap(reader);
			else if (linuxFlavor.equals(AVAILABLE_LINUX_FLAVORS.UBUNTU))
				detailMap = generateUbuntuDetailMap(reader);
			
			int exitVal = process.waitFor();
			if (exitVal != 0)
			{
				String error = "Unexpected process exit value (" + exitVal + "), while " +
						"attempting to generate the Detail Map!";
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
			String error = "Unexpected error while attempting to software details!";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return detailMap;
	}
	
	/**
	 * (U) This method is used to run a Unix command, and process its output.
	 * 
	 * @param reader              BufferedReader to read the output.
	 * @param separator           character value for the separator, used to tell us where to stop
	 *                            the package at.
	 * @param preProcessingString String value used to tell us when to start looking for packages.
	 * @return List of String for the Software found.
	 * @throws SBomException if we are unable to get the list of software.
	 */
	public List<String> processListCmdOutput(BufferedReader reader, char separator,
			String preProcessingString)
	{
		List<String> softwareList = new ArrayList<>();
		
		String line;
		int index = 0;
		boolean preProcessed = false;
		
		if (preProcessingString == null)
			preProcessed = true;
		
		try
		{
			while ((line = reader.readLine()) != null)
			{
				if(!line.trim().startsWith("WARNING:"))
				{	
					if (preProcessed)
					{
						if(separator == '\n')
							index = line.length();
						else
							index = line.indexOf(separator);
						if (index > 0)
						{
							line = line.substring(0, index);
							softwareList.add(line);
						}
					}
					else if (line.startsWith(preProcessingString))
					{
						preProcessed = true;
					}
				}
				else
					logger.info("Ignoring line for package! (" + line + ").");
			}
		}
		catch (IOException ioe)
		{
			String error = "Unable to process output from unix process to get list of installed " +
					"software on the server!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return softwareList;
	}
	
	/**
	 * (U) This method process the output of the Unix Command, that is used to build the list of
	 * software.
	 * 
	 * @param process             Unix Process to generate the list of software from.
	 * @param cmd                 String value of the command we are running.
	 * @param separator           Character that tells us the separator used in the output of the
	 *                            Unix command.
	 * @param preProcessingString String value that helps us parse the output of the Unix command.
	 * @return List of Strings that make up the list of software.
	 * @throws SBomException in the event we are unable to process the output of the Unix command.
	 */
	public List<String> processListOfSoftware(Process process, String cmd, char separator,
			String preProcessingString)
	{
		List<String> softwareList = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())))
		{
			softwareList = processListCmdOutput(reader, separator, preProcessingString);
			
			int exitVal = process.waitFor();
			if (exitVal != 0)
			{
				String error = "Unexpected process exit value (" + exitVal + "), while " +
						"attempting to get the list of software installed (" +
						cmd + ")!";
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
			String error = "Unexpected error while attempting to get the list of " +
					"software installed (" + cmd + ")!";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return softwareList;
	}
	
	/**
	 * U) This method is used to produce a Detail Map of the Software in question. This will be used
	 * to create a CycloneDx Component.
	 * 
	 * @param cmd         String value of the command to run.
	 * @param linuxFlavor Enumeration that tells us how to parse the results.
	 * @return Map containing the key value pairs about the software.
	 * @throws SBomException in the event we can NOT produce the detail map of the software.
	 */
	protected Map<String, String> produceDetailMap(String cmd,
			AVAILABLE_LINUX_FLAVORS linuxFlavor)
	{
		Map<String, String> detailMap = new HashMap<>();
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		
		processBuilder.command("bash", "-c", cmd);
		
		try
		{
			Process process = processBuilder.start();
			detailMap = processDetailMapCommand(process, linuxFlavor);
		}
		catch (SBomException sbe)
		{
			String error = "Unable to process Unix Command's output while attempting to get the " +
					"software details (" + cmd + ")!";
			logger.error(error, sbe);
			throw sbe;
		}
		catch (IOException ioe)
		{
			String error = "Unable to build unix process to get software details (" + cmd + ")!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return detailMap;
	}
	
	/**
	 * (U) This method is used to add the Unix Package Manger that created this component.
	 * 
	 * @param component      Component to add the package manager to.
	 * @param packageManager String value that tells us which package manager was used.
	 * @return Component the update Component with the package manager added to it.
	 */
	public Component addPackageManager(Component component, String packageManager)
	{
		Property packageManagerProperty = new Property();
		packageManagerProperty.setName("unixPropertyManager");
		packageManagerProperty.setValue(packageManager);
		
		List<Property> properties = component.getProperties();
		if (properties == null)
			properties = new ArrayList<>();
		properties.add(packageManagerProperty);
		
		component.setProperties(properties);
		
		return component;
	}
}
