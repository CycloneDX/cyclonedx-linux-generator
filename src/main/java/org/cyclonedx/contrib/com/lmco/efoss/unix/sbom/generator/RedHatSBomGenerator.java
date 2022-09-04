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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.github.packageurl.MalformedPackageURLException;
import com.github.packageurl.PackageURL;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.LicenseChoice;

import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;

/**
 * * (U) This class is responsible for generating the Software Bill Of Materials (SBOM) for all
 * Oracle Red Hat Linux Operating Systems.
 *
 * @author wrgoff
 * @since 24 April 2020
 */
public class RedHatSBomGenerator extends UnixSBomGenerator
{
	private static final List<String> POSSIBLE_LICENSE_FILES = new ArrayList<>(
			List.of("LICENSE.txt",
			"LICENSE", "COPYING", "COPYING.LGPL", "PORTING", "Copying"));

	private static final String PACKAGE_MANAGER = "yum";

	// Unix Commands.
	private static final String PURL_CMD = "yumdownloader --urls";
	private static final String SOFTWARE_DETAIL_CMD = "yum info";
	private static final String SOFTWARE_LIST_CMD = "yum list installed";

	private String purlNamespace = "rhel";

	private ProcessBuilder processBuilder = new ProcessBuilder();

	public void setPurlNamespace(String ns){
		this.purlNamespace = ns;
	}

	/**
	 * (U) This method is used to generate the Software Bill Of Materials (SBOM) for all RedHat
	 * Linux Operating systems.
	 *
	 * @return Bom The Software Bill Of Materials for this RedHat Linux Operating System.
	 * @throws SBomException if we are unable to build the SBOM.
	 */
    public Bom generateSBom() {
        List<String> softwareList = generateListOfSoftware(SOFTWARE_LIST_CMD, ' ',
                "Installed Packages");

        Bom bom = new Bom();

        if (logger.isDebugEnabled())
            logger.debug("Processing " + softwareList.size() + " software programs.");

        Map<String, String> detailMap = null;
        String version = null;
        String group = null;
        LicenseChoice license = null;
        PackageURL purl = null;
        Component component = null;
        for (String software : softwareList) {
            if (logger.isDebugEnabled())
                logger.debug("Generating Component (" + software + ")");
            detailMap = produceDetailMap(software);
            version = detailMap.get("Version");
            group = detailMap.get("Release");
            license = processLicense(software, version);
            try {
                purl = getPurl(software, version);

                // TODO: get arch and distro
                //purl = getPurl(software,version, arch, distro);
            } catch (MalformedPackageURLException e) {
                logger.debug("Can't get purl", e);
            }

			try {
				String downloadUrl = getPackageDownloadUrl(software);
				if (downloadUrl != null) {
					detailMap.put("download_url", downloadUrl);
				}
			} catch(SBomException e){
				logger.debug("Error getting download_url", e);
			}
            component = createComponents(software, detailMap, license, group,
                    version, purl, detailMap.get("Priority"));
            bom.addComponent(addPackageManager(component, PACKAGE_MANAGER));
        }
        return bom;
    }

	/**
	 * (U) This method is used to attempt to figure out which file is the license file. If any.
	 *
	 * @param software String value of the software we are attempting to get the license file for.
	 * @param version  String value of the version of the software we are attempting to get the
	 *                 license for.
	 * @return String if NOT null contains a file name, which should be the license file.
	 */
	public String getLicenseFileName(String software, String version)
	{
		File tempFile = null;

		software = software.trim();

		if (software.endsWith(".x86_64"))
			software = software.replace(".x86_64", "");

		if (version != null)
			software = software + "-" + version.trim();

		if (logger.isDebugEnabled())
			logger.debug("Attempting to get license file from " + SOFTWARE_LICENSE_DIR + software +
					".");

		for (String fileToTry : POSSIBLE_LICENSE_FILES)
		{
			tempFile = new File(SOFTWARE_LICENSE_DIR + software, fileToTry);
			if (tempFile.exists())
				return (SOFTWARE_LICENSE_DIR + software + "/" + fileToTry);
		}
		return null;
	}


	public PackageURL getPurl(String software, String version) throws MalformedPackageURLException {
		return this.getPurl(software, version, null, null);
	}

