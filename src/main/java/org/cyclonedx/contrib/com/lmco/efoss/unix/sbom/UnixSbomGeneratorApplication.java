/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package org.cyclonedx.contrib.com.lmco.efoss.unix.sbom;

import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.exceptions.SBomException;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.generator.SBomGenerator;
import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.DateUtils;

/**
 * (U) This Spring Boot application is used to build a Software Build Of Materials (SBOM) for a Unix
 * Virtual Machine (VM), regardless if it is Centos, Redhat, or Ubuntu.
 *
 * @author wrgoff
 * @since 22 April 2020
 */
public class UnixSbomGeneratorApplication
{
	private static final Logger logger = Logger
			.getLogger(UnixSbomGeneratorApplication.class.getName());

	
	/**
	 * (U) Main method used to start the building of the linux operating system building of the
	 * Software Bill Of Materials (SBOM).
	 * 
	 * @param args String array of arguments.
	 */
	public static void main(String[] args)
	{
		int softwareProcessed = 0;

		Date startDate = DateUtils.rightNowDate();
		boolean failed = false;

		CommandLineParser cliParser = new DefaultParser();
		Options cliOptions = createCliOptions();
		boolean runningHelp = false;
		try
		{
			CommandLine cli = cliParser.parse(cliOptions, args);
			if (cli.hasOption("help"))
			{
				runningHelp = true;
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("help", cliOptions);
			}
			else
			{
				softwareProcessed = SBomGenerator.generateSBom(cli);
			}
		}
		catch (SBomException sbe)
		{
			logger.error(sbe.getMessage(), sbe);
			failed = true;
		}
		catch (Exception e)
		{
			failed = true;
			logger.error("Encountered an unexpected error while attempting to build the Software " +
					"Bill Of Materials for your operating system!", e);
			
		}
		finally
		{
			if (logger.isInfoEnabled())
			{
				StringBuilder msg = new StringBuilder("It took " + DateUtils.computeDiff(startDate,
						DateUtils.rightNowDate()));
				if (failed)
					msg.append(" to fail to ");
				else
					msg.append(" to successfully ");

				if (runningHelp)
					msg.append("show the usage.");
				else
				{
					msg.append("build the Software Bill Of Materials (SBOM)!");
					if (!failed)
						msg.append("  With " + softwareProcessed + " components!");
				}
				logger.info(msg.toString());
			}
		}
	}

	/**
	 * (U) This method is used to create the valid options for command line usage.
	 * 
	 * @return Options for use when running via command line.
	 */
	private static Options createCliOptions()
	{
		Options cliOptions = new Options();
		cliOptions.addOption(new Option("h", "help", false, "will print out the command line " +
						"options."));
		cliOptions.addOption(new Option("i", "image", true,
						"Docker Image file to use as top level component."));
		cliOptions.addOption(new Option("g", "group", true,
						"Group value to assign to top level component."));
		cliOptions.addOption(new Option("n", "name", true,
						"Name value to assign to top level component."));
		cliOptions.addOption(new Option("v", "version", true,
						"Version value to assign to top level component."));
		cliOptions.addOption(new Option("nc", "no-components", false, "Will only campture master " +
						"component.  Will not include any components in the list of Components."));

		return cliOptions;
	}

}