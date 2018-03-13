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

	public static final String PREFIX = "portal.openfoam.";
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
	
	public OpenFOAMProperties(Properties properties) throws ConfigurationException
	{
		super(PREFIX, properties, META, log);
	}

}
