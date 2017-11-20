package pl.edu.icm.unicore.portal.openfoam;

import eu.unicore.griddisco.core.model.BrokerService;
import eu.unicore.griddisco.core.model.StorageFactoryService;

public class OpenFOAMGridEnvironment {
	public final BrokerService broker;
	public final StorageFactoryService sfs;
	
	public OpenFOAMGridEnvironment(BrokerService broker, StorageFactoryService sfs)
	{
		this.broker = broker;
		this.sfs = sfs;
	}	

}
