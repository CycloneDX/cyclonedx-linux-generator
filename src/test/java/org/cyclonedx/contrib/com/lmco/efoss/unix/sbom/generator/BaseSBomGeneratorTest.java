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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import org.cyclonedx.contrib.com.lmco.efoss.unix.sbom.utils.Log4JTestWatcher;

/**
 * (U) This is the base class used for any common SBomGeneratorTest methods.
 * 
 * @author wrgoff
 * @since 29 Apr 2020
 */
public class BaseSBomGeneratorTest
{
	
	/**
	 * (U) This method is used to get the list of expected Details.
	 * 
	 * @param fileName String value of the name of the file to get the expected details from.
	 * @param watcher  Log4JTestWatcher to send any logs to.
	 * @return Map containing the keys and their values.
	 */
	protected Map<String, String> getDetailMap(String fileName, Log4JTestWatcher watcher)
	{
		Map<String, String> expectedDetails = new HashMap<String, String>();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				UnixSBomGeneratorTest.class.getResourceAsStream(fileName))))
		{
			String line;
			String key;
			String value;
			int index = 0;
			while ((line = reader.readLine()) != null)
			{
				if (line.contains(":"))
				{
					index = line.indexOf(':');
					key = line.substring(0, index);
					value = line.substring(index + 1);
					expectedDetails.put(key.trim(), value.trim());
				}
			}
		}
		catch (IOException ioe)
		{
			String error = "Our Test case failed to read the Expected Package Details " +
					"file(" + fileName + ").";
			watcher.getLogger().error(error, ioe);
			Assert.fail("Unable to read Expected Package Details File (" + fileName + "). ");
		}
		return expectedDetails;
	}
	
	/**
	 * (U) This method is used to get a Detail Map as a String, mostly used for debugging.
	 * @param detailMap Map to get the values from.
	 * @param tabs String value for the indentation.
	 * @return String a nicely formatted string.
	 */
	protected String getDetailMapAsString(Map<String, String> detailMap, String tabs)
	{
		StringBuilder sb = new StringBuilder();
		
		Set<String> keys = detailMap.keySet();
		for(String key: keys)
			sb.append(tabs + key + " : " + detailMap.get(key) + "\n");
		
		return sb.toString();
	}
}
