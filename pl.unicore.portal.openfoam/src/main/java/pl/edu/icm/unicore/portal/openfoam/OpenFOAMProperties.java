package pl.edu.icm.unicore.portal.openfoam;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import eu.unicore.util.configuration.ConfigurationException;
import eu.unicore.util.configuration.PropertiesHelper;
import eu.unicore.util.configuration.PropertyMD;

/**
 * 
 * OpenFOAM plugin config
 * 
 * @author mateusz
 *
 */
public class OpenFOAMProperties extends PropertiesHelper {
	
	private static final Logger log = Logger.getLogger(OpenFOAMProperties.class);

	public static final String PREFIX = "portal.angiomerge.";
	
	public static final Map<String, PropertyMD> META = new HashMap<String, PropertyMD>();
	
	public OpenFOAMProperties(Properties properties) throws ConfigurationException
	{
		super(PREFIX, properties, META, log);
	}

}
