/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.sinusmed;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sinusmed configuration
 * @author K. Benedyczak
 */
public class SinusMedProperties extends PropertiesHelper
{
	private static final Logger log = Logger.getLogger(SinusMedProperties.class);
	public static final String MSG_ID = "sinusmed";

	public static final String PREFIX = "portal.sinusmed.";
	public static final String WORKSPACE = "workspace";
	public static final String GRANTS_ATTRIBUTE = "grantsAttribute";
	
	public static final Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();
	
	static
	{
		META.put(WORKSPACE, new PropertyMD().setMandatory().setPath().
				setDescription("Directory path where user's workspaces will be located."
						+ " Note that files stored there might be large (up to 500MB per job)"));
		META.put(GRANTS_ATTRIBUTE, new PropertyMD("grants").
				setDescription("The value of this property is used as a name of attribute as provided "
						+ "by external identity provider service (typically Unity). "
						+ "If attribute with the name configured here is found for the authenticated user,"
						+ " then its value is used as a list of grants allowed to be chosen for the user. "
						+ "Otherwise any grant name may be provided."));
	}
	
	
	public SinusMedProperties(Properties properties) throws ConfigurationException
	{
		super(PREFIX, properties, META, log);
	}
}
