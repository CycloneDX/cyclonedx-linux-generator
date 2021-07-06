/*
 * Copyright (c) 2018,2019 Lockheed Martin Corporation.
 *
 * This work is owned by Lockheed Martin Corporation. Lockheed Martin personnel are permitted to use and
 * modify this software.  Lockheed Martin personnel may also deliver this source code to any US Government
 * customer Agency under a "US Government Purpose Rights" license.
 *
 * See the LICENSE file distributed with this work for licensing and distribution terms
 */
package com.lmco.efoss.unix.sbom.exceptions;

/**
 * (U) This class is the Software Bill Of Materials (SBOM) Exception class.
 * 
 * @author wrgoff
 * @since 22 April 2020
 */
public class SBomException extends Exception
{
	private static final long serialVersionUID = 8505738900643528230L;
	
	/**
	 * (U) Constructs a new SBomException with null as its details.
	 */
	public SBomException()
	{}
	
	/**
	 * (U) Constructs a new SBomException with the specified detail message.
	 *
	 * @param message String value to set the message to.
	 */
	public SBomException(String message)
	{
		super(message);
	}
	
	/**
	 * (U) Constructs a new SBomException with the specified detail message and cause.
	 *
	 * @param message String value to set the message to.
	 * @param cause   Throwable class to set the cause to.
	 */
	public SBomException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * (U) Constructs a new SBomException with the specified detail message, cause, suppression flag
	 * set to either enabled or disabled, and the writable stack trace flag set to either enable or
	 * disabled.
	 *
	 * @param message             String value to set the message to.
	 * @param cause               Throwable class to set the cause to.
	 * @param enableSuppression   boolean used to set the enabled suppression flag to.
	 * @param writeableStackTrace boolean used to set the write able stack trace flag to.
	 */
	public SBomException(String message, Throwable cause, boolean enableSuppression,
			boolean writeableStackTrace)
	{
		super(message, cause, enableSuppression, writeableStackTrace);
	}
	
	/**
	 * (U) Constructs a new SBomException with the cause set.
	 * 
	 * @param cause Throwable class to set the cause to.
	 */
	public SBomException(Throwable cause)
	{
		super(cause);
	}
}
