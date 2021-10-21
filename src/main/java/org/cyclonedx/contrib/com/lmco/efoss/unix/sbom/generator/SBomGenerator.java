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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.DateUtils;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.SBomCommons;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.SBomCommonsException;
import org.cyclonedx.contrib.com.lmco.efoss.sbom.commons.utils.StringUtils;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.OperatingSystemUtils;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Hash;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.Tool;
import org.cyclonedx.util.BomUtils;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;

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
	
	private static final String dockerImageFile = "DockerImage";

	/**
	 * Constructor.
	 */
	private SBomGenerator()
	{
	}

	/**
	 * (U) This method is used to create the Tools, and add the UnixSBomGenerator to
	 * the list of tools.
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
	 * (U) This method prepares the master Component.
	 * 
	 * @param imageUrl String value of where go get the image from.
	 * @param name     String value of the name if supplied.
	 * @param group    String value of the group if supplied.
	 * @param version  String value of the version if supplied.
	 * @return Component, either null if no imageUrl, name, group, or version are
	 *         supplied. Else it will be filled in.
	 * @throws SBomException in the event we are unable to pull the image from the
	 *                       imageUrl provided.
	 */
	public static Component createMasterComponent(String imageUrl, String name,
					String group, String version) throws SBomException
	{
		Component master = null;
		if ((StringUtils.isValid(imageUrl)) || 
						(StringUtils.isValid(name) && ((StringUtils.isValid(version)))))
		{
			master = createMasterComponent(imageUrl);
			
			if (StringUtils.isValid(imageUrl))
			{
				TreeMap<String, String> qualifiers = new TreeMap<>();
				qualifiers.put("repository_url", getBaseUrl(imageUrl));
				qualifiers.put("type", "image");
				try
				{
					PackageURL packageUrl = new PackageURL(
									PackageURL.StandardTypes.DOCKER, null,
									name.toLowerCase(), version, qualifiers,
									getSubPath(imageUrl));
					master.setPurl(packageUrl);
					master.setBomRef(master.getPurl());
				}
				catch (MalformedPackageURLException e)
				{
					String error = "Failed to build the Package URL!";
					logger.error(error, e);
					throw new SBomException(error);
				}
			}
			if (StringUtils.isValid(name))
				master.setName(name.toLowerCase());
			if (StringUtils.isValid(group))
				master.setGroup(group.toLowerCase());
			if (StringUtils.isValid(version))
				master.setVersion(version);
		}

		return master;
	}
	
	/**
	 * (U) This method is used to generate the XML and JSon files containing the Software Bill of
	 * Materials.
	 * 
	 * @param bom Bill of Materials to create the files form.
	 * @throws SBomException in the event we can NOT create either the XML of JSon files.
	 */
	public static void geneateBoms(Bom bom)
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
	 * (U) This method is used to generate the Software Bill Of Materials (SBOM) for
	 * this operating system.
	 * 
	 * @param cli CommandLine arguments.
	 * @return int the number of programs found within the operating system.
	 * @throws SBomException if we are unable to build the SBOM.
	 */
	public static int generateSBom(CommandLine cli)
	{		
		OperatingSystemUtils osUtils = new OperatingSystemUtils();
		String vendor = osUtils.getOsVendor();
		
		if (logger.isDebugEnabled())
			logger.debug("Attempting to build SBOM for " + vendor + ".");
		
		Component master = createMasterComponent(cli);

		Bom bom = null;
		
		int softwareSize = 0;

		if (cli.hasOption("nc"))
		{
			bom = new Bom();
			bom.setComponents(new ArrayList<Component>());
		}
		else
		{
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
		}
		
		if(bom != null)
		{
			addBomTools(bom);
			bom.getMetadata().setComponent(master);
			softwareSize = bom.getComponents().size();
			geneateBoms(bom);

		}
		return softwareSize;
	}
	
	/**
	 * (U) This method is responsible for build the Master Component (upper level
	 * Component).
	 * 
	 * @param cli ComandLine options used to build the master component.
	 * @return Component newly created master component.
	 * @throws SBomException in the event we are unable to get the image from the
	 *                       image URL (if it was provided).
	 */
	private static Component createMasterComponent(CommandLine cli) throws SBomException
	{
		Component master = null;

		String imageUrl = cli.getOptionValue("image");
		String name = cli.getOptionValue("name");
		String group = cli.getOptionValue("group");
		String version = cli.getOptionValue("version");

		if ((!StringUtils.isValid(name)) || (!StringUtils.isValid(version)))
		{
			OperatingSystemUtils osUtils = new OperatingSystemUtils();

			if (!StringUtils.isValid(name))
				name = osUtils.getOsVendor();
			if (!StringUtils.isValid(version))
				version = osUtils.getOsVersion();
		}

		master = createMasterComponent(imageUrl, name, group, version);

		return master;
	}

	/**
	 * (U) This method is used to create the master component. It will then fill in
	 * the image information (if provided).
	 * 
	 * @param imageUrl String value of where to get the docker image from.
	 * @return Component created, and filled in if the imageUrl is provided.
	 * @throws SBomException in the event we are unable to pull the image via the
	 *                       image URL provided.
	 */
	private static Component createMasterComponent(String imageUrl) throws SBomException
	{
		Component master = new Component();
		master.setType(org.cyclonedx.model.Component.Type.CONTAINER);

		if (StringUtils.isValid(imageUrl))
		{
			logger.debug("Attempting to pull docker image(" + imageUrl + ")!");
			if (!imageUrl.startsWith("http"))
				imageUrl = "https://" + imageUrl;

			try (InputStream in = new URL(imageUrl).openStream())
			{
				Files.copy(in, Paths.get(dockerImageFile), StandardCopyOption.REPLACE_EXISTING);

				List<Hash> hashes = BomUtils.calculateHashes(
								Paths.get(dockerImageFile).toFile(),
								CycloneDxSchema.Version.VERSION_13);
				master.setHashes(hashes);
			}
			catch (MalformedURLException e)
			{
				String error = "Failed to build hashes from image url (" +
								imageUrl + ")!";
				logger.error(error, e);
				throw new SBomException(error);
			}
			catch (IOException e)
			{
				String error = "Failed to build hashes from File pulled from image " +
								"url (" + imageUrl + ")!";
				logger.error(error, e);
				throw new SBomException(error);
			}
			finally
			{
				try
				{
					Files.delete(Paths.get(dockerImageFile));
				}
				catch (IOException e)
				{
					logger.warn("Failed to delete Docker image file!");
				}
			}
		}
		return master;
	}

	/**
	 * (U) This method is used to get the Base URL that will be used to build the
	 * package URL.
	 * 
	 * @param imageUrl String value of where to get the image from.
	 * @return String the Base URL, that will be used to build the package URL.
	 */
	private static String getBaseUrl(String imageUrl)
	{
		String baseUrl = imageUrl;

		if (imageUrl.contains("/"))
			baseUrl = baseUrl.substring(0, baseUrl.indexOf('/'));

		return baseUrl;
	}

	/**
	 * (U) This method is used to get the Sub path that will be used to build the
	 * Package URL.
	 * 
	 * @param imageUrl String value of where to get the image from.
	 * @return String the sub path used to build the package URL.
	 */
	private static String getSubPath(String imageUrl)
	{
		String subPath = null;

		if (imageUrl.contains("/"))
		{
			int startIndex = imageUrl.indexOf('/');
			int endIndex = imageUrl.indexOf('/', startIndex + 1);
			if ((startIndex > 0) && (endIndex > 0))
				subPath = imageUrl.substring(startIndex + 1, endIndex);
		}

		return subPath;
	}
}