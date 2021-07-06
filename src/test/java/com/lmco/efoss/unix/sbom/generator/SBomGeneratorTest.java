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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Tool;
import org.cyclonedx.parsers.Parser;
import org.cyclonedx.parsers.XmlParser;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.lmco.efoss.unix.sbom.exceptions.SBomException;
import com.lmco.efoss.unix.sbom.utils.DateUtils;
import com.lmco.efoss.unix.sbom.utils.Log4JTestWatcher;
import com.lmco.efoss.unix.sbom.utils.TestUtils;

/**
 * (U) This class contains the test cases for the SBomGenerator.
 * 
 * @author wrgoff
 * @since 15 October 2020
 */
class SBomGeneratorTest
{
	private static final String LOG4J_FILE = "SBomGeneratorTestLog4J.xml";
	
	public Log4JTestWatcher watcher = new Log4JTestWatcher(LOG4J_FILE, this.getClass().getName());
	
	/**
	 * (U) This method is used to test that we added the UnixSBomGenerator tool as the first tool in
	 * the list.
	 */
	@Test
	void addBomToolsTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/sboms/bom.xml";
		
		String expectedToolName = "UnixSBomGenerator";
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Parser parser = new XmlParser();
			Bom sbom = parser.parse(stream);
			
			SBomGenerator.addBomTools(sbom);
			
			List<Tool> tools = sbom.getMetadata().getTools();
			
			Tool tool = tools.get(0);
			
			Assert.assertEquals(expectedToolName, tool.getName());
		}
		catch (Exception sbe)
		{
			String error = "Failed to Add Unix Bom Tool to the list of tools for " +
					"the SBom!";
			watcher.getLogger().error(error, sbe);
			Assert.fail(error);
		}
		finally
		{
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
	
	/**
	 * (U) This method is used to test the parsing of the Map into External References.
	 */
	@Test
	void geneateBomsTest()
	{
		String methodName = new Object()
		{}.getClass().getEnclosingMethod().getName();
		
		Date startDate = DateUtils.rightNowDate();
		
		TestUtils.logTestStart(methodName, watcher.getLogger());
		
		String file = "/sboms/bom.xml";
		
		try (InputStream stream = UnixSBomGeneratorTest.class.getResourceAsStream(file))
		{
			Parser parser = new XmlParser();
			Bom sbom = parser.parse(stream);
			SBomGenerator.addBomTools(sbom);
				
			watcher.getLogger().info("sbom: " + sbom.toString());
				
			SBomGenerator.geneateBoms(sbom);
				
			Bom generatedSbom = parser.parse(new File("output/bom.xml"));
				
			if (sbom.getComponents().size() == generatedSbom.getComponents().size())
			{
				watcher.getLogger().info("SBomGenerator generated the same number of " +
						"components (" + sbom.getComponents().size() + ") as the original!");
			}
			else
				watcher.getLogger().error("SBomGenerator did NOT generate the same number of " +
						"of components as the original.  Generated: " +
						generatedSbom.getComponents().size() + ", original: " +
						sbom.getComponents().size() + ".");
				
			Assert.assertEquals(sbom.getComponents().size(),
					generatedSbom.getComponents().size());
		}
		catch (IOException ioe)
		{
			String error = "Our Test case geneateBomsTest failed to read the Test SBom " +
					"from a file(" + file + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read our Test SBom File (" + file + "). ");
		}
		catch (ParseException e)
		{
			String error = "Failed to parse the SBom from the file (" + file + ");";
			watcher.getLogger().error(error, e);
			Assert.fail(error);
		}
		catch (SBomException sbe)
		{
			String error = "Failed to generate SBom file from Sbom!";
			watcher.getLogger().error(error, sbe);
			Assert.fail(error);
		}
		finally
		{
			try
			{
				Files.delete(Paths.get("output/bom.xml"));
				Files.delete(Paths.get("output/bom.json"));
			}
			catch (Exception e)
			{
				watcher.getLogger().warn("Filed to cleanup output files!");
			}
			TestUtils.logTestFinish(methodName, startDate, watcher.getLogger());
		}
	}
}