	public PackageURL getPurl(String software, String version, String arch, String distro) throws MalformedPackageURLException {
		TreeMap<String, String> qualifiers = new TreeMap<>();
		if (arch != null) {
			qualifiers.put("arch", arch);
		}
		if (distro != null) {
			qualifiers.put("distro", distro);
		}
		return new PackageURL(
				PackageURL.StandardTypes.RPM, purlNamespace.toLowerCase(), software, version, qualifiers, null);
	}


	/**
	 * (U) This method is used to get the Product Uniform Resource Locator (PURL) or as we know it
	 * the download Uniform Resource Locator (URL).
	 *
	 * @param software String value of the software to get the PURL for.
	 * @return String the URL that will be used to download this software product.
	 * @throws SBomException in the event we are unable to get the PURL from the server.
	 */
	public String getPackageDownloadUrl(String software){
		String purl = null;
		String cmd = PURL_CMD + " " + software;

		processBuilder.command("bash", "-c", cmd);

		if (logger.isDebugEnabled())
			logger.debug("Attempting to get PURL for " + software + ".");

		try
		{
			Process process = processBuilder.start();
			purl = parsePurl(process, software);
		}
		catch (IOException ioe)
		{
			String error = "Unable to build unix process to get software package's PURL (" + cmd +
					")!";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return purl;
	}

	/**
	 * (U) This method is used to parse the PURL from the output of the command process.
	 *
	 * @param process  Process that is running the YUM command.
	 * @param software String value of the software, used for debugging purposes.
	 * @return String the PURL if available.
	 * @throws SBomException if we are unable to process the output of the YUM command.
	 */
	public String parsePurl(Process process, String software)
	{
		String purl = null;

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream())))
		{
			purl = parsePurlCmdOutput(reader);

			if (purl == null)
				logger.warn("No PURL found for software package (" + software + ").");

			int exitVal = process.waitFor();
			if (exitVal != 0)
			{
				String error = "Unexpected process exit value (" + exitVal + "), while " +
						"attempting to get a software package's (" + software + ")!";
				logger.warn(error);
			}
		}
		catch (SBomException sbom)
		{
			throw sbom;
		}
		catch (Exception e)
		{
			String error = "Unexpected error while attempting to lookup a software " +
					"package's (" + software + ")!";
			logger.error(error, e);
			throw new SBomException(error, e);
		}
		return purl;
	}

	/**
	 * (U) This method is used to parse the Unix Command Output to get the download Uniform Resource
	 * Locator (URL) also known as the Product Uniform Resource Locator (PURL)
	 *
	 * @param reader BufferedReader to parse the output form.
	 * @return String the PURL or download URL that can be used to download the software package.
	 * @throws SBomException in the event we are unable to parse the command's output.
	 */
	public String parsePurlCmdOutput(BufferedReader reader)
	{
		String purl = null;
		try
		{
			String line;
			boolean found = false;
			while (((line = reader.readLine()) != null) && (!found))
			{
				if (line.startsWith("http"))
				{
					purl = line.trim();
					found = true;
				}
				else if (line.startsWith("No Match for argument")) // No PURL found.
				{
					found = true;
				}
			}
		}
		catch (IOException ioe)
		{
			String error = "Unable to process output from unix process to get the Package " +
					"download URL.";
			logger.error(error, ioe);
			throw new SBomException(error, ioe);
		}
		return purl;
	}

	/**
	 * (U) This method is responsible for getting the license (if present) and placing it in the
	 * LicenseChoice Object passed back.
	 *
	 * @param software String value of the software we are attempting to get the license for.
	 * @param version  String value of the version of the software we are attempting to get the
	 *                 license for.
	 * @return LicenseChoice that contains information about the license.
	 */
	private LicenseChoice processLicense(String software, String version)
	{
		if (logger.isDebugEnabled())
			logger.debug("Attempting to get license file for " + software + ".");

		LicenseChoice licenseChoice = null;

		String licenseFile = getLicenseFileName(software, version);

		try
		{
			if (licenseFile != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Attempting to process license (" + licenseFile + ")");

				String licenseTxt = new String(Files.readAllBytes(Paths.get(licenseFile)));
				licenseChoice = parseLicenseText(licenseTxt, AVAILABLE_LINUX_FLAVORS.REDHAT);
			}
			else
				logger.warn("Unable to find suitable license file for " + software + ".");
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
		String cmd = SOFTWARE_DETAIL_CMD + " " + software;

		return (produceDetailMap(cmd, AVAILABLE_LINUX_FLAVORS.REDHAT));
	}
}
