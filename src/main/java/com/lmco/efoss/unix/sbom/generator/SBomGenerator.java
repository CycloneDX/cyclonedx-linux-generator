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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.Tool;

import com.lmco.efoss.sbom.commons.utils.DateUtils;
import com.lmco.efoss.sbom.commons.utils.SBomCommons;
import com.lmco.efoss.sbom.commons.utils.SBomCommonsException;
import com.lmco.efoss.unix.sbom.exceptions.SBomException;
import com.lmco.efoss.unix.sbom.utils.OperatingSystemUtils;

/**
 * (U) This class is responsible for the actual generation of the Software Bill Of Materials (SBOM)
 * of the Unix operating system.
 * 
 * @author wrgoff
 * @since 22 April 2020
 */
public class SBomGenerator
{
	private static final CharSequence ALPINE = "ALPINE";
	
	private static final CharSequence DEBIAN = "DEBIAN";
	private static final Logger logger = Logger.getLogger(SBomGenerator.class.getName());
	private static final CharSequence UBUNTU = "UBUNTU";
	
	/**
	 * Constructor.
	 */
	private SBomGenerator()
	{}
	
	/**
	 * (U) This method is used to create the Tools, and add the UnixSBomGenerator to the list of
	 * tools.
	 * 
	 * @param bom Software Bom to add the UnixSbomGenerator Tool to.
	 */
	public static void addBomTools(Bom bom)
	{
		Metadata metadata = new Metadata();
		metadata.setTimestamp(DateUtils.rightNowDate());
		
		Tool unixSbomGenerator = new Tool();
		unixSbomGenerator.setName("UnixSBomGenerator");
		unixSbomGenerator.setVendor("Lockheed Martin");
		
		String version = "unknown";
		try
		{
			String path = SBomGenerator.class.getProtectionDomain().getCodeSource().getLocation()
					.getPath();
			
			String jarSub = "UnixSbomGenerator-";
			int index = path.indexOf(jarSub);
			int endIndex = path.indexOf(".jar");
			
			version = path.substring(index + jarSub.length(), endIndex);
			
			System.out.println("Version: " + version);
		}
		catch (Exception e)
		{
			logger.warn("Unable to determine verison of UnixSBomGenerator Tool!");
		}
		unixSbomGenerator.setVersion(version);
		List<Tool> completeToolList = new ArrayList<>();
		completeToolList.add(unixSbomGenerator);
		
		metadata.setTools(completeToolList);
		
		bom.setMetadata(metadata);
	}
	
	/**
	 * (U) This method creates the file and places the contents in the file.
	 * 
	 * @param bom    Bill of Materials to put in the file.
	 * @param format Enumeration that tell us which file we are producing.
	 * @throws SBomException if we are unable to produce the file.
	 */
	public static void createBomFile(Bom bom, SBomCommons.AVAILABLE_FORMATS format)
			throws SBomException
	{
		File file = new File("output/bom." + format.toString().toLowerCase());
		try (FileWriter writer = new FileWriter(file))
		{
			writer.write(SBomCommons.generateOutputString(bom, format));
		}
		catch (SBomCommonsException sce)
		{
			logger.error(sce);
			throw new SBomException(sce);
		}
		catch (IOException ioe)
		{
			String error = "Unable to write to " + format.toString() + " SBOM File!";
			logger.error(error, ioe);
			throw new SBomException(error);
		}
	}
	
	/**
	 * (U) This method is used to generate the XML and JSon files containing the Software Bill of
	 * Materials.
	 * 
	 * @param bom Bill of Materials to create the files form.
	 * @throws SBomException in the event we can NOT create either the XML of JSon files.
	 */
	public static void geneateBoms(Bom bom) throws SBomException
	{
		try
		{	
			if (logger.isDebugEnabled())
				logger.debug("Generating bom.xml file from bom entries");
			
			Files.createDirectories(Paths.get("output"));
			
			createBomFile(bom, SBomCommons.AVAILABLE_FORMATS.XML);
			createBomFile(bom, SBomCommons.AVAILABLE_FORMATS.JSON);
		}
		catch (IOException ioe)
		{
			String error = "Unable to create ouptut directory!";
			logger.error(error, ioe);
			throw new SBomException(error);
		}
	}

	/**
	 * (U) This method is used to generate the Software Bill Of Materials (SBOM) for this operating
	 * system.
	 * 
	 * @return int the number of programs found within the operating system.
	 * @throws SBomException if we are unable to build the SBOM.
	 */
	public static int generateSBom() throws SBomException
	{		
		String vendor = OperatingSystemUtils.getOsVendor();
		
		if (logger.isDebugEnabled())
			logger.debug("Attempting to build SBOM for " + vendor + ".");
		
		Bom bom = null;
		
		int softwareSize = 0;
				
		if (vendor.toUpperCase().trim().contains(ALPINE))
		{
			if (logger.isInfoEnabled())
				logger.info(vendor + ", uses the APK package manager.");
			AlpineSBomGenerator generator = new AlpineSBomGenerator();
			bom = generator.generateSBom();
		}
		else if ((vendor.toUpperCase().trim().contains(UBUNTU)) || 
				(vendor.toUpperCase().trim().contains(DEBIAN)))
		{
			if (logger.isInfoEnabled())
				logger.info(vendor + ", uses the APT package manager.");
			UbuntuSBomGenerator generator = new UbuntuSBomGenerator();
			bom = generator.generateSBom();
		}
		else
		{
			if (logger.isInfoEnabled())
				logger.info(vendor + ", assuming it is a redhat flavor (Yum Package Manager).");
			RedHatSBomGenerator generator = new RedHatSBomGenerator();
			bom = generator.generateSBom();
		}
		
		if(bom != null)
		{
			addBomTools(bom);
			softwareSize = bom.getComponents().size();
			geneateBoms(bom);
		}
		return softwareSize;
	}
	
}