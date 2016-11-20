/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.angio;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * AngioMerge configuration
 * @author K. Benedyczak
 */
public class AngioProperties extends PropertiesHelper
{
	private static final Logger log = Logger.getLogger(AngioProperties.class);

	public static final String PREFIX = "portal.angiomerge.";
	public static final String GRANTS_ATTRIBUTE = "grantsAttribute";
	
	public static final Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();
	
	static
	{
		META.put(GRANTS_ATTRIBUTE, new PropertyMD("grants").
				setDescription("The value of this property is used as a name of attribute as provided "
						+ "by external identity provider service (typically Unity). "
						+ "If attribute with the name configured here is found for the authenticated user,"
						+ " then its value is used as a list of grants allowed to be chosen for the user. "
						+ "Otherwise any grant name may be provided."));
	}
	
	
	public AngioProperties(Properties properties) throws ConfigurationException
	{
		super(PREFIX, properties, META, log);
	}
}
