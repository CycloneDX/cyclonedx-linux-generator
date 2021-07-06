/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package com.lmco.efoss.unix.sbom.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * (U) This utility class contains utilities for the manipulation of Dates.
 *
 * @author wrgoff
 * @since 22 April 2020
 */
public class DateUtils
{
	public enum TimeZoneLocale
	{
		GMT, LOCAL
	}
	
	private static final String DATE_FORMAT_PATTERN = "yyyy MM dd HH:mm:ss.SSS";
	
	
	/**
	 * (U) This method is used to compute the difference between dates. It produces a nice readable formatted
	 * String.
	 *
	 * @param date1 Date to compare to the second date passed in.
	 * @param date2 Date to compare to the first date passed in.
	 * @return String a nicely formatted human readable string stating the difference between date 1 and date 2
	 *         passed in.
	 */
	public static String computeDiff(Date date1, Date date2)
	{
		Objects.requireNonNull(date1, "Unable to compute the difference from a null date (date1)!");
		Objects.requireNonNull(date2, "Unable to compute the difference from a null date (date2)!");
		
		long diffInMillies = date2.getTime() - date1.getTime();
		List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
		
		Collections.reverse(units);
		Map<TimeUnit, Long> result = new TreeMap<>();
		long milliesRest = diffInMillies;
		
		for (TimeUnit unit : units)
		{
			long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
			long diffInMilliesForUnit = unit.toMillis(diff);
			milliesRest = milliesRest - diffInMilliesForUnit;
			result.put(unit, diff);
		}
		
		StringBuilder message = new StringBuilder();
		Set<TimeUnit> timeUnitsSet = result.keySet();
		List<TimeUnit> timeUnits = new ArrayList<>(timeUnitsSet);
		
		Collections.sort(timeUnits, Collections.reverseOrder());
		
		long timeUnitValue;
		
		for (TimeUnit timeUnit : timeUnits)
		{
			timeUnitValue = result.get(timeUnit);
			
			if (timeUnitValue > 0)
			{
				if (message.length() > 0)
				{
					message.append(", ");
				}
				message.append(timeUnitValue + " " + timeUnit);
			}
		}
		
		if (message.length() == 0)
		{
			message.append("0 MILLISECONDS");
		}
		return message.toString();
	}
	
	/**
	 * (U) This method is used to produce a pretty String (used mostly for debugging) of a date.
	 *
	 * @param myDate Date to represent in a pretty formatted String.
	 * @return String value of the date passed in.
	 */
	public static String dateAsPrettyString(Date myDate)
	{
		return (dateAsPrettyString(myDate, TimeZoneLocale.GMT));
	}
	
	/**
	 * (U) This method is used to produce a pretty String (used mostly for debugging) of a date.
	 *
	 * @param myDate Date to represent in a pretty formatted String.
	 * @param locale Enumeration that tell us if the user wants GMT or local time.
	 * @return String value of the date passed in.
	 */
	public static String dateAsPrettyString(Date myDate, TimeZoneLocale locale)
	{
		String dateString = "";
		if (myDate != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PATTERN);
			
			if (TimeZoneLocale.GMT.equals(locale))
			{
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			}
			dateString = sdf.format(myDate);
		}
		return dateString;
	}
	
	/**
	 * (U) This method gets the current date.
	 *
	 * @return Date the current date/time.
	 */
	public static Date rightNowDate()
	{
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}
	
	/**
	 * (U) This method converts a date into a nice readable String.
	 *
	 * @param myDate java.util.Date to produce a readable string from.
	 * @return String the readable format of the date.
	 */
	public static String toString(Date myDate)
	{
		Objects.requireNonNull(myDate, "Cannot convert a null java.util.Date to a String!");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		
		return (simpleDateFormat.format(myDate));
	}
	
	/**
	 * (U) Base Constructor.
	 */
	private DateUtils()
	{}
}
